package com.emissor.mobile.data.local.dao

import androidx.room.*
import com.emissor.mobile.data.local.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    
    @Query("SELECT * FROM items ORDER BY dataCriacao DESC")
    fun getAllItems(): Flow<List<ItemEntity>>
    
    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: Long): ItemEntity?
    
    @Query("SELECT * FROM items WHERE codigoReferencia = :codigo LIMIT 1")
    suspend fun getItemByCodigo(codigo: String): ItemEntity?
    
    @Query("SELECT * FROM items WHERE sincronizado = 0")
    suspend fun getUnsyncedItems(): List<ItemEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long
    
    @Update
    suspend fun updateItem(item: ItemEntity)
    
    @Delete
    suspend fun deleteItem(item: ItemEntity)
    
    @Query("DELETE FROM items")
    suspend fun deleteAllItems()
    
    @Query("UPDATE items SET sincronizado = 1, idServidor = :idServidor WHERE id = :id")
    suspend fun markAsSynced(id: Long, idServidor: Long)
    
    @Query("SELECT COUNT(*) FROM items")
    fun getItemCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM items WHERE sincronizado = 0")
    fun getUnsyncedCount(): Flow<Int>
}
