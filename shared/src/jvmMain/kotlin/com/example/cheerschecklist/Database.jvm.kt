package com.example.cheerschecklist

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dir = File(System.getProperty("user.home"), ".cheerschecklist").apply { mkdirs() }
    val dbFile = File(dir, "cheerschecklist.db")
    return Room.databaseBuilder<AppDatabase>(name = dbFile.absolutePath)
}
