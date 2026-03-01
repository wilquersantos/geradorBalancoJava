package com.emissor.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emissor.mobile.data.local.entity.CollectionGroupEntity
import com.emissor.mobile.data.local.entity.ItemEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsListScreen(
    group: CollectionGroupEntity,
    items: List<ItemEntity>,
    unsyncedCount: Int,
    onScanClick: () -> Unit,
    onItemClick: (ItemEntity) -> Unit,
    onSyncClick: () -> Unit,
    onBackClick: () -> Unit,
    onDeleteItemsClick: (Long) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(group.name, style = MaterialTheme.typography.titleMedium)
                        Text("Coleta em andamento", style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                },
                actions = {
                    if (unsyncedCount > 0) {
                        BadgedBox(
                            badge = { Badge { Text(unsyncedCount.toString()) } },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            IconButton(onClick = onSyncClick) {
                                Icon(Icons.Default.CloudUpload, "Sincronizar Grupo")
                            }
                        }
                    } else if (items.isNotEmpty()) {
                        IconButton(onClick = onSyncClick) {
                            Icon(Icons.Default.CloudDone, "Sincronizado")
                        }
                    }
                    
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.DeleteSweep, "Limpar itens")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onScanClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.QrCodeScanner, "Escanear")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (items.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.QrCode2, null, modifier = Modifier.size(120.dp), tint = MaterialTheme.colorScheme.outline)
                    Text("Nenhum item nesta coleta", color = MaterialTheme.colorScheme.outline)
                    Button(onClick = onScanClick, Modifier.padding(top = 16.dp)) {
                        Text("Começar a Bipar")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        ItemCard(item = item, onClick = { onItemClick(item) })
                    }
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Limpar itens desta coleta?") },
            text = { Text("Isso removerá todos os bips feitos neste grupo.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteItemsClick(group.id)
                    showDeleteDialog = false
                }) { Text("Limpar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun ItemCard(item: ItemEntity, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(if (item.sincronizado) Color(0xFF4CAF50) else Color(0xFFFF9800)))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (item.descricao.isNotBlank()) item.descricao else "Sem descrição",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = item.codigoReferencia,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("Bipado às " + dateFormat.format(Date(item.dataCriacao)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary) {
                Text(item.quantidade.toString(), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
