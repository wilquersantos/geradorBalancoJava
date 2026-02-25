package com.emissor.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.emissor.mobile.data.local.entity.ItemEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEditDialog(
    item: ItemEntity,
    onDismiss: () -> Unit,
    onSave: (ItemEntity) -> Unit,
    onDelete: (ItemEntity) -> Unit
) {
    var quantidade by remember { mutableStateOf(item.quantidade.toString()) }
    var descricao by remember { mutableStateOf(item.descricao) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Editar Item",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Fechar")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Barcode (read-only)
                OutlinedTextField(
                    value = item.codigoReferencia,
                    onValueChange = {},
                    label = { Text("Código de Barras") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Quantity
                OutlinedTextField(
                    value = quantidade,
                    onValueChange = { 
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            quantidade = it
                        }
                    },
                    label = { Text("Quantidade") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Description
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Delete button
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Excluir")
                    }
                    
                    Row {
                        // Cancel button
                        TextButton(onClick = onDismiss) {
                            Text("Cancelar")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Save button
                        Button(
                            onClick = {
                                val qtd = quantidade.toIntOrNull() ?: item.quantidade
                                val updatedItem = item.copy(
                                    quantidade = qtd,
                                    descricao = descricao,
                                    sincronizado = false // Mark as unsynced
                                )
                                onSave(updatedItem)
                            },
                            enabled = quantidade.toIntOrNull() != null && quantidade.toInt() > 0
                        ) {
                            Text("Salvar")
                        }
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Excluir item?") },
            text = { Text("Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(item)
                        showDeleteConfirm = false
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
