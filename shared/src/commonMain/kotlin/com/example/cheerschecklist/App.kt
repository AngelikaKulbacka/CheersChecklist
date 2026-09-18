package com.example.cheerschecklist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@OptIn(ExperimentalTime::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        val viewModel: TastingViewModel = viewModel { TastingViewModel() }
        val entries by viewModel.entries.collectAsState()

        var name by remember { mutableStateOf("") }
        var category by remember { mutableStateOf(DrinkCategory.WHISKY) }
        var rating by remember { mutableStateOf(0) }
        var notes by remember { mutableStateOf("") }

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

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                    )

                    Button(
                        onClick = {
                            viewModel.addEntry(
                                name = name,
                                category = category,
                                dateTasted = Clock.System.todayIn(TimeZone.currentSystemDefault()),
                                rating = rating,
                                notes = notes,
                            )
                            name = ""
                            rating = 0
                            notes = ""
                        },
                        enabled = name.isNotBlank() && rating > 0,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Add")
                    }
                }
            }

            items(entries) { entry ->
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("${entry.name} (${entry.category.name})", style = MaterialTheme.typography.titleMedium)
                    Text("${entry.dateTasted} · ${entry.rating}/5", style = MaterialTheme.typography.bodySmall)
                    if (entry.notes.isNotBlank()) {
                        Text(entry.notes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
