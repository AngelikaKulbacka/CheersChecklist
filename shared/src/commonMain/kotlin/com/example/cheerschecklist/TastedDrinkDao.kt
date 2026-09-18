package com.example.cheerschecklist

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TastedDrinkDao {
    @Insert
    suspend fun insert(drink: TastedDrink)

    @Update
    suspend fun update(drink: TastedDrink)

    @Delete
    suspend fun delete(drink: TastedDrink)

    @Query("SELECT * FROM TastedDrink ORDER BY dateTasted DESC")
    fun getAll(): Flow<List<TastedDrink>>
}
