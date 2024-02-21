package com.example.simplemoneytracker.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplemoneytracker.ui.data.CategoryRepository
import com.example.simplemoneytracker.ui.data.SourceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.NumberFormat
import java.time.LocalDate


class HomeViewModel(
    categoryRepository: CategoryRepository,
    sourceRepository: SourceRepository,
) : ViewModel() {

    private val date = LocalDate.now()
    var dateToUse by mutableStateOf(DateToUse(date.year, date.monthValue))
        private set

    var displayDatePicker by mutableStateOf(false)
    var displayTutorial by mutableStateOf(false)
    var netMoney by mutableDoubleStateOf(0.0)
    var pieValues by mutableStateOf(listOf(0f, 0f))

    val allCategoriesUiState: StateFlow<CategoryUiList> =
        categoryRepository.getAllCategoriesOrderedByBillType().map { CategoryUiList(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = CategoryUiList()
            )

    val allSourcesUiState: StateFlow<SourceUiList> =
        sourceRepository.getAllSourcesOrderedByCategoryId().map { SourceUiList(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SourceUiList()
            )


    private fun getBillType(categoryId: Int, allCategories: CategoryUiList): Boolean? {
        for (category in allCategories.categories) {
            if (category.id == categoryId) {
                return category.isABill
            }
        }
        return null
    }

    fun getCurrentDateToUse(): DateToUse {
        return dateToUse
    }

    fun updateMonth(month: Int) {
        dateToUse = dateToUse.copy(month = month)
    }

    fun updateYear(year: Int) {
        dateToUse = dateToUse.copy(year = year)
    }

    fun toggleDatePicker() {
        displayDatePicker = !displayDatePicker
    }

    fun toggleTutorial() {
        displayTutorial = !displayTutorial
    }

    fun getTotalIncomeAmount(
        allSources: SourceUiList,
        allCategories: CategoryUiList
    ): Double {
        var income = 0.0
        for (source in allSources.sources) {
            if (getBillType(source.categoryId, allCategories) == false)
                income += source.amount
        }
        return income
    }

    fun getTotalExpenseAmount(
        allSources: SourceUiList,
        allCategories: CategoryUiList
    ): Double {
        var expense = 0.0
        for (source in allSources.sources) {
            if (getBillType(source.categoryId, allCategories) == true)
                expense += source.amount
        }
        return expense
    }

    fun getPercentages(total: Double, income: Double, expense: Double): List<Float> {
        val incomePercentage: Float = ((income / total) * 100).toFloat()
        val expensePercentage: Float = ((expense / total) * 100).toFloat()
        return listOf(expensePercentage, incomePercentage)
    }

    fun getFormattedMoney(money: Double): String {
        return NumberFormat.getCurrencyInstance().format(money)
    }
}

data class DateToUse(
    val year: Int,
    val month: Int
)