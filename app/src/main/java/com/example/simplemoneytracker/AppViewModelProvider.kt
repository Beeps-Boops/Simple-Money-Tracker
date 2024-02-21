package com.example.simplemoneytracker

import android.app.Application
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import com.example.simplemoneytracker.ui.CategoriesViewModel
import com.example.simplemoneytracker.ui.HomeViewModel
import com.example.simplemoneytracker.ui.ItemEntryViewModel
import com.example.simplemoneytracker.ui.LoadingPageViewModel
import com.example.simplemoneytracker.ui.SourceViewModel

object AppViewModelProvider{
    val Factory = viewModelFactory {
        initializer {
            LoadingPageViewModel(
                sourceRepository = smtApplication().container.sourceRepository,
                categoryRepository = smtApplication().container.categoryRepository
            )
        }
        initializer {
            HomeViewModel(
                categoryRepository = smtApplication().container.categoryRepository,
                sourceRepository = smtApplication().container.sourceRepository
            )
        }
        initializer {
            CategoriesViewModel(
                categoryRepository = smtApplication().container.categoryRepository,
                sourceRepository = smtApplication().container.sourceRepository,
                savedStateHandle = this.createSavedStateHandle()
            )
        }
        initializer {
            SourceViewModel(
                sourceRepository = smtApplication().container.sourceRepository,
                categoryRepository = smtApplication().container.categoryRepository,
                savedStateHandle = this.createSavedStateHandle()
            )
        }
        initializer {
            ItemEntryViewModel(
                sourceRepository = smtApplication().container.sourceRepository,
                categoryRepository = smtApplication().container.categoryRepository,
                savedStateHandle = this.createSavedStateHandle())
        }
    }
}


/**
 * Extension function to queries for [Application] object and returns an instance of
 * [InventoryApplication].
 */
fun CreationExtras.smtApplication(): SmtApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as SmtApplication)
