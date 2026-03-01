package com.emissor.mobile.data.local.dao

import androidx.room.*
import com.emissor.mobile.data.local.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    
    @Query("SELECT * FROM items WHERE groupId = :groupId ORDER BY dataCriacao DESC")
    fun getItemsByGroup(groupId: Long): Flow<List<ItemEntity>>
    
    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: Long): ItemEntity?
    
    @Query("SELECT * FROM items WHERE groupId = :groupId AND codigoReferencia = :codigo LIMIT 1")
    suspend fun getItemByCodigoInGroup(groupId: Long, codigo: String): ItemEntity?
    
    @Query("SELECT * FROM items WHERE groupId = :groupId AND sincronizado = 0")
    suspend fun getUnsyncedItemsByGroup(groupId: Long): List<ItemEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long
    
    @Update
    suspend fun updateItem(item: ItemEntity)
    
    @Delete
    suspend fun deleteItem(item: ItemEntity)
    
    @Query("DELETE FROM items WHERE groupId = :groupId")
    suspend fun deleteItemsByGroup(groupId: Long)
    
    @Query("UPDATE items SET sincronizado = 1, idServidor = :idServidor WHERE id = :id")
    suspend fun markAsSynced(id: Long, idServidor: Long)
    
    @Query("SELECT COUNT(*) FROM items WHERE groupId = :groupId")
    fun getItemCountByGroup(groupId: Long): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM items WHERE groupId = :groupId AND sincronizado = 0")
    fun getUnsyncedCountByGroup(groupId: Long): Flow<Int>
}
