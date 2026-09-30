package com.example.cheerschecklist

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity
data class TastedDrink(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String? = null,
    val category: String,
    val color: String? = null,
    val oiliness: String? = null,
    val scent: String? = null,
    val flavor: String? = null,
    val dateTasted: LocalDate,
    val rating: Int,
    val notes: String = "",
    val photoPath: String? = null,
)
