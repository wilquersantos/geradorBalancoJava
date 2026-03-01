package com.emissor.mobile.data.repository

import com.emissor.mobile.data.local.dao.CollectionGroupDao
import com.emissor.mobile.data.local.dao.ItemDao
import com.emissor.mobile.data.local.entity.CollectionGroupEntity
import com.emissor.mobile.data.local.entity.ItemEntity
import com.emissor.mobile.data.preferences.PreferencesManager
import com.emissor.mobile.data.remote.RetrofitClient
import com.emissor.mobile.data.remote.dto.ItemRequestDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CollectionRepository(
    private val groupDao: CollectionGroupDao,
    private val itemDao: ItemDao,
    private val preferencesManager: PreferencesManager
) {
    // Group operations
    fun getAllGroups(): Flow<List<CollectionGroupEntity>> = groupDao.getAllGroups()
    
    suspend fun createGroup(name: String): Long {
        return groupDao.insertGroup(CollectionGroupEntity(name = name))
    }
    
    suspend fun deleteGroup(group: CollectionGroupEntity) {
        groupDao.deleteGroup(group)
    }
    
    suspend fun getGroupById(id: Long): CollectionGroupEntity? = groupDao.getGroupById(id)
    
    // Item operations per group
    fun getItemsByGroup(groupId: Long): Flow<List<ItemEntity>> = itemDao.getItemsByGroup(groupId)
    
    fun getUnsyncedCountByGroup(groupId: Long): Flow<Int> = itemDao.getUnsyncedCountByGroup(groupId)
    
    suspend fun handleBarcodeScanned(
        groupId: Long,
        barcode: String
    ): Pair<ItemEntity, Boolean> {
        val existingItem = itemDao.getItemByCodigoInGroup(groupId, barcode)
        
        return if (existingItem != null) {
            val updated = existingItem.copy(
                quantidade = existingItem.quantidade + 1,
                sincronizado = false
            )
            itemDao.updateItem(updated)
            Pair(updated, false)
        } else {
            val newItem = ItemEntity(
                groupId = groupId,
                codigoReferencia = barcode,
                quantidade = 1,
                descricao = "",
                sincronizado = false
            )
            val id = itemDao.insertItem(newItem)
            Pair(newItem.copy(id = id), true)
        }
    }

    suspend fun updateItem(item: ItemEntity) = itemDao.updateItem(item)
    
    suspend fun deleteItem(item: ItemEntity) = itemDao.deleteItem(item)

    suspend fun deleteItemsByGroup(groupId: Long) = itemDao.deleteItemsByGroup(groupId)

    // Sync specific group
    suspend fun syncGroup(groupId: Long): Result<Int> {
        return try {
            val group = groupDao.getGroupById(groupId) ?: return Result.failure(Exception("Grupo não encontrado"))
            val unsyncedItems = itemDao.getUnsyncedItemsByGroup(groupId)
            val baseUrl = preferencesManager.baseUrl.first()
            val token = preferencesManager.apiToken.first()
            val api = RetrofitClient.getApi(baseUrl)
            
            var syncedCount = 0
            for (item in unsyncedItems) {
                val request = ItemRequestDTO(
                    coleta = group.name,
                    codigoReferencia = item.codigoReferencia,
                    quantidade = item.quantidade,
                    descricao = item.descricao
                )
                val response = api.createItem(token, request)
                if (response.isSuccessful && response.body() != null) {
                    val serverItem = response.body()!!
                    itemDao.markAsSynced(item.id, serverItem.id)
                    syncedCount++
                }
            }
            
            if (syncedCount == unsyncedItems.size && unsyncedItems.isNotEmpty()) {
                groupDao.markAsSynced(groupId)
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
