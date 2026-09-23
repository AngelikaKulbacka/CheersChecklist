package com.example.cheerschecklist

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class EntrySortTest {

    private fun drink(name: String, date: LocalDate, rating: Int) = TastedDrink(
        name = name,
        category = DrinkCategory.WHISKY,
        dateTasted = date,
        rating = rating,
    )

    private val entries = listOf(
        drink("Chardonnay", LocalDate(2026, 1, 5), rating = 3),
        drink("aberlour 12", LocalDate(2026, 9, 20), rating = 5),
        drink("Guinness", LocalDate(2026, 3, 15), rating = 1),
    )

    @Test
    fun dateDesc_ordersNewestFirst() {
        assertEquals(
            listOf("aberlour 12", "Guinness", "Chardonnay"),
            entries.orderedBy(SortOption.DATE_DESC).map { it.name },
        )
    }

    @Test
    fun nameAsc_ordersAlphabeticallyIgnoringCase() {
        assertEquals(
            listOf("aberlour 12", "Chardonnay", "Guinness"),
            entries.orderedBy(SortOption.NAME_ASC).map { it.name },
        )
    }

    @Test
    fun ratingDesc_ordersHighestFirst() {
        assertEquals(
            listOf("aberlour 12", "Chardonnay", "Guinness"),
            entries.orderedBy(SortOption.RATING_DESC).map { it.name },
        )
    }
}
