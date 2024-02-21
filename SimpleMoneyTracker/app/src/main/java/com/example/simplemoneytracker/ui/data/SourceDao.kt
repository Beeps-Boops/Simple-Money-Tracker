package com.example.simplemoneytracker.ui.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(source: Source)

    @Update
    suspend fun update(source: Source)

    @Delete
    suspend fun delete(source: Source)

    @Query("DELETE FROM source")
    suspend fun deleteAll()

    @Query("SELECT * FROM source ORDER BY id DESC LIMIT 1")
    fun getLastMadeSource(): Flow<Source>

    @Query("SELECT * FROM source WHERE id = :id")
    fun getSource(id: Int): Flow<Source>

    @Query("SELECT * FROM source WHERE categoryId = :categoryId ORDER BY id ASC")
    fun getCategoriesSources(categoryId: Int): Flow<List<Source>>

    @Query("SELECT * FROM source ORDER BY categoryId ASC")
    fun getAllSourcesOrderedByCategoryId(): Flow<List<Source>>

    @Query("SELECT * FROM source WHERE year = :year AND month = :month ORDER BY categoryId ASC")
    fun getAllSourcesOrderedByCategoryId(year: Int, month: Int): Flow<List<Source>>

    @Query("SELECT * FROM source WHERE name = :name AND date = :date AND originalAmount = :originalAmount")
    fun getAllOtherIncarnationsOfASource(name: String, date: Long, originalAmount: Double): Flow<List<Source>>
}