package com.example.cheerschecklist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    viewModel: TastingViewModel,
    onAddNew: () -> Unit,
    onEditEntry: (TastedDrink) -> Unit,
    onManageCategories: () -> Unit,
) {
    val entries by viewModel.entries.collectAsState()
    val categoryFilter by viewModel.activeCategoryFilter.collectAsState()
    val activeSortOption by viewModel.activeSortOption.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    var searchText by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<TastedDrink?>(null) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ModalDrawerSheet {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("Filters & sorting", style = MaterialTheme.typography.titleMedium)

                            val filterOptions: List<String?> = listOf(null) + availableCategories
                            ExposedDropdownMenuBox(
                                expanded = categoryDropdownExpanded,
                                onExpandedChange = { categoryDropdownExpanded = it },
                            ) {
                                OutlinedTextField(
                                    value = categoryFilter?.uppercase() ?: "ALL",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Category") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded)
                                    },
                                    modifier = Modifier
                                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                        .fillMaxWidth(),
                                )
                                ExposedDropdownMenu(
                                    expanded = categoryDropdownExpanded,
                                    onDismissRequest = { categoryDropdownExpanded = false },
                                ) {
                                    filterOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option?.uppercase() ?: "ALL") },
                                            onClick = {
                                                viewModel.setCategoryFilter(option)
                                                categoryDropdownExpanded = false
                                            },
                                        )
                                    }
                                }
                            }

                            Button(onClick = onManageCategories) {
                                Text("Edit your categories")
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Sort by", style = MaterialTheme.typography.labelLarge)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SortOption.entries.forEach { option ->
                                        FilterChip(
                                            selected = activeSortOption == option,
                                            onClick = { viewModel.setSortOption(option) },
                                            label = { Text(sortOptionLabel(option)) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = onAddNew,
                            shape = CircleShape,
                            containerColor = lerp(MaterialTheme.colorScheme.primaryContainer, Color.Black, 0.12f),
                        ) {
                            Text(
                                text = "+",
                                fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                            )
                        }
                    },
                ) { paddingValues ->
                    LazyColumn(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .safeContentPadding()
                            .fillMaxSize()
                            .padding(paddingValues),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text("Cheers Checklist", style = MaterialTheme.typography.headlineMedium)
                                    TextButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                        Text("☰ Filters")
                                    }
                                }

                                val keyboardController = LocalSoftwareKeyboardController.current
                                OutlinedTextField(
                                    value = searchText,
                                    onValueChange = {
                                        searchText = it
                                        viewModel.setSearchQuery(it)
                                    },
                                    label = { Text("Search by name or type") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                                    modifier = Modifier.fillMaxWidth(),
                                )

                                if (entries.isEmpty()) {
                                    val filtering = searchText.isNotBlank() || categoryFilter != null
                                    Text(
                                        if (filtering) "No matching entries" else "No entries yet — tap + to add one",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }

                        items(entries) { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onEditEntry(entry) },
                                ) {
                                    Text(
                                        if (entry.brand != null) "${entry.name} — ${entry.brand} (${entry.category})" else "${entry.name} (${entry.category})",
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    Text("${entry.dateTasted} · ${entry.rating}/5", style = MaterialTheme.typography.bodySmall)
                                    if (entry.notes.isNotBlank()) {
                                        Text(entry.notes, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                                TextButton(onClick = { pendingDelete = entry }) {
                                    Text("Delete")
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(88.dp))
                        }
                    }
                }

                val toDelete = pendingDelete
                if (toDelete != null) {
                    AlertDialog(
                        onDismissRequest = { pendingDelete = null },
                        title = { Text("Delete entry?") },
                        text = { Text("Delete \"${toDelete.name}\"? This can't be undone.") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.deleteEntry(toDelete)
                                    pendingDelete = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError,
                                ),
                            ) {
                                Text("Delete")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { pendingDelete = null }) {
                                Text("Cancel")
                            }
                        },
                    )
                }
            }
        }
    }
}

private fun sortOptionLabel(option: SortOption): String = when (option) {
    SortOption.DATE_DESC -> "Newest"
    SortOption.NAME_ASC -> "Name"
    SortOption.RATING_DESC -> "Rating"
}
