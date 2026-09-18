package com.example.cheerschecklist

import kotlinx.coroutines.flow.Flow

interface TastedDrinkRepository {
    fun getAll(): Flow<List<TastedDrink>>
    suspend fun add(drink: TastedDrink)
}

class RoomTastedDrinkRepository(
    private val dao: TastedDrinkDao,
) : TastedDrinkRepository {
    override fun getAll(): Flow<List<TastedDrink>> = dao.getAll()
    override suspend fun add(drink: TastedDrink) = dao.insert(drink)
}
