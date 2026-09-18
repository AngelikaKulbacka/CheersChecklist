package com.example.cheerschecklist

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TastedDrinkDao {
    @Insert
    suspend fun insert(drink: TastedDrink)

    @Query("SELECT * FROM TastedDrink ORDER BY dateTasted DESC")
    fun getAll(): Flow<List<TastedDrink>>
}
