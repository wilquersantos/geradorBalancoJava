package com.emissor.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collection_groups")
data class CollectionGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dataCriacao: Long = System.currentTimeMillis(),
    val sincronizado: Boolean = false
)
