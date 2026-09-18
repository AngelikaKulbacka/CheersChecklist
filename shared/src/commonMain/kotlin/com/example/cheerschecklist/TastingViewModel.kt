package com.example.cheerschecklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class TastingViewModel(
    private val repository: TastedDrinkRepository = Repositories.tastedDrinkRepository,
) : ViewModel() {

    val entries: StateFlow<List<TastedDrink>> =
        repository.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addEntry(name: String, category: DrinkCategory, dateTasted: LocalDate, rating: Int, notes: String) {
        viewModelScope.launch {
            repository.add(TastedDrink(name = name, category = category, dateTasted = dateTasted, rating = rating, notes = notes))
        }
    }
}
