package com.example.cheerschecklist

import androidx.room.Room
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CustomCategoryDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: CustomCategoryDao

    @BeforeTest
    fun setUp() {
        database = getRoomDatabase(Room.inMemoryDatabaseBuilder<AppDatabase>())
        dao = database.customCategoryDao()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun insert_makesCategoryAppearInGetAll() = runBlocking {
        dao.insert(CustomCategory("Mead"))

        assertEquals(listOf("Mead"), dao.getAll().first())
    }

    @Test
    fun insert_caseDifferentDuplicate_isIgnored() = runBlocking {
        dao.insert(CustomCategory("Mead"))
        dao.insert(CustomCategory("mead"))

        assertEquals(listOf("Mead"), dao.getAll().first())
    }

    @Test
    fun getAll_ordersAlphabetically() = runBlocking {
        dao.insert(CustomCategory("Sake"))
        dao.insert(CustomCategory("Absinthe"))

        assertEquals(listOf("Absinthe", "Sake"), dao.getAll().first())
    }

    @Test
    fun delete_removesCategoryFromGetAll() = runBlocking {
        dao.insert(CustomCategory("Mead"))
        dao.insert(CustomCategory("Sake"))

        dao.delete(CustomCategory("Mead"))

        assertEquals(listOf("Sake"), dao.getAll().first())
    }

    @Test
    fun repository_addCustomCategoryGoesThroughRealRoom() = runBlocking {
        val repository = RoomCategoryRepository(dao)

        repository.addCustomCategory("Mead")

        assertEquals(listOf("Mead"), repository.getCustomCategories().first())
    }
}
