package com.example.cheerschecklist

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TastingViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() { Dispatchers.setMain(dispatcher) }

    @AfterTest
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun addEntry_insertsIntoRepository() = runTest(dispatcher) {
        val fakeRepository = FakeTastedDrinkRepository()
        val viewModel = TastingViewModel(fakeRepository)

        viewModel.addEntry(
            name = "Aberlour 12",
            category = DrinkCategory.WHISKY,
            dateTasted = LocalDate(2026, 9, 18),
            rating = 5,
            notes = "Smooth",
        )
        advanceUntilIdle()

        assertEquals(1, fakeRepository.added.size)
        assertEquals("Aberlour 12", fakeRepository.added.single().name)
    }
}

private class FakeTastedDrinkRepository : TastedDrinkRepository {
    val added = mutableListOf<TastedDrink>()
    private val flow = MutableStateFlow<List<TastedDrink>>(emptyList())

    override fun getAll(): Flow<List<TastedDrink>> = flow

    override suspend fun add(drink: TastedDrink) {
        added += drink
        flow.value = added.toList()
    }
}
