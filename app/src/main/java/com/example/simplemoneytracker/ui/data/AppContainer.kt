package com.example.simplemoneytracker.ui.data

import android.content.Context

interface AppContainer {
    val categoryRepository: CategoryRepository
    val sourceRepository: SourceRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val categoryRepository: CategoryRepository by lazy {
        OfflineCategoryRepository(CategoryDatabase.getDatabase(context).categoryDao())
    }
    override val sourceRepository: SourceRepository by lazy {
        OfflineSourceRepository(SourceDatabase.getDatabase(context).sourceDao())
    }
}