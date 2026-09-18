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
    fun saveEntry_insertsIntoRepository() = runTest(dispatcher) {
        val fakeRepository = FakeTastedDrinkRepository()
        val viewModel = TastingViewModel(fakeRepository)

        viewModel.saveEntry(
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

    @Test
    fun saveEntry_whileEditing_updatesInsteadOfAdding() = runTest(dispatcher) {
        val fakeRepository = FakeTastedDrinkRepository()
        val viewModel = TastingViewModel(fakeRepository)

        viewModel.saveEntry(
            name = "Aberlour 12",
            category = DrinkCategory.WHISKY,
            dateTasted = LocalDate(2026, 9, 18),
            rating = 5,
            notes = "Smooth",
        )
        advanceUntilIdle()
        val added = fakeRepository.added.single()

        viewModel.startEditing(added)
        viewModel.saveEntry(
            name = "Aberlour 16",
            category = DrinkCategory.WHISKY,
            dateTasted = LocalDate(2026, 9, 19),
            rating = 4,
            notes = "Richer",
        )
        advanceUntilIdle()

        assertEquals(1, fakeRepository.added.size)
        assertEquals("Aberlour 16", fakeRepository.added.single().name)
    }

    @Test
    fun deleteEntry_removesFromRepository() = runTest(dispatcher) {
        val fakeRepository = FakeTastedDrinkRepository()
        val viewModel = TastingViewModel(fakeRepository)

        viewModel.saveEntry(
            name = "Aberlour 12",
            category = DrinkCategory.WHISKY,
            dateTasted = LocalDate(2026, 9, 18),
            rating = 5,
            notes = "Smooth",
        )
        advanceUntilIdle()
        val added = fakeRepository.added.single()

        viewModel.deleteEntry(added)
        advanceUntilIdle()

        assertEquals(0, fakeRepository.added.size)
    }
}

private class FakeTastedDrinkRepository : TastedDrinkRepository {
    val added = mutableListOf<TastedDrink>()
    private val flow = MutableStateFlow<List<TastedDrink>>(emptyList())
    private var nextId = 1L

    override fun getAll(): Flow<List<TastedDrink>> = flow

    override suspend fun add(drink: TastedDrink) {
        added += drink.copy(id = nextId++)
        flow.value = added.toList()
    }

    override suspend fun update(drink: TastedDrink) {
        val index = added.indexOfFirst { it.id == drink.id }
        if (index >= 0) added[index] = drink
        flow.value = added.toList()
    }

    override suspend fun delete(drink: TastedDrink) {
        added.removeAll { it.id == drink.id }
        flow.value = added.toList()
    }
}
