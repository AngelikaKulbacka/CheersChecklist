package com.example.cheerschecklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class TastingViewModel(
    private val repository: TastedDrinkRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val categoryFilter = MutableStateFlow<String?>(null)
    private val sortOption = MutableStateFlow(SortOption.DATE_DESC)
    private val language = MutableStateFlow(AppLanguage.ENGLISH)

    val activeCategoryFilter: StateFlow<String?> = categoryFilter.asStateFlow()
    val activeSortOption: StateFlow<SortOption> = sortOption.asStateFlow()
    val activeLanguage: StateFlow<AppLanguage> = language.asStateFlow()

    fun toggleLanguage() {
        language.value = if (language.value == AppLanguage.ENGLISH) AppLanguage.POLISH else AppLanguage.ENGLISH
    }

    val customCategories: StateFlow<List<String>> =
        categoryRepository.getCustomCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableCategories: StateFlow<List<String>> =
        categoryRepository.getCustomCategories()
            .map { custom -> (BUILT_IN_CATEGORIES + custom).sortedBy { it.uppercase() } }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                BUILT_IN_CATEGORIES.sortedBy { it.uppercase() },
            )

    val entries: StateFlow<List<TastedDrink>> =
        combine(repository.getAll(), searchQuery, categoryFilter, sortOption) { all, query, category, sort ->
            all.filtered(query, category).orderedBy(sort)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        categoryFilter.value = category
    }

    fun setSortOption(option: SortOption) {
        sortOption.value = option
    }

    fun addCustomCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        if (availableCategories.value.any { it.equals(trimmed, ignoreCase = true) }) return
        viewModelScope.launch { categoryRepository.addCustomCategory(trimmed) }
    }

    fun renameCustomCategory(oldName: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank() || trimmed.equals(oldName, ignoreCase = true)) return
        if (availableCategories.value.any { it.equals(trimmed, ignoreCase = true) }) return
        viewModelScope.launch {
            categoryRepository.addCustomCategory(trimmed)
            repository.renameCategoryInEntries(oldName, trimmed)
            categoryRepository.deleteCustomCategory(oldName)
            if (categoryFilter.value == oldName) categoryFilter.value = trimmed
        }
    }

    fun deleteCustomCategory(name: String) {
        viewModelScope.launch {
            repository.renameCategoryInEntries(name, "BEER")
            categoryRepository.deleteCustomCategory(name)
            if (categoryFilter.value == name) categoryFilter.value = null
        }
    }

    private val _editingEntry = MutableStateFlow<TastedDrink?>(null)
    val editingEntry: StateFlow<TastedDrink?> = _editingEntry.asStateFlow()

    fun startEditing(drink: TastedDrink) {
        _editingEntry.value = drink
    }

    fun cancelEditing() {
        _editingEntry.value = null
    }

    fun saveEntry(
        name: String,
        brand: String?,
        category: String,
        dateTasted: LocalDate,
        rating: Int,
        notes: String,
        color: String? = null,
        oiliness: String? = null,
        scent: String? = null,
        flavor: String? = null,
    ) {
        val editing = _editingEntry.value
        viewModelScope.launch {
            if (editing != null) {
                repository.update(
                    editing.copy(
                        name = name, brand = brand, category = category,
                        color = color, oiliness = oiliness, scent = scent, flavor = flavor,
                        dateTasted = dateTasted, rating = rating, notes = notes,
                    ),
                )
            } else {
                repository.add(
                    TastedDrink(
                        name = name, brand = brand, category = category,
                        color = color, oiliness = oiliness, scent = scent, flavor = flavor,
                        dateTasted = dateTasted, rating = rating, notes = notes,
                    ),
                )
            }
            _editingEntry.value = null
        }
    }

    fun deleteEntry(drink: TastedDrink) {
        viewModelScope.launch {
            repository.delete(drink)
            if (_editingEntry.value?.id == drink.id) _editingEntry.value = null
        }
    }
}
