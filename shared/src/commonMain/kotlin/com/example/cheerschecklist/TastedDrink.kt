package com.example.cheerschecklist

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

enum class DrinkCategory { WHISKY, WINE, BEER, COCKTAIL, OTHER }

@Entity
data class TastedDrink(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: DrinkCategory,
    val dateTasted: LocalDate,
    val rating: Int,
    val notes: String = "",
)
