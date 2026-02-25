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
    fun getAllItems(): Flow<List<ItemEntity>> = itemDao.getAllItems()
    
    fun getItemCount(): Flow<Int> = itemDao.getItemCount()
    
    fun getUnsyncedCount(): Flow<Int> = itemDao.getUnsyncedCount()
    
    suspend fun getItemById(id: Long): ItemEntity? = itemDao.getItemById(id)
    
    suspend fun getItemByCodigo(codigo: String): ItemEntity? = 
        itemDao.getItemByCodigo(codigo)
    
    suspend fun insertItem(item: ItemEntity): Long = itemDao.insertItem(item)
    
    suspend fun updateItem(item: ItemEntity) = itemDao.updateItem(item)
    
    suspend fun deleteItem(item: ItemEntity) = itemDao.deleteItem(item)
    
    suspend fun deleteAllItems() = itemDao.deleteAllItems()
    
    // Barcode scanning logic
    suspend fun handleBarcodeScanned(
        barcode: String,
        autoQuantity: Boolean
    ): ItemEntity {
        val existingItem = getItemByCodigo(barcode)
        
        return if (existingItem != null && autoQuantity) {
            // Incrementar quantidade automaticamente
            val updated = existingItem.copy(
                quantidade = existingItem.quantidade + 1,
                sincronizado = false // Marcar como não sincronizado
            )
            updateItem(updated)
            updated
        } else if (existingItem != null) {
            // Retornar item existente sem modificar
            existingItem
        } else {
            // Criar novo item
            val newItem = ItemEntity(
                codigoReferencia = barcode,
                quantidade = 1,
                descricao = "",
                sincronizado = false
            )
            val id = insertItem(newItem)
            newItem.copy(id = id)
        }
    }
    
    // Server synchronization
    suspend fun syncItemWithServer(item: ItemEntity): Result<ItemEntity> {
        return try {
            val baseUrl = preferencesManager.baseUrl.first()
            val token = preferencesManager.apiToken.first()
            val api = RetrofitClient.getApi(baseUrl)
            
            val request = ItemRequestDTO(
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
    
    suspend fun syncAllUnsyncedItems(): Result<Int> {
        return try {
            val unsyncedItems = itemDao.getUnsyncedItems()
            var syncedCount = 0
            
            for (item in unsyncedItems) {
                val result = syncItemWithServer(item)
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
