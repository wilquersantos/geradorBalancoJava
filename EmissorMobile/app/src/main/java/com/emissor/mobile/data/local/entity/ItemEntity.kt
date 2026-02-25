package com.emissor.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val codigoReferencia: String,
    val quantidade: Int,
    val descricao: String = "",
    val dataCriacao: Long = System.currentTimeMillis(),
    val sincronizado: Boolean = false,
    val idServidor: Long? = null
)
