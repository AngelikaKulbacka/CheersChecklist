package com.example.cheerschecklist

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE TastedDrink ADD COLUMN brand TEXT")
    }
}

val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE TastedDrink ADD COLUMN color TEXT")
        connection.execSQL("ALTER TABLE TastedDrink ADD COLUMN oiliness TEXT")
        connection.execSQL("ALTER TABLE TastedDrink ADD COLUMN scent TEXT")
        connection.execSQL("ALTER TABLE TastedDrink ADD COLUMN flavor TEXT")
    }
}

val MIGRATION_3_4: Migration = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `CustomCategory` (`name` TEXT NOT NULL COLLATE NOCASE, PRIMARY KEY(`name`))",
        )
    }
}
