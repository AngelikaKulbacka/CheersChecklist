package com.example.cheerschecklist

import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCustomCategories(): Flow<List<String>>
    suspend fun addCustomCategory(name: String)
}

class RoomCategoryRepository(
    private val dao: CustomCategoryDao,
) : CategoryRepository {
    override fun getCustomCategories(): Flow<List<String>> = dao.getAll()
    override suspend fun addCustomCategory(name: String) = dao.insert(CustomCategory(name))
}
