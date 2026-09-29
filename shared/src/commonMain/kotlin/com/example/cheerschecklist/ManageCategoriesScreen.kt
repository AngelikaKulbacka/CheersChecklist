package com.example.cheerschecklist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ManageCategoriesScreen(
    viewModel: TastingViewModel,
    onDone: () -> Unit,
) {
    val customCategories by viewModel.customCategories.collectAsState()
    val language by viewModel.activeLanguage.collectAsState()
    val strings = stringsFor(language)
    var renamingCategory by remember { mutableStateOf<String?>(null) }
    var renameText by remember { mutableStateOf("") }
    var pendingDeleteCategory by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.manageTypesTitle) },
                navigationIcon = {
                    TextButton(onClick = onDone) { Text(strings.back) }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (customCategories.isEmpty()) {
                Text(
                    strings.noCustomTypesYet,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                customCategories.forEach { name ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(name.uppercase(), style = MaterialTheme.typography.bodyLarge)
                        Row {
                            TextButton(onClick = {
                                renamingCategory = name
                                renameText = name
                            }) {
                                Text(strings.rename)
                            }
                            TextButton(onClick = { pendingDeleteCategory = name }) {
                                Text(strings.delete)
                            }
                        }
                    }
                }
            }
        }
    }

    val toRename = renamingCategory
    if (toRename != null) {
        AlertDialog(
            onDismissRequest = { renamingCategory = null },
            title = { Text(strings.renameTypeTitle) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text(strings.typeNameLabel) },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    enabled = renameText.isNotBlank(),
                    onClick = {
                        viewModel.renameCustomCategory(toRename, renameText)
                        renamingCategory = null
                    },
                ) {
                    Text(strings.rename)
                }
            },
            dismissButton = {
                TextButton(onClick = { renamingCategory = null }) {
                    Text(strings.cancel)
                }
            },
        )
    }

    val toDeleteCategory = pendingDeleteCategory
    if (toDeleteCategory != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteCategory = null },
            title = { Text(strings.deleteTypeTitle) },
            text = {
                Text(strings.deleteTypeText(toDeleteCategory.uppercase()))
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomCategory(toDeleteCategory)
                        pendingDeleteCategory = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteCategory = null }) {
                    Text(strings.cancel)
                }
            },
        )
    }
}
