package com.example.cheerschecklist

fun List<TastedDrink>.filtered(query: String, category: DrinkCategory?): List<TastedDrink> {
    val trimmed = query.trim()
    return filter { drink ->
        (category == null || drink.category == category) &&
            (
                trimmed.isEmpty() ||
                    drink.name.contains(trimmed, ignoreCase = true) ||
                    drink.brand?.contains(trimmed, ignoreCase = true) == true
                )
    }
}
