package com.example.cheerschecklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class TastingViewModel(
    private val repository: TastedDrinkRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val categoryFilter = MutableStateFlow<DrinkCategory?>(null)
    private val sortOption = MutableStateFlow(SortOption.DATE_DESC)

    val activeCategoryFilter: StateFlow<DrinkCategory?> = categoryFilter.asStateFlow()
    val activeSortOption: StateFlow<SortOption> = sortOption.asStateFlow()

    val entries: StateFlow<List<TastedDrink>> =
        combine(repository.getAll(), searchQuery, categoryFilter, sortOption) { all, query, category, sort ->
            all.filtered(query, category).orderedBy(sort)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setCategoryFilter(category: DrinkCategory?) {
        categoryFilter.value = category
    }

    fun setSortOption(option: SortOption) {
        sortOption.value = option
    }

    private val _editingEntry = MutableStateFlow<TastedDrink?>(null)
    val editingEntry: StateFlow<TastedDrink?> = _editingEntry.asStateFlow()

    fun startEditing(drink: TastedDrink) {
        _editingEntry.value = drink
    }

    fun cancelEditing() {
        _editingEntry.value = null
    }

    fun saveEntry(name: String, brand: String?, category: DrinkCategory, dateTasted: LocalDate, rating: Int, notes: String) {
        val editing = _editingEntry.value
        viewModelScope.launch {
            if (editing != null) {
                repository.update(editing.copy(name = name, brand = brand, category = category, dateTasted = dateTasted, rating = rating, notes = notes))
            } else {
                repository.add(TastedDrink(name = name, brand = brand, category = category, dateTasted = dateTasted, rating = rating, notes = notes))
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
