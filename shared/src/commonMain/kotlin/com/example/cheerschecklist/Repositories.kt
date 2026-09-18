package com.example.cheerschecklist

object Repositories {
    val tastedDrinkRepository: TastedDrinkRepository by lazy {
        RoomTastedDrinkRepository(DatabaseProvider.getDatabase().tastedDrinkDao())
    }
}
