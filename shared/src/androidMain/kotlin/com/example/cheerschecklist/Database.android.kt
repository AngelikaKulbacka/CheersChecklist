package com.example.cheerschecklist

import androidx.room.Room
import androidx.room.RoomDatabase

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val appContext = AppContext.instance
    val dbFile = appContext.getDatabasePath("cheerschecklist.db")
    return Room.databaseBuilder<AppDatabase>(context = appContext, name = dbFile.absolutePath)
}
