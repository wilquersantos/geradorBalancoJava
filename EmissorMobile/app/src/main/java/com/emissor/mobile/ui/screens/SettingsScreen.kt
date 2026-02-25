package com.emissor.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    serverIp: String,
    serverPort: String,
    apiToken: String,
    autoQuantity: Boolean,
    autoSync: Boolean,
    onServerIpChange: (String) -> Unit,
    onServerPortChange: (String) -> Unit,
    onApiTokenChange: (String) -> Unit,
    onAutoQuantityChange: (Boolean) -> Unit,
    onAutoSyncChange: (Boolean) -> Unit,
    onTestConnection: () -> Unit,
    onBack: () -> Unit
) {
    var localIp by remember { mutableStateOf(serverIp) }
    var localPort by remember { mutableStateOf(serverPort) }
    var localToken by remember { mutableStateOf(apiToken) }
    var connectionStatus by remember { mutableStateOf<ConnectionStatus?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Server Configuration Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Servidor",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    OutlinedTextField(
                        value = localIp,
                        onValueChange = {
                            localIp = it
                            onServerIpChange(it)
                        },
                        label = { Text("IP do Servidor") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("192.168.1.136") }
                    )
                    
                    OutlinedTextField(
                        value = localPort,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() }) {
                                localPort = it
                                onServerPortChange(it)
                            }
                        },
                        label = { Text("Porta") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("8084") }
                    )
                    
                    OutlinedTextField(
                        value = localToken,
                        onValueChange = {
                            localToken = it
                            onApiTokenChange(it)
                        },
                        label = { Text("Token de Autenticação") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("emissor-token-2026") }
                    )
                    
                    // Test connection button
                    Button(
                        onClick = {
                            connectionStatus = ConnectionStatus.Testing
                            onTestConnection()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Testar Conexão")
                    }
                    
                    // Connection status
                    when (connectionStatus) {
                        ConnectionStatus.Testing -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Testando conexão...")
                            }
                        }
                        ConnectionStatus.Success -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Servidor conectado!",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        ConnectionStatus.Error -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Erro de conexão",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        null -> {}
                    }
                    
                    Divider()
                    
                    Text(
                        text = "URL: http://$localIp:$localPort/api/items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            // App Behavior Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Comportamento",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    // Auto quantity toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Quantidade Automática",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Incrementar ao escanear o mesmo código",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Switch(
                            checked = autoQuantity,
                            onCheckedChange = onAutoQuantityChange
                        )
                    }
                    
                    Divider()
                    
                    // Auto sync toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sincronização Automática",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Enviar automaticamente para o servidor",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Switch(
                            checked = autoSync,
                            onCheckedChange = onAutoSyncChange
                        )
                    }
                }
            }
            
            // Info section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Sobre o App",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Emissor Mobile v1.0\nLeitor de código de barras com sincronização",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

enum class ConnectionStatus {
    Testing, Success, Error
}
