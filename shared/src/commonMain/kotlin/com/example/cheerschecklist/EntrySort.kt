package com.example.cheerschecklist

enum class SortOption { DATE_DESC, NAME_ASC, RATING_DESC }

fun List<TastedDrink>.orderedBy(option: SortOption): List<TastedDrink> = when (option) {
    SortOption.DATE_DESC -> sortedByDescending { it.dateTasted }
    SortOption.NAME_ASC -> sortedBy { it.name.lowercase() }
    SortOption.RATING_DESC -> sortedByDescending { it.rating }
}
