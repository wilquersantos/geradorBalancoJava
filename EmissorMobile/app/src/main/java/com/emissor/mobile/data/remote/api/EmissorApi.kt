package com.emissor.mobile.data.remote.api

import com.emissor.mobile.data.remote.dto.HealthResponse
import com.emissor.mobile.data.remote.dto.ItemRequestDTO
import com.emissor.mobile.data.remote.dto.ItemResponseDTO
import retrofit2.Response
import retrofit2.http.*

interface EmissorApi {
    
    @GET("api/health")
    suspend fun checkHealth(): Response<HealthResponse>
    
    @POST("api/items")
    suspend fun createItem(
        @Header("X-API-Token") token: String,
        @Body item: ItemRequestDTO
    ): Response<ItemResponseDTO>
    
    @GET("api/items")
    suspend fun getAllItems(
        @Header("X-API-Token") token: String
    ): Response<List<ItemResponseDTO>>
    
    @GET("api/items/{id}")
    suspend fun getItemById(
        @Header("X-API-Token") token: String,
        @Path("id") id: Long
    ): Response<ItemResponseDTO>
    
    @PUT("api/items/{id}")
    suspend fun updateItem(
        @Header("X-API-Token") token: String,
        @Path("id") id: Long,
        @Body item: ItemRequestDTO
    ): Response<ItemResponseDTO>
    
    @DELETE("api/items/{id}")
    suspend fun deleteItem(
        @Header("X-API-Token") token: String,
        @Path("id") id: Long
    ): Response<Unit>
}
