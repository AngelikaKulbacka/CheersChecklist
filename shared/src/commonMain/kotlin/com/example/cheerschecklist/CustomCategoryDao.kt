package com.example.cheerschecklist

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomCategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CustomCategory)

    @Delete
    suspend fun delete(category: CustomCategory)

    @Query("SELECT name FROM CustomCategory ORDER BY name ASC")
    fun getAll(): Flow<List<String>>
}
