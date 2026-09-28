package com.example.cheerschecklist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(viewModel: TastingViewModel, onDone: () -> Unit) {
    val editingEntry by viewModel.editingEntry.collectAsState()
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    val availableCategories by viewModel.availableCategories.collectAsState()

    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("WHISKY") }
    var newCategoryText by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var oiliness by remember { mutableStateOf("") }
    var scent by remember { mutableStateOf("") }
    var flavor by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0) }
    var notes by remember { mutableStateOf("") }
    var dateTasted by remember { mutableStateOf(today) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(editingEntry) {
        val entry = editingEntry
        if (entry != null) {
            name = entry.name
            brand = entry.brand ?: ""
            category = entry.category
            color = entry.color ?: ""
            oiliness = entry.oiliness ?: ""
            scent = entry.scent ?: ""
            flavor = entry.flavor ?: ""
            rating = entry.rating
            notes = entry.notes
            dateTasted = entry.dateTasted
        } else {
            name = ""
            brand = ""
            category = "WHISKY"
            color = ""
            oiliness = ""
            scent = ""
            flavor = ""
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
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    if (editingEntry != null) "Edit entry" else "New entry",
                    style = MaterialTheme.typography.headlineMedium,
                )

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
                        availableCategories.chunked(3).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { entry ->
                                    FilterChip(
                                        selected = category == entry,
                                        onClick = { category = entry },
                                        label = { Text(entry.uppercase()) },
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = newCategoryText,
                        onValueChange = { newCategoryText = it },
                        label = { Text("Add your own type") },
                        modifier = Modifier.weight(1f),
                    )
                    Button(
                        onClick = {
                            val trimmed = newCategoryText.trim()
                            viewModel.addCustomCategory(trimmed)
                            category = trimmed
                            newCategoryText = ""
                        },
                        enabled = newCategoryText.isNotBlank(),
                    ) {
                        Text("Add")
                    }
                }

                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Color (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = oiliness,
                    onValueChange = { oiliness = it },
                    label = { Text("Oiliness (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = scent,
                    onValueChange = { scent = it },
                    label = { Text("Scent (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = flavor,
                    onValueChange = { flavor = it },
                    label = { Text("Flavor (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )

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
                    TextButton(onClick = {
                        viewModel.cancelEditing()
                        onDone()
                    }) {
                        Text("Cancel")
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
                                color = color.trim().ifBlank { null },
                                oiliness = oiliness.trim().ifBlank { null },
                                scent = scent.trim().ifBlank { null },
                                flavor = flavor.trim().ifBlank { null },
                            )
                            onDone()
                        },
                        enabled = name.isNotBlank() && rating > 0,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(if (editingEntry != null) "Save" else "Add")
                    }
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
}
