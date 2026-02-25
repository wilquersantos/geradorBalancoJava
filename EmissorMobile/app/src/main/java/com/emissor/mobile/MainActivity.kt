package com.emissor.mobile

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.emissor.mobile.data.local.entity.ItemEntity
import com.emissor.mobile.ui.screens.ItemEditDialog
import com.emissor.mobile.ui.screens.ItemsListScreen
import com.emissor.mobile.ui.screens.SettingsScreen
import com.emissor.mobile.ui.scanner.BarcodeScannerScreen
import com.emissor.mobile.ui.theme.EmissorMobileTheme
import com.emissor.mobile.ui.viewmodel.MainViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            EmissorMobileTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val items by viewModel.items.collectAsState()
    val unsyncedCount by viewModel.unsyncedCount.collectAsState()
    val showScanner by viewModel.showScanner.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val autoQuantity by viewModel.autoQuantity.collectAsState()
    val autoSync by viewModel.autoSync.collectAsState()
    val serverIp by viewModel.serverIp.collectAsState()
    val serverPort by viewModel.serverPort.collectAsState()
    val apiToken by viewModel.apiToken.collectAsState()
    
    var selectedItem by remember { mutableStateOf<ItemEntity?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    // Handle UI state messages
    LaunchedEffect(uiState) {
        when (uiState) {
            is MainViewModel.UiState.Success -> {
                snackbarHostState.showSnackbar(
                    (uiState as MainViewModel.UiState.Success).message
                )
                viewModel.clearUiState()
            }
            is MainViewModel.UiState.Error -> {
                snackbarHostState.showSnackbar(
                    (uiState as MainViewModel.UiState.Error).message,
                    duration = SnackbarDuration.Long
                )
                viewModel.clearUiState()
            }
            else -> {}
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when {
                showSettings -> {
                    SettingsScreen(
                        serverIp = serverIp,
                        serverPort = serverPort,
                        apiToken = apiToken,
                        autoQuantity = autoQuantity,
                        autoSync = autoSync,
                        onServerIpChange = viewModel::setServerIp,
                        onServerPortChange = viewModel::setServerPort,
                        onApiTokenChange = viewModel::setApiToken,
                        onAutoQuantityChange = viewModel::setAutoQuantity,
                        onAutoSyncChange = viewModel::setAutoSync,
                        onTestConnection = viewModel::checkServerConnection,
                        onBack = { showSettings = false }
                    )
                }
                showScanner && cameraPermissionState.status.isGranted -> {
                    BarcodeScannerScreen(
                        onBarcodeScanned = viewModel::onBarcodeScanned,
                        onClose = { viewModel.showScanner(false) }
                    )
                }
                else -> {
                    ItemsListScreen(
                        items = items,
                        unsyncedCount = unsyncedCount,
                        onScanClick = {
                            if (cameraPermissionState.status.isGranted) {
                                viewModel.showScanner(true)
                            } else {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        },
                        onItemClick = { selectedItem = it },
                        onSyncClick = viewModel::syncAllItems,
                        onSettingsClick = { showSettings = true },
                        onDeleteAllClick = viewModel::deleteAllItems
                    )
                }
            }
            
            // Item edit dialog
            selectedItem?.let { item ->
                ItemEditDialog(
                    item = item,
                    onDismiss = { selectedItem = null },
                    onSave = { updatedItem ->
                        viewModel.updateItem(updatedItem)
                        selectedItem = null
                    },
                    onDelete = { itemToDelete ->
                        viewModel.deleteItem(itemToDelete)
                        selectedItem = null
                    }
                )
            }
            
            // Loading overlay
            if (uiState is MainViewModel.UiState.Loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
