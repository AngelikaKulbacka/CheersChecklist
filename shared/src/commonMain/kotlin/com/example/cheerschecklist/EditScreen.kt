package com.example.cheerschecklist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    val language by viewModel.activeLanguage.collectAsState()
    val strings = stringsFor(language)

    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(availableCategories.firstOrNull() ?: "") }
    var newCategoryText by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var oiliness by remember { mutableStateOf("") }
    var scent by remember { mutableStateOf("") }
    var flavor by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0) }
    var notes by remember { mutableStateOf("") }
    var dateTasted by remember { mutableStateOf(today) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCancelConfirm by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var ratingError by remember { mutableStateOf(false) }
    var newCategoryError by remember { mutableStateOf(false) }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var originalPhotoPath by remember { mutableStateOf<String?>(null) }
    var originalName by remember { mutableStateOf("") }
    var originalBrand by remember { mutableStateOf("") }
    var originalCategory by remember { mutableStateOf("") }
    var originalColor by remember { mutableStateOf("") }
    var originalOiliness by remember { mutableStateOf("") }
    var originalScent by remember { mutableStateOf("") }
    var originalFlavor by remember { mutableStateOf("") }
    var originalRating by remember { mutableStateOf(0) }
    var originalNotes by remember { mutableStateOf("") }
    var originalDateTasted by remember { mutableStateOf(today) }
    var photoMenuExpanded by remember { mutableStateOf(false) }
    var showFullScreenPhoto by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showRemovePhotoConfirm by remember { mutableStateOf(false) }
    val launchPhotoPicker = rememberPhotoPicker { newPath ->
        if (photoPath != originalPhotoPath) photoPath?.let { deletePhotoFile(it) }
        photoPath = newPath
    }
    val photoBitmap = remember(photoPath) { photoPath?.let { decodeImageBitmap(it) } }

    val hasUnsavedChanges = name != originalName ||
        brand != originalBrand ||
        category != originalCategory ||
        color != originalColor ||
        oiliness != originalOiliness ||
        scent != originalScent ||
        flavor != originalFlavor ||
        rating != originalRating ||
        notes != originalNotes ||
        dateTasted != originalDateTasted ||
        photoPath != originalPhotoPath

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
            photoPath = entry.photoPath
            originalPhotoPath = entry.photoPath
            originalName = entry.name
            originalBrand = entry.brand ?: ""
            originalCategory = entry.category
            originalColor = entry.color ?: ""
            originalOiliness = entry.oiliness ?: ""
            originalScent = entry.scent ?: ""
            originalFlavor = entry.flavor ?: ""
            originalRating = entry.rating
            originalNotes = entry.notes
            originalDateTasted = entry.dateTasted
            nameError = false
            ratingError = false
            newCategoryError = false
            showRemovePhotoConfirm = false
            isSaving = false
        } else {
            name = ""
            brand = ""
            category = availableCategories.firstOrNull() ?: ""
            color = ""
            oiliness = ""
            scent = ""
            flavor = ""
            rating = 0
            notes = ""
            dateTasted = today
            photoPath = null
            originalPhotoPath = null
            originalName = ""
            originalBrand = ""
            originalCategory = category
            originalColor = ""
            originalOiliness = ""
            originalScent = ""
            originalFlavor = ""
            originalRating = 0
            originalNotes = ""
            originalDateTasted = today
            nameError = false
            ratingError = false
            newCategoryError = false
            showRemovePhotoConfirm = false
            isSaving = false
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
                    if (editingEntry != null) strings.editEntryTitle else strings.newEntryTitle,
                    style = MaterialTheme.typography.headlineMedium,
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text(strings.nameLabel) },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text(strings.cantBeEmpty) }
                    } else {
                        null
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text(strings.brandLabel) },
                    modifier = Modifier.fillMaxWidth(),
                )

                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = it },
                ) {
                    OutlinedTextField(
                        value = category.uppercase(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings.category) },
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
                        availableCategories.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.uppercase()) },
                                onClick = {
                                    category = option
                                    categoryDropdownExpanded = false
                                },
                            )
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
                        onValueChange = {
                            newCategoryText = it
                            if (it.isNotBlank()) newCategoryError = false
                        },
                        label = { Text(strings.addYourOwnType) },
                        isError = newCategoryError,
                        supportingText = if (newCategoryError) {
                            { Text(strings.cantBeEmpty) }
                        } else {
                            null
                        },
                        modifier = Modifier.weight(1f),
                    )
                    Button(
                        onClick = {
                            val trimmed = newCategoryText.trim()
                            if (trimmed.isBlank()) {
                                newCategoryError = true
                            } else {
                                viewModel.addCustomCategory(trimmed)
                                category = trimmed
                                newCategoryText = ""
                            }
                        },
                    ) {
                        Text(strings.add)
                    }
                }

                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text(strings.colorLabel) },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = oiliness,
                    onValueChange = { oiliness = it },
                    label = { Text(strings.oilinessLabel) },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = scent,
                    onValueChange = { scent = it },
                    label = { Text(strings.scentLabel) },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = flavor,
                    onValueChange = { flavor = it },
                    label = { Text(strings.flavorLabel) },
                    modifier = Modifier.fillMaxWidth(),
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        strings.ratingLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (ratingError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..5).forEach { star ->
                            FilterChip(
                                selected = rating == star,
                                onClick = {
                                    rating = star
                                    ratingError = false
                                },
                                label = { Text(star.toString()) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                                border = if (ratingError) {
                                    FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = rating == star,
                                        borderColor = MaterialTheme.colorScheme.error,
                                        selectedBorderColor = MaterialTheme.colorScheme.error,
                                        borderWidth = 1.5.dp,
                                    )
                                } else {
                                    FilterChipDefaults.filterChipBorder(enabled = true, selected = rating == star)
                                },
                            )
                        }
                    }
                    if (ratingError) {
                        Text(
                            strings.cantBeEmpty,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(strings.dateLabel(dateTasted.toString()))
                }

                if (photoPath == null) {
                    Row {
                        OutlinedButton(onClick = { photoMenuExpanded = true }) {
                            Text(strings.addPhoto)
                        }
                        DropdownMenu(
                            expanded = photoMenuExpanded,
                            onDismissRequest = { photoMenuExpanded = false },
                        ) {
                            if (supportsCameraCapture) {
                                DropdownMenuItem(
                                    text = { Text(strings.takePhoto) },
                                    onClick = {
                                        photoMenuExpanded = false
                                        launchPhotoPicker(PhotoSource.CAMERA)
                                    },
                                )
                            }
                            DropdownMenuItem(
                                text = { Text(strings.chooseFromGallery) },
                                onClick = {
                                    photoMenuExpanded = false
                                    launchPhotoPicker(PhotoSource.GALLERY)
                                },
                            )
                        }
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (photoBitmap != null) {
                            Image(
                                bitmap = photoBitmap,
                                contentDescription = null,
                                modifier = Modifier.size(96.dp).clickable { showFullScreenPhoto = true },
                            )
                        }
                        Button(onClick = { showRemovePhotoConfirm = true }) {
                            Text(strings.removePhoto)
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(strings.notesLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextButton(onClick = {
                        if (hasUnsavedChanges) {
                            showCancelConfirm = true
                        } else {
                            viewModel.cancelEditing()
                            onDone()
                        }
                    }) {
                        Text(strings.cancel)
                    }
                    Button(
                        onClick = {
                            if (isSaving) return@Button
                            nameError = name.isBlank()
                            ratingError = rating <= 0
                            if (!nameError && !ratingError) {
                                isSaving = true
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
                                    photoPath = photoPath,
                                )
                                val oldPhotoPath = originalPhotoPath
                                if (oldPhotoPath != null && oldPhotoPath != photoPath) {
                                    deletePhotoFile(oldPhotoPath)
                                }
                                onDone()
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(if (editingEntry != null) strings.save else strings.add)
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
                    Text(strings.ok)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(strings.cancel)
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showCancelConfirm) {
        AlertDialog(
            onDismissRequest = { showCancelConfirm = false },
            title = { Text(strings.discardChangesTitle) },
            text = { Text(strings.discardChangesText) },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelConfirm = false
                        val unsavedPhotoPath = photoPath
                        if (unsavedPhotoPath != null && unsavedPhotoPath != originalPhotoPath) {
                            deletePhotoFile(unsavedPhotoPath)
                        }
                        viewModel.cancelEditing()
                        onDone()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text(strings.discard)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirm = false }) {
                    Text(strings.keepEditing)
                }
            },
        )
    }

    if (showRemovePhotoConfirm) {
        AlertDialog(
            onDismissRequest = { showRemovePhotoConfirm = false },
            title = { Text(strings.removePhotoTitle) },
            text = { Text(strings.removePhotoText) },
            confirmButton = {
                Button(
                    onClick = {
                        if (photoPath != originalPhotoPath) photoPath?.let { deletePhotoFile(it) }
                        photoPath = null
                        showRemovePhotoConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text(strings.removePhoto)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemovePhotoConfirm = false }) {
                    Text(strings.cancel)
                }
            },
        )
    }

    if (showFullScreenPhoto && photoBitmap != null) {
        Dialog(
            onDismissRequest = { showFullScreenPhoto = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { showFullScreenPhoto = false },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    bitmap = photoBitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
