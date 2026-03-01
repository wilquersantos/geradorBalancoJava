package com.emissor.mobile.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    
    companion object {
        private val SERVER_IP = stringPreferencesKey("server_ip")
        private val SERVER_PORT = stringPreferencesKey("server_port")
        private val API_TOKEN = stringPreferencesKey("api_token")
        private val COLETA_NOME = stringPreferencesKey("coleta_nome")
        private val AUTO_QUANTITY = booleanPreferencesKey("auto_quantity")
        private val AUTO_SYNC = booleanPreferencesKey("auto_sync")
        
        const val DEFAULT_IP = "192.168.1.136"
        const val DEFAULT_PORT = "8084"
        const val DEFAULT_TOKEN = "emissor-token-2026"
        const val DEFAULT_COLETA = "GERAL"
    }
    
    val serverIp: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SERVER_IP] ?: DEFAULT_IP
    }
    
    val serverPort: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SERVER_PORT] ?: DEFAULT_PORT
    }
    
    val apiToken: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[API_TOKEN] ?: DEFAULT_TOKEN
    }

    val coletaNome: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[COLETA_NOME] ?: DEFAULT_COLETA
    }
    
    val autoQuantity: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_QUANTITY] ?: true
    }
    
    val autoSync: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_SYNC] ?: true
    }
    
    val baseUrl: Flow<String> = context.dataStore.data.map { preferences ->
        val ip = preferences[SERVER_IP] ?: DEFAULT_IP
        val port = preferences[SERVER_PORT] ?: DEFAULT_PORT
        "http://$ip:$port/"
    }
    
    suspend fun setServerIp(ip: String) {
        context.dataStore.edit { preferences ->
            preferences[SERVER_IP] = ip
        }
    }
    
    suspend fun setServerPort(port: String) {
        context.dataStore.edit { preferences ->
            preferences[SERVER_PORT] = port
        }
    }
    
    suspend fun setApiToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[API_TOKEN] = token
        }
    }

    suspend fun setColetaNome(coletaNome: String) {
        context.dataStore.edit { preferences ->
            val sanitized = coletaNome.trim().ifEmpty { DEFAULT_COLETA }
            preferences[COLETA_NOME] = sanitized
        }
    }
    
    suspend fun setAutoQuantity(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_QUANTITY] = enabled
        }
    }
    
    suspend fun setAutoSync(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_SYNC] = enabled
        }
    }
}
