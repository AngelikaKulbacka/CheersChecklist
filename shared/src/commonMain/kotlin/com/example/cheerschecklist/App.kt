package com.example.cheerschecklist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        val viewModel: TastingViewModel = viewModel { TastingViewModel() }
        val entries by viewModel.entries.collectAsState()
        val editingEntry by viewModel.editingEntry.collectAsState()
        val categoryFilter by viewModel.activeCategoryFilter.collectAsState()
        var searchText by remember { mutableStateOf("") }

        val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

        var name by remember { mutableStateOf("") }
        var brand by remember { mutableStateOf("") }
        var category by remember { mutableStateOf(DrinkCategory.WHISKY) }
        var rating by remember { mutableStateOf(0) }
        var notes by remember { mutableStateOf("") }
        var dateTasted by remember { mutableStateOf(today) }
        var showDatePicker by remember { mutableStateOf(false) }
        var pendingDelete by remember { mutableStateOf<TastedDrink?>(null) }

        LaunchedEffect(editingEntry) {
            val entry = editingEntry
            if (entry != null) {
                name = entry.name
                brand = entry.brand ?: ""
                category = entry.category
                rating = entry.rating
                notes = entry.notes
                dateTasted = entry.dateTasted
            } else {
                name = ""
                brand = ""
                category = DrinkCategory.WHISKY
                rating = 0
                notes = ""
                dateTasted = today
            }
        }

        LazyColumn(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
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
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Category", style = MaterialTheme.typography.labelLarge)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            DrinkCategory.entries.chunked(3).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    row.forEach { entry ->
                                        FilterChip(
                                            selected = category == entry,
                                            onClick = { category = entry },
                                            label = { Text(entry.name) },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Rating", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            (1..5).forEach { star ->
                                FilterChip(
                                    selected = rating >= star,
                                    onClick = { rating = star },
                                    label = { Text(star.toString()) },
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Date: $dateTasted")
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (editingEntry != null) {
                            TextButton(onClick = { viewModel.cancelEditing() }) {
                                Text("Cancel")
                            }
                        }
                        Button(
                            onClick = {
                                viewModel.saveEntry(
                                    name = name,
                                    brand = brand.trim().ifBlank { null },
                                    category = category,
                                    dateTasted = dateTasted,
                                    rating = rating,
                                    notes = notes,
                                )
                                name = ""
                                brand = ""
                                rating = 0
                                notes = ""
                                dateTasted = today
                            },
                            enabled = name.isNotBlank() && rating > 0,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(if (editingEntry != null) "Save" else "Add")
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = {
                            searchText = it
                            viewModel.setSearchQuery(it)
                        },
                        label = { Text("Search by name") },
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

                    if (entries.isEmpty()) {
                        val filtering = searchText.isNotBlank() || categoryFilter != null
                        Text(
                            if (filtering) "No matching entries" else "No entries yet — add your first one above",
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
                            .clickable { viewModel.startEditing(entry) },
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

        if (showDatePicker) {
            val pickerState = rememberDatePickerState(initialSelectedDateMillis = dateTasted.toPickerMillis())
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        pickerState.selectedDateMillis?.let { dateTasted = it.toLocalDateFromPickerMillis() }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                },
            ) {
                DatePicker(state = pickerState)
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
}
