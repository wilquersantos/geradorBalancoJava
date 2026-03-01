package com.emissor.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ItemRequestDTO(
    @SerializedName("coleta")
    val coleta: String,

    @SerializedName("codigoReferencia")
    val codigoReferencia: String,
    
    @SerializedName("quantidade")
    val quantidade: Int,
    
    @SerializedName("descricao")
    val descricao: String
)

data class ItemResponseDTO(
    @SerializedName("id")
    val id: Long,

    @SerializedName("coleta")
    val coleta: String? = null,
    
    @SerializedName("codigoReferencia")
    val codigoReferencia: String,
    
    @SerializedName("quantidade")
    val quantidade: Int,
    
    @SerializedName("descricao")
    val descricao: String,
    
    @SerializedName("dataRecebimento")
    val dataRecebimento: String
)

data class HealthResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("service")
    val service: String
)
