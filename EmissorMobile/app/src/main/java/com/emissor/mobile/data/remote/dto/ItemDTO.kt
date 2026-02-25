package com.emissor.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ItemRequestDTO(
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
