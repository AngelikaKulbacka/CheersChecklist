package com.example.cheerschecklist

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

val BUILT_IN_CATEGORIES = listOf("WHISKY", "WINE", "BEER", "COCKTAIL", "OTHER")

@Entity
data class CustomCategory(
    @PrimaryKey @ColumnInfo(collate = ColumnInfo.NOCASE) val name: String,
)
