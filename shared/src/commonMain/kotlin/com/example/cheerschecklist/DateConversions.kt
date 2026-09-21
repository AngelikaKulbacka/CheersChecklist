package com.example.cheerschecklist

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalTime::class)
fun LocalDate.toPickerMillis(): Long = atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

@OptIn(ExperimentalTime::class)
fun Long.toLocalDateFromPickerMillis(): LocalDate =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date
