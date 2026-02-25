package com.emissor.mobile.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.emissor.mobile.data.local.AppDatabase
import com.emissor.mobile.data.local.entity.ItemEntity
import com.emissor.mobile.data.preferences.PreferencesManager
import com.emissor.mobile.data.repository.ItemRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val preferencesManager = PreferencesManager(application)
    private val repository = ItemRepository(database.itemDao(), preferencesManager)
    
    // State flows
    val items = repository.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val itemCount = repository.getItemCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    val unsyncedCount = repository.getUnsyncedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    val autoQuantity = preferencesManager.autoQuantity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    val autoSync = preferencesManager.autoSync
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    val serverIp = preferencesManager.serverIp
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 
            PreferencesManager.DEFAULT_IP)
    
    val serverPort = preferencesManager.serverPort
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 
            PreferencesManager.DEFAULT_PORT)
    
    val apiToken = preferencesManager.apiToken
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 
            PreferencesManager.DEFAULT_TOKEN)
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    private val _showScanner = MutableStateFlow(false)
    val showScanner: StateFlow<Boolean> = _showScanner.asStateFlow()
    
    // Actions
    fun onBarcodeScanned(barcode: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                
                val item = repository.handleBarcodeScanned(
                    barcode = barcode,
                    autoQuantity = autoQuantity.value
                )
                
                _uiState.value = UiState.Success("Código escaneado: $barcode")
                
                // Auto sync if enabled
                if (autoSync.value) {
                    syncItem(item)
                }
                
                // Close scanner
                _showScanner.value = false
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Erro ao processar código")
            }
        }
    }
    
    fun updateItem(item: ItemEntity) {
        viewModelScope.launch {
            try {
                repository.updateItem(item)
                _uiState.value = UiState.Success("Item atualizado")
                
                if (autoSync.value) {
                    syncItem(item)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Erro ao atualizar item")
            }
        }
    }
    
    fun deleteItem(item: ItemEntity) {
        viewModelScope.launch {
            try {
                repository.deleteItem(item)
                _uiState.value = UiState.Success("Item excluído")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Erro ao excluir item")
            }
        }
    }
    
    fun deleteAllItems() {
        viewModelScope.launch {
            try {
                repository.deleteAllItems()
                _uiState.value = UiState.Success("Todos os itens foram excluídos")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Erro ao excluir itens")
            }
        }
    }
    
    fun syncItem(item: ItemEntity) {
        viewModelScope.launch {
            try {
                val result = repository.syncItemWithServer(item)
                if (result.isSuccess) {
                    _uiState.value = UiState.Success("Item sincronizado")
                } else {
                    _uiState.value = UiState.Error(
                        result.exceptionOrNull()?.message ?: "Erro ao sincronizar"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Erro de conexão")
            }
        }
    }
    
    fun syncAllItems() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val result = repository.syncAllUnsyncedItems()
                
                if (result.isSuccess) {
                    val count = result.getOrDefault(0)
                    _uiState.value = UiState.Success("$count itens sincronizados")
                } else {
                    _uiState.value = UiState.Error(
                        result.exceptionOrNull()?.message ?: "Erro ao sincronizar"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Erro de conexão")
            }
        }
    }
    
    fun checkServerConnection() {
        viewModelScope.launch {
            try {
                val result = repository.checkServerConnection()
                if (result.isSuccess) {
                    _uiState.value = UiState.Success("Servidor conectado")
                } else {
                    _uiState.value = UiState.Error("Servidor não disponível")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Erro de conexão")
            }
        }
    }
    
    // Settings
    fun setServerIp(ip: String) {
        viewModelScope.launch {
            preferencesManager.setServerIp(ip)
        }
    }
    
    fun setServerPort(port: String) {
        viewModelScope.launch {
            preferencesManager.setServerPort(port)
        }
    }
    
    fun setApiToken(token: String) {
        viewModelScope.launch {
            preferencesManager.setApiToken(token)
        }
    }
    
    fun setAutoQuantity(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAutoQuantity(enabled)
        }
    }
    
    fun setAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAutoSync(enabled)
        }
    }
    
    fun showScanner(show: Boolean) {
        _showScanner.value = show
    }
    
    fun clearUiState() {
        _uiState.value = UiState.Idle
    }
    
    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }
}
