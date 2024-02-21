package com.example.simplemoneytracker.ui.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("DELETE FROM category")
    suspend fun deleteAll()

    @Query("SELECT * FROM category WHERE id = :id")
    fun getCategory(id: Int): Flow<Category>

    @Query("SELECT * FROM category WHERE name = :name ORDER BY id DESC")
    fun getCategory(name: String): Flow<Category>

    @Query("SELECT * FROM category WHERE isABill = :isABill ORDER BY id ASC")
    fun getAllCategoriesOfABillType(isABill: Boolean): Flow<List<Category>>

    @Query("SELECT * FROM category WHERE isABill = :isABill AND year = :year AND month = :month ORDER BY id ASC")
    fun getAllCategoriesOfABillType(isABill: Boolean, year: Int, month: Int): Flow<List<Category>>

    @Query("SELECT * FROM category ORDER BY isABill ASC")
    fun getAllCategoriesOrderedByBillType(): Flow<List<Category>>
}