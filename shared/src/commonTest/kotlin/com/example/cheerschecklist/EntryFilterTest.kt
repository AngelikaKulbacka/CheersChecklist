package com.example.cheerschecklist

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class EntryFilterTest {

    private fun drink(name: String, category: DrinkCategory, brand: String? = null) = TastedDrink(
        name = name,
        brand = brand,
        category = category,
        dateTasted = LocalDate(2026, 9, 21),
        rating = 3,
    )

    private val entries = listOf(
        drink("Aberlour 12", DrinkCategory.WHISKY, brand = "Pernod Ricard"),
        drink("Lagavulin 16", DrinkCategory.WHISKY),
        drink("Chardonnay", DrinkCategory.WINE),
        drink("Guinness", DrinkCategory.BEER),
    )

    @Test
    fun blankQueryAndNoCategory_returnsEverything() {
        assertEquals(entries, entries.filtered("", null))
    }

    @Test
    fun whitespaceOnlyQuery_isTreatedAsBlank() {
        assertEquals(entries, entries.filtered("   ", null))
    }

    @Test
    fun query_isCaseInsensitiveSubstringMatch() {
        assertEquals(listOf("Aberlour 12"), entries.filtered("aBER", null).map { it.name })
    }

    @Test
    fun category_narrowsToThatCategory() {
        assertEquals(
            listOf("Aberlour 12", "Lagavulin 16"),
            entries.filtered("", DrinkCategory.WHISKY).map { it.name },
        )
    }

    @Test
    fun queryAndCategory_combineWithAnd() {
        assertEquals(
            listOf("Lagavulin 16"),
            entries.filtered("16", DrinkCategory.WHISKY).map { it.name },
        )
        assertEquals(emptyList(), entries.filtered("16", DrinkCategory.WINE))
    }

    @Test
    fun noMatch_returnsEmptyList() {
        assertEquals(emptyList(), entries.filtered("zzz", null))
    }

    @Test
    fun query_matchesBrandEvenWhenNameDoesNotMatch() {
        assertEquals(listOf("Aberlour 12"), entries.filtered("pernod", null).map { it.name })
    }

    @Test
    fun query_doesNotThrowWhenBrandIsNull() {
        assertEquals(emptyList(), entries.filtered("pernod", DrinkCategory.WINE))
    }
}
