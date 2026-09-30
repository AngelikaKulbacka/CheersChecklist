package com.example.cheerschecklist

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
        val viewModel = TastingViewModel(fakeRepository, FakeCategoryRepository())

        viewModel.saveEntry(
            name = "Aberlour 12",
            brand = "Aberlour",
            category = "WHISKY",
            dateTasted = LocalDate(2026, 9, 18),
            rating = 5,
            notes = "Smooth",
            color = "Amber",
            oiliness = "Medium",
            scent = "Honey",
            flavor = "Vanilla",
        )
        advanceUntilIdle()

        val added = fakeRepository.added.single()
        assertEquals(1, fakeRepository.added.size)
        assertEquals("Aberlour 12", added.name)
        assertEquals("Aberlour", added.brand)
        assertEquals("Amber", added.color)
        assertEquals("Medium", added.oiliness)
        assertEquals("Honey", added.scent)
        assertEquals("Vanilla", added.flavor)
    }

    @Test
    fun saveEntry_whileEditing_updatesInsteadOfAdding() = runTest(dispatcher) {
        val fakeRepository = FakeTastedDrinkRepository()
        val viewModel = TastingViewModel(fakeRepository, FakeCategoryRepository())

        viewModel.saveEntry(
            name = "Aberlour 12",
            brand = null,
            category = "WHISKY",
            dateTasted = LocalDate(2026, 9, 18),
            rating = 5,
            notes = "Smooth",
        )
        advanceUntilIdle()
        val added = fakeRepository.added.single()

        viewModel.startEditing(added)
        viewModel.saveEntry(
            name = "Aberlour 16",
            brand = null,
            category = "WHISKY",
            dateTasted = LocalDate(2026, 9, 19),
            rating = 4,
            notes = "Richer",
        )
        advanceUntilIdle()

        assertEquals(1, fakeRepository.added.size)
        assertEquals("Aberlour 16", fakeRepository.added.single().name)
        assertEquals(LocalDate(2026, 9, 19), fakeRepository.added.single().dateTasted)
    }

    @Test
    fun deleteEntry_removesFromRepository() = runTest(dispatcher) {
        val fakeRepository = FakeTastedDrinkRepository()
        val viewModel = TastingViewModel(fakeRepository, FakeCategoryRepository())

        viewModel.saveEntry(
            name = "Aberlour 12",
            brand = null,
            category = "WHISKY",
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

    @Test
    fun categoryFilterAndSearch_narrowEntries() = runTest(dispatcher) {
        val fakeRepository = FakeTastedDrinkRepository()
        val viewModel = TastingViewModel(fakeRepository, FakeCategoryRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.entries.collect {} }

        viewModel.saveEntry("Aberlour 12", null, "WHISKY", LocalDate(2026, 9, 18), 5, "")
        viewModel.saveEntry("Chardonnay", null, "WINE", LocalDate(2026, 9, 19), 3, "")
        advanceUntilIdle()
        assertEquals(2, viewModel.entries.value.size)

        viewModel.setCategoryFilter("WINE")
        advanceUntilIdle()
        assertEquals(listOf("Chardonnay"), viewModel.entries.value.map { it.name })

        viewModel.setCategoryFilter(null)
        viewModel.setSearchQuery("aber")
        advanceUntilIdle()
        assertEquals(listOf("Aberlour 12"), viewModel.entries.value.map { it.name })
    }

    @Test
    fun setSortOption_reordersEntries() = runTest(dispatcher) {
        val fakeRepository = FakeTastedDrinkRepository()
        val viewModel = TastingViewModel(fakeRepository, FakeCategoryRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.entries.collect {} }

        viewModel.saveEntry("Chardonnay", null, "WINE", LocalDate(2026, 1, 5), 3, "")
        viewModel.saveEntry("Aberlour 12", null, "WHISKY", LocalDate(2026, 9, 20), 5, "")
        viewModel.saveEntry("Guinness", null, "BEER", LocalDate(2026, 3, 15), 1, "")
        advanceUntilIdle()
        assertEquals(listOf("Aberlour 12", "Guinness", "Chardonnay"), viewModel.entries.value.map { it.name })

        viewModel.setSortOption(SortOption.NAME_ASC)
        advanceUntilIdle()
        assertEquals(listOf("Aberlour 12", "Chardonnay", "Guinness"), viewModel.entries.value.map { it.name })

        viewModel.setSortOption(SortOption.RATING_DESC)
        advanceUntilIdle()
        assertEquals(listOf("Aberlour 12", "Chardonnay", "Guinness"), viewModel.entries.value.map { it.name })
    }

    @Test
    fun addCustomCategory_appearsInAvailableCategories() = runTest(dispatcher) {
        val fakeCategoryRepository = FakeCategoryRepository()
        val viewModel = TastingViewModel(FakeTastedDrinkRepository(), fakeCategoryRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.availableCategories.collect {} }

        viewModel.addCustomCategory("Mead")
        advanceUntilIdle()

        assertEquals(
            (BUILT_IN_CATEGORIES + listOf("Mead")).sortedBy { it.uppercase() },
            viewModel.availableCategories.value,
        )
    }

    @Test
    fun addCustomCategory_duplicateOfBuiltInOrExisting_isNoOp() = runTest(dispatcher) {
        val fakeCategoryRepository = FakeCategoryRepository()
        val viewModel = TastingViewModel(FakeTastedDrinkRepository(), fakeCategoryRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.availableCategories.collect {} }

        viewModel.addCustomCategory("whisky")
        advanceUntilIdle()
        assertEquals(BUILT_IN_CATEGORIES.sortedBy { it.uppercase() }, viewModel.availableCategories.value)

        viewModel.addCustomCategory("Mead")
        advanceUntilIdle()
        viewModel.addCustomCategory("mead")
        advanceUntilIdle()
        assertEquals(
            (BUILT_IN_CATEGORIES + listOf("Mead")).sortedBy { it.uppercase() },
            viewModel.availableCategories.value,
        )
    }

    @Test
    fun renameCustomCategory_replacesNameAndUpdatesEntries() = runTest(dispatcher) {
        val fakeRepository = FakeTastedDrinkRepository()
        val fakeCategoryRepository = FakeCategoryRepository()
        val viewModel = TastingViewModel(fakeRepository, fakeCategoryRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.customCategories.collect {} }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.entries.collect {} }

        viewModel.addCustomCategory("Mead")
        advanceUntilIdle()
        viewModel.saveEntry("Honey Mead", null, "Mead", LocalDate(2026, 9, 18), 5, "")
        advanceUntilIdle()

        viewModel.renameCustomCategory("Mead", "Honey Wine")
        advanceUntilIdle()

        assertEquals(listOf("Honey Wine"), viewModel.customCategories.value)
        assertEquals("Honey Wine", fakeRepository.added.single().category)
    }

    @Test
    fun renameCustomCategory_updatesActiveFilterWhenPointingAtOldName() = runTest(dispatcher) {
        val fakeCategoryRepository = FakeCategoryRepository()
        val viewModel = TastingViewModel(FakeTastedDrinkRepository(), fakeCategoryRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.customCategories.collect {} }

        viewModel.addCustomCategory("Mead")
        advanceUntilIdle()
        viewModel.setCategoryFilter("Mead")

        viewModel.renameCustomCategory("Mead", "Honey Wine")
        advanceUntilIdle()

        assertEquals("Honey Wine", viewModel.activeCategoryFilter.value)
    }

    @Test
    fun deleteCustomCategory_reassignsEntriesToBeerAndRemovesFromList() = runTest(dispatcher) {
        val fakeRepository = FakeTastedDrinkRepository()
        val fakeCategoryRepository = FakeCategoryRepository()
        val viewModel = TastingViewModel(fakeRepository, fakeCategoryRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.customCategories.collect {} }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.entries.collect {} }

        viewModel.addCustomCategory("Mead")
        advanceUntilIdle()
        viewModel.saveEntry("Honey Mead", null, "Mead", LocalDate(2026, 9, 18), 5, "")
        advanceUntilIdle()

        viewModel.deleteCustomCategory("Mead")
        advanceUntilIdle()

        assertEquals(emptyList(), viewModel.customCategories.value)
        assertEquals("BEER", fakeRepository.added.single().category)
    }

    @Test
    fun deleteCustomCategory_clearsActiveFilterWhenPointingAtDeletedName() = runTest(dispatcher) {
        val fakeCategoryRepository = FakeCategoryRepository()
        val viewModel = TastingViewModel(FakeTastedDrinkRepository(), fakeCategoryRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.customCategories.collect {} }

        viewModel.addCustomCategory("Mead")
        advanceUntilIdle()
        viewModel.setCategoryFilter("Mead")

        viewModel.deleteCustomCategory("Mead")
        advanceUntilIdle()

        assertEquals(null, viewModel.activeCategoryFilter.value)
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

    override suspend fun renameCategoryInEntries(oldCategory: String, newCategory: String) {
        for (i in added.indices) {
            if (added[i].category == oldCategory) added[i] = added[i].copy(category = newCategory)
        }
        flow.value = added.toList()
    }
}

private class FakeCategoryRepository : CategoryRepository {
    private val flow = MutableStateFlow<List<String>>(emptyList())

    override fun getCustomCategories(): Flow<List<String>> = flow

    override suspend fun addCustomCategory(name: String) {
        if (flow.value.any { it.equals(name, ignoreCase = true) }) return
        flow.value = flow.value + name
    }

    override suspend fun deleteCustomCategory(name: String) {
        flow.value = flow.value.filterNot { it.equals(name, ignoreCase = true) }
    }
}
