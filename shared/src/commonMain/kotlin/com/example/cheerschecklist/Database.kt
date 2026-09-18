package com.example.cheerschecklist

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>

fun getRoomDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()

object DatabaseProvider {
    private var instance: AppDatabase? = null
    fun getDatabase(): AppDatabase =
        instance ?: getRoomDatabase(getDatabaseBuilder()).also { instance = it }
}
