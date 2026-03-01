package com.emissor.mobile.data.repository

import com.emissor.mobile.data.local.dao.ItemDao
import com.emissor.mobile.data.local.entity.ItemEntity
import com.emissor.mobile.data.preferences.PreferencesManager
import com.emissor.mobile.data.remote.RetrofitClient
import com.emissor.mobile.data.remote.dto.ItemRequestDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ItemRepository(
    private val itemDao: ItemDao,
    private val preferencesManager: PreferencesManager
) {
    
    // Local database operations
    fun getItemsByGroup(groupId: Long): Flow<List<ItemEntity>> = itemDao.getItemsByGroup(groupId)
    
    fun getItemCountByGroup(groupId: Long): Flow<Int> = itemDao.getItemCountByGroup(groupId)
    
    fun getUnsyncedCountByGroup(groupId: Long): Flow<Int> = itemDao.getUnsyncedCountByGroup(groupId)
    
    suspend fun getItemById(id: Long): ItemEntity? = itemDao.getItemById(id)
    
    suspend fun getItemByCodigoInGroup(groupId: Long, codigo: String): ItemEntity? = 
        itemDao.getItemByCodigoInGroup(groupId, codigo)
    
    suspend fun insertItem(item: ItemEntity): Long = itemDao.insertItem(item)
    
    suspend fun updateItem(item: ItemEntity) = itemDao.updateItem(item)
    
    suspend fun deleteItem(item: ItemEntity) = itemDao.deleteItem(item)
    
    suspend fun deleteItemsByGroup(groupId: Long) = itemDao.deleteItemsByGroup(groupId)
    
    // Barcode scanning logic
    suspend fun handleBarcodeScanned(
        groupId: Long,
        barcode: String,
        autoQuantity: Boolean
    ): Pair<ItemEntity, Boolean> {
        val existingItem = itemDao.getItemByCodigoInGroup(groupId, barcode)
        
        return if (existingItem != null) {
            val updated = existingItem.copy(
                quantidade = existingItem.quantidade + 1,
                sincronizado = false
            )
            updateItem(updated)
            Pair(updated, false)
        } else {
            val newItem = ItemEntity(
                groupId = groupId,
                codigoReferencia = barcode,
                quantidade = 1,
                descricao = "",
                sincronizado = false
            )
            val id = insertItem(newItem)
            Pair(newItem.copy(id = id), true)
        }
    }
    
    // Server synchronization
    suspend fun syncItemWithServer(coleta: String, item: ItemEntity): Result<ItemEntity> {
        return try {
            val baseUrl = preferencesManager.baseUrl.first()
            val token = preferencesManager.apiToken.first()
            val api = RetrofitClient.getApi(baseUrl)
            
            val request = ItemRequestDTO(
                coleta = coleta,
                codigoReferencia = item.codigoReferencia,
                quantidade = item.quantidade,
                descricao = item.descricao
            )
            
            val response = api.createItem(token, request)
            
            if (response.isSuccessful && response.body() != null) {
                val serverItem = response.body()!!
                val updatedItem = item.copy(
                    sincronizado = true,
                    idServidor = serverItem.id
                )
                updateItem(updatedItem)
                Result.success(updatedItem)
            } else {
                Result.failure(Exception("Erro ao sincronizar: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncAllUnsyncedItemsByGroup(coleta: String, groupId: Long): Result<Int> {
        return try {
            val unsyncedItems = itemDao.getUnsyncedItemsByGroup(groupId)
            var syncedCount = 0
            
            for (item in unsyncedItems) {
                val result = syncItemWithServer(coleta, item)
                if (result.isSuccess) {
                    syncedCount++
                }
            }
            
            Result.success(syncedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun checkServerConnection(): Result<Boolean> {
        return try {
            val baseUrl = preferencesManager.baseUrl.first()
            val api = RetrofitClient.getApi(baseUrl)
            val response = api.checkHealth()
            
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
