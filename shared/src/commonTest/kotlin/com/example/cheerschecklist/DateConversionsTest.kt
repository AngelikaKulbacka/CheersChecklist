package com.example.cheerschecklist

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class DateConversionsTest {

    @Test
    fun roundTrip_returnsOriginalDate() {
        val date = LocalDate(2026, 9, 21)

        assertEquals(date, date.toPickerMillis().toLocalDateFromPickerMillis())
    }

    @Test
    fun epochStart_isZeroMillis() {
        assertEquals(0L, LocalDate(1970, 1, 1).toPickerMillis())
    }

    @Test
    fun midDayMillis_mapsToSameDate() {
        val noonUtc = LocalDate(2026, 9, 21).toPickerMillis() + 12 * 60 * 60 * 1000L

        assertEquals(LocalDate(2026, 9, 21), noonUtc.toLocalDateFromPickerMillis())
    }
}
