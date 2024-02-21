package com.example.simplemoneytracker.ui.data

import kotlinx.coroutines.flow.Flow

interface SourceRepository {
    suspend fun insertSource(source: Source)
    suspend fun updateSource(source: Source)
    suspend fun deleteSource(source: Source)
    suspend fun deleteAllSources()
    fun getLastMadeSource(): Flow<Source>
    fun getSingleSource(id: Int): Flow<Source?>
    fun getAllSourcesInACategory(categoryId: Int) : Flow<List<Source>>
    fun getAllSourcesOrderedByCategoryId() : Flow<List<Source>>
    fun getAllSourcesOrderedByCategoryId(year: Int, month: Int) : Flow<List<Source>>
    fun getAllOtherIncarnationsOfASource(name: String, date: Long, originalAmount: Double): Flow<List<Source>>
}

class OfflineSourceRepository(private val sourceDao: SourceDao) : SourceRepository {
    override suspend fun insertSource(source: Source) = sourceDao.insert(source)
    override suspend fun updateSource(source: Source) = sourceDao.update(source)
    override suspend fun deleteSource(source: Source) = sourceDao.delete(source)
    override suspend fun deleteAllSources() = sourceDao.deleteAll()
    override fun getLastMadeSource(): Flow<Source> = sourceDao.getLastMadeSource()
    override fun getSingleSource(id: Int): Flow<Source?> = sourceDao.getSource(id)
    override fun getAllSourcesInACategory(categoryId: Int): Flow<List<Source>> = sourceDao.getCategoriesSources(categoryId)
    override fun getAllSourcesOrderedByCategoryId(): Flow<List<Source>> = sourceDao.getAllSourcesOrderedByCategoryId()
    override fun getAllSourcesOrderedByCategoryId(year: Int, month: Int): Flow<List<Source>> = sourceDao.getAllSourcesOrderedByCategoryId(year, month)
    override fun getAllOtherIncarnationsOfASource(name: String, date: Long, originalAmount: Double): Flow<List<Source>> = sourceDao.getAllOtherIncarnationsOfASource(name, date, originalAmount)
}