package com.emissor.mobile.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.emissor.mobile.data.local.AppDatabase
import com.emissor.mobile.data.local.entity.CollectionGroupEntity
import com.emissor.mobile.data.local.entity.ItemEntity
import com.emissor.mobile.data.preferences.PreferencesManager
import com.emissor.mobile.data.repository.CollectionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val preferencesManager = PreferencesManager(application)
    private val repository = CollectionRepository(
        database.collectionGroupDao(),
        database.itemDao(),
        preferencesManager
    )
    
    // Groups state
    val groups = repository.getAllGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentGroup = MutableStateFlow<CollectionGroupEntity?>(null)
    val currentGroup: StateFlow<CollectionGroupEntity?> = _currentGroup.asStateFlow()

    // Items for current group
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val itemsInCurrentGroup = _currentGroup
        .filterNotNull()
        .flatMapLatest { group -> repository.getItemsByGroup(group.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val unsyncedCountInCurrentGroup = _currentGroup
        .filterNotNull()
        .flatMapLatest { group -> repository.getUnsyncedCountByGroup(group.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Settings flows
    val autoQuantity = preferencesManager.autoQuantity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    val autoSync = preferencesManager.autoSync
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    val serverIp = preferencesManager.serverIp
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreferencesManager.DEFAULT_IP)
    
    val serverPort = preferencesManager.serverPort
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreferencesManager.DEFAULT_PORT)
    
    val apiToken = preferencesManager.apiToken
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreferencesManager.DEFAULT_TOKEN)
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()
    
    private val _showScanner = MutableStateFlow(false)
    val showScanner: StateFlow<Boolean> = _showScanner.asStateFlow()

    private val _selectedItem = MutableStateFlow<ItemEntity?>(null)
    val selectedItem: StateFlow<ItemEntity?> = _selectedItem.asStateFlow()
    
    // Actions
    fun createCollection(name: String) {
        viewModelScope.launch {
            repository.createGroup(name)
            _uiState.value = UiState.Success("Coleta '$name' criada")
        }
    }

    fun selectGroup(group: CollectionGroupEntity?) {
        _currentGroup.value = group
    }

    fun deleteGroup(group: CollectionGroupEntity) {
        viewModelScope.launch {
            repository.deleteGroup(group)
            if (_currentGroup.value?.id == group.id) {
                _currentGroup.value = null
            }
            _uiState.value = UiState.Success("Coleta excluída")
        }
    }

    fun deleteItemsInCurrentGroup() {
        val group = _currentGroup.value ?: return
        viewModelScope.launch {
            repository.deleteItemsByGroup(group.id)
            _uiState.value = UiState.Success("Itens da coleta removidos")
        }
    }

    fun onBarcodeScanned(barcode: String) {
        val group = _currentGroup.value ?: return
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val result = repository.handleBarcodeScanned(group.id, barcode)
                val item = result.first
                val isNew = result.second
                
                if (isNew) {
                    _selectedItem.value = item
                    _uiState.value = UiState.Success("Novo produto. Informe a descrição.")
                } else {
                    _uiState.value = UiState.Success("Bipado: $barcode. Qtd: ${item.quantidade}")
                    // Trigger auto sync if enabled
                    if (autoSync.value) {
                        syncCurrentGroup()
                    }
                }
                _showScanner.value = false
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Erro ao bipar")
            }
        }
    }
    
    fun updateItem(item: ItemEntity) {
        viewModelScope.launch {
            try {
                repository.updateItem(item)
                _uiState.value = UiState.Success("Item atualizado")
                
                // Trigger auto sync if enabled
                if (autoSync.value) {
                    syncCurrentGroup()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Erro ao atualizar item")
            }
        }
    }
    
    fun deleteItem(item: ItemEntity) {
        viewModelScope.launch {
            repository.deleteItem(item)
            _uiState.value = UiState.Success("Item excluído")
        }
    }
    
    fun syncCurrentGroup() {
        val group = _currentGroup.value ?: return
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val result = repository.syncGroup(group.id)
                if (result.isSuccess) {
                    _uiState.value = UiState.Success("${result.getOrDefault(0)} itens sincronizados")
                } else {
                    _uiState.value = UiState.Error("Erro ao sincronizar")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Erro de conexão")
            }
        }
    }
    
    fun checkServerConnection() {
        viewModelScope.launch {
            _connectionStatus.value = ConnectionStatus.Testing
            val result = repository.checkServerConnection()
            if (result.isSuccess && result.getOrDefault(false)) {
                _connectionStatus.value = ConnectionStatus.Connected
                _uiState.value = UiState.Success("Servidor online")
            } else {
                _connectionStatus.value = ConnectionStatus.Disconnected
                _uiState.value = UiState.Error("Servidor offline")
            }
        }
    }
    
    fun setSelectedItem(item: ItemEntity?) { _selectedItem.value = item }
    fun showScanner(show: Boolean) { _showScanner.value = show }
    fun clearUiState() { _uiState.value = UiState.Idle }
    fun setServerIp(ip: String) { viewModelScope.launch { preferencesManager.setServerIp(ip) } }
    fun setServerPort(port: String) { viewModelScope.launch { preferencesManager.setServerPort(port) } }
    fun setApiToken(token: String) { viewModelScope.launch { preferencesManager.setApiToken(token) } }
    fun setAutoQuantity(enabled: Boolean) { viewModelScope.launch { preferencesManager.setAutoQuantity(enabled) } }
    fun setAutoSync(enabled: Boolean) { viewModelScope.launch { preferencesManager.setAutoSync(enabled) } }

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }
}
