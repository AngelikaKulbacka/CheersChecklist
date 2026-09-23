package com.example.cheerschecklist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun ListScreen(
    viewModel: TastingViewModel,
    onAddNew: () -> Unit,
    onEditEntry: (TastedDrink) -> Unit,
) {
    val entries by viewModel.entries.collectAsState()
    val categoryFilter by viewModel.activeCategoryFilter.collectAsState()
    val activeSortOption by viewModel.activeSortOption.collectAsState()
    var searchText by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<TastedDrink?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNew) {
                Text("+")
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
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Cheers Checklist", style = MaterialTheme.typography.headlineMedium)
                    Text("Your tasting log", style = MaterialTheme.typography.bodyMedium)

                    OutlinedTextField(
                        value = searchText,
                        onValueChange = {
                            searchText = it
                            viewModel.setSearchQuery(it)
                        },
                        label = { Text("Search by name or brand") },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    val filterOptions: List<DrinkCategory?> = listOf(null) + DrinkCategory.entries
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        filterOptions.chunked(3).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { option ->
                                    FilterChip(
                                        selected = categoryFilter == option,
                                        onClick = { viewModel.setCategoryFilter(option) },
                                        label = { Text(option?.name ?: "ALL") },
                                    )
                                }
                            }
                        }
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
                            if (entry.brand != null) "${entry.name} — ${entry.brand} (${entry.category.name})" else "${entry.name} (${entry.category.name})",
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
        }
    }

    val toDelete = pendingDelete
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete entry?") },
            text = { Text("Delete \"${toDelete.name}\"? This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEntry(toDelete)
                    pendingDelete = null
                }) {
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

private fun sortOptionLabel(option: SortOption): String = when (option) {
    SortOption.DATE_DESC -> "Newest"
    SortOption.NAME_ASC -> "Name"
    SortOption.RATING_DESC -> "Rating"
}
