package com.emissor.mobile.data.local.dao

import androidx.room.*
import com.emissor.mobile.data.local.entity.CollectionGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionGroupDao {
    @Query("SELECT * FROM collection_groups ORDER BY dataCriacao DESC")
    fun getAllGroups(): Flow<List<CollectionGroupEntity>>

    @Query("SELECT * FROM collection_groups WHERE id = :id")
    suspend fun getGroupById(id: Long): CollectionGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: CollectionGroupEntity): Long

    @Update
    suspend fun updateGroup(group: CollectionGroupEntity)

    @Delete
    suspend fun deleteGroup(group: CollectionGroupEntity)

    @Query("UPDATE collection_groups SET sincronizado = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Long)
}
