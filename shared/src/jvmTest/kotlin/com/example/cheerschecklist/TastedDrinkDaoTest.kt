package com.example.cheerschecklist

import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TastedDrinkDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: TastedDrinkDao

    @BeforeTest
    fun setUp() {
        database = getRoomDatabase(Room.inMemoryDatabaseBuilder<AppDatabase>())
        dao = database.tastedDrinkDao()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    private fun drink(name: String, date: LocalDate = LocalDate(2026, 9, 21), brand: String? = null) = TastedDrink(
        name = name,
        brand = brand,
        category = DrinkCategory.WHISKY,
        dateTasted = date,
        rating = 4,
        notes = "notes for $name",
    )

    @Test
    fun insert_assignsIdAndRoundTripsEveryField() = runBlocking {
        dao.insert(
            TastedDrink(
                name = "Chardonnay",
                brand = "Concha y Toro",
                category = DrinkCategory.WINE,
                dateTasted = LocalDate(2026, 3, 15),
                rating = 5,
                notes = "Buttery",
            ),
        )

        val stored = dao.getAll().first().single()

        assertNotEquals(0L, stored.id)
        assertEquals("Chardonnay", stored.name)
        assertEquals("Concha y Toro", stored.brand)
        assertEquals(DrinkCategory.WINE, stored.category)
        assertEquals(LocalDate(2026, 3, 15), stored.dateTasted)
        assertEquals(5, stored.rating)
        assertEquals("Buttery", stored.notes)
    }

    @Test
    fun insert_withoutBrand_staysNull() = runBlocking {
        dao.insert(drink("Guinness"))

        assertEquals(null, dao.getAll().first().single().brand)
    }

    @Test
    fun getAll_ordersByDateTastedNewestFirst() = runBlocking {
        dao.insert(drink("January", LocalDate(2026, 1, 5)))
        dao.insert(drink("September", LocalDate(2026, 9, 20)))
        dao.insert(drink("March", LocalDate(2026, 3, 15)))

        assertEquals(listOf("September", "March", "January"), dao.getAll().first().map { it.name })
    }

    @Test
    fun update_changesTheRowInsteadOfAddingOne() = runBlocking {
        dao.insert(drink("Aberlour 12"))
        val stored = dao.getAll().first().single()

        dao.update(stored.copy(name = "Aberlour 16", rating = 2))

        val after = dao.getAll().first()
        assertEquals(1, after.size)
        assertEquals(stored.id, after.single().id)
        assertEquals("Aberlour 16", after.single().name)
        assertEquals(2, after.single().rating)
    }

    @Test
    fun delete_removesOnlyThatRow() = runBlocking {
        dao.insert(drink("Keep", LocalDate(2026, 9, 21)))
        dao.insert(drink("Remove", LocalDate(2026, 9, 20)))
        val toRemove = dao.getAll().first().single { it.name == "Remove" }

        dao.delete(toRemove)

        assertEquals(listOf("Keep"), dao.getAll().first().map { it.name })
    }

    @Test
    fun getAll_emitsAgainAfterInsert() = runBlocking {
        val emissions = Channel<List<TastedDrink>>(Channel.UNLIMITED)
        val collector = launch(Dispatchers.Default) {
            dao.getAll().collect { emissions.send(it) }
        }

        withTimeout(5_000) {
            assertEquals(0, emissions.receive().size)

            dao.insert(drink("Late arrival"))

            assertEquals(listOf("Late arrival"), emissions.receive().map { it.name })
        }
        collector.cancel()
    }

    @Test
    fun repository_addAndGetAllGoThroughRealRoom() = runBlocking {
        val repository = RoomTastedDrinkRepository(dao)

        repository.add(drink("Via repository"))

        assertEquals(listOf("Via repository"), repository.getAll().first().map { it.name })
    }
}
