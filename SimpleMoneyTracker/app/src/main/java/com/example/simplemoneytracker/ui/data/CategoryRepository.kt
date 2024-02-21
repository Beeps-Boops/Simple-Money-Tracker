package com.example.simplemoneytracker.ui.data

import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    suspend fun insertCategory(category: Category)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(category: Category)
    suspend fun deleteAllCategories()
    fun getSingleCategory(id: Int): Flow<Category?>
    fun getSingleCategory(name: String): Flow<Category?>
    fun getAllCategoriesOfABillType(isABill: Boolean) : Flow<List<Category>>
    fun getAllCategoriesOfABillType(isABill: Boolean, year: Int, month: Int) : Flow<List<Category>>
    fun getAllCategoriesOrderedByBillType() : Flow<List<Category>>
}

class OfflineCategoryRepository(private val categoryDao: CategoryDao) : CategoryRepository {
    override suspend fun insertCategory(category: Category) = categoryDao.insert(category)
    override suspend fun updateCategory(category: Category) = categoryDao.update(category)
    override suspend fun deleteCategory(category: Category) = categoryDao.delete(category)
    override suspend fun deleteAllCategories() = categoryDao.deleteAll()
    override fun getSingleCategory(id: Int): Flow<Category?> = categoryDao.getCategory(id)
    override fun getSingleCategory(name: String): Flow<Category?> = categoryDao.getCategory(name)
    override fun getAllCategoriesOfABillType(isABill: Boolean): Flow<List<Category>> = categoryDao.getAllCategoriesOfABillType(isABill)
    override fun getAllCategoriesOfABillType(isABill: Boolean, year: Int, month: Int): Flow<List<Category>> = categoryDao.getAllCategoriesOfABillType(isABill, year, month)
    override fun getAllCategoriesOrderedByBillType(): Flow<List<Category>> = categoryDao.getAllCategoriesOrderedByBillType()
}