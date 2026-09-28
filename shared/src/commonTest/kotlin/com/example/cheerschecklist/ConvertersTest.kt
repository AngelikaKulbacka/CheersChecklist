package com.example.cheerschecklist

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun fromLocalDate_producesIsoFormattedString() {
        assertEquals("2026-09-28", converters.fromLocalDate(LocalDate(2026, 9, 28)))
    }

    @Test
    fun fromLocalDate_padsSingleDigitMonthAndDay() {
        assertEquals("2026-01-05", converters.fromLocalDate(LocalDate(2026, 1, 5)))
    }

    @Test
    fun toLocalDate_parsesIsoFormattedString() {
        assertEquals(LocalDate(2026, 9, 28), converters.toLocalDate("2026-09-28"))
    }

    @Test
    fun roundTrip_returnsOriginalDate() {
        val date = LocalDate(2026, 12, 31)

        assertEquals(date, converters.toLocalDate(converters.fromLocalDate(date)))
    }

    @Test
    fun toLocalDate_invalidFormat_throws() {
        assertFailsWith<IllegalArgumentException> {
            converters.toLocalDate("not-a-date")
        }
    }
}
