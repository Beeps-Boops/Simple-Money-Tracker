package com.example.simplemoneytracker.ui

import android.icu.util.Calendar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplemoneytracker.ui.data.Category
import com.example.simplemoneytracker.ui.data.CategoryRepository
import com.example.simplemoneytracker.ui.data.SourceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class ItemEntryViewModel(
    private val sourceRepository: SourceRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val date = LocalDate.now()
    private val calendar: Calendar = Calendar.getInstance()

    val sourceId: Int = checkNotNull(savedStateHandle[ItemEntryDestination.SOURCE_ID])
    val categoryId: Int = checkNotNull(savedStateHandle[ItemEntryDestination.CATEGORY_ID])
    val year: Int = checkNotNull(savedStateHandle[ItemEntryDestination.YEAR])
    val month: Int = checkNotNull(savedStateHandle[ItemEntryDestination.MONTH])

    var sourceUiState by mutableStateOf(SourceUiState())
        private set
    var dateTextUiState by mutableStateOf("${month}-" +
            if (isCurrentDate())
                "${date.dayOfMonth}"
            else "1"
             +
            "-${year}")
        private set
    var displayCalendar by mutableStateOf(false)
    var displayCreateSourceWarning by mutableStateOf(false)
        private set

    fun loadSourceFromDatabase() {
        viewModelScope.launch {
            sourceRepository.getSingleSource(sourceId).collect { source ->
                sourceUiState = SourceUiState(source?.toDetails() ?: SourceDetails())
                calendar.timeInMillis = source?.date ?: 0
                dateTextUiState = "${calendar.get(Calendar.MONTH) + 1}-" +
                        "${calendar.get(Calendar.DAY_OF_MONTH)}-" +
                        "${calendar.get(Calendar.YEAR)}"
            }
        }
    }

    suspend fun loadLastMadeSource(){
        sourceUiState = SourceUiState(sourceRepository.getLastMadeSource().first().toDetails())
    }

    fun displayCreationWarning(){
        displayCreateSourceWarning = true
    }

    fun hideCreationWarning(){
        displayCreateSourceWarning = false
    }

    fun isCurrentDate(): Boolean {
        return date.year == year && date.monthValue == month
    }

    fun setTodaysDateForCalendar(){
        if (isCurrentDate())
            calendar.set(date.year, date.monthValue - 1, date.dayOfMonth)
        else
            calendar.set(year, month - 1, 1)
        sourceUiState = SourceUiState(sourceUiState.sourceDetails.copy(
            month = month,
            year = year,
            date = calendar.timeInMillis,
            lastUpdated = calendar.timeInMillis)
        )

    }

    fun onDateChange(year: Int, month: Int, day: Int) {
        dateTextUiState = "${month + 1}-$day-$year"
        calendar.set(year, month, day)
        sourceUiState = SourceUiState(sourceUiState.sourceDetails.copy(date = calendar.timeInMillis, lastUpdated = calendar.timeInMillis))
    }

    fun onSourceUpdate(sourceDetails: SourceDetails){
        if (sourceDetails.name.length <= 15 && sourceDetails.amount.length <= 10)
            sourceUiState = SourceUiState(sourceDetails = sourceDetails)
    }

    fun resetSourceState() {
        sourceUiState = SourceUiState()
    }

    fun setCategoryId() {
        sourceUiState = SourceUiState(sourceUiState.sourceDetails.copy(categoryId = categoryId))
    }

    private fun validateEntry(sourceDetails: SourceDetails = sourceUiState.sourceDetails): Boolean {
        return sourceDetails.name.isNotBlank() && sourceDetails.amount.isNotBlank()
    }

    suspend fun updateNewlyMadeSourceAmount() {
        val presentCalendar = Calendar.getInstance()
        val nextUpdateCalendar = Calendar.getInstance()
        val lastUpdatedCalendar = Calendar.getInstance()
        val currentDate = LocalDate.now()

        presentCalendar.set(currentDate.year, currentDate.monthValue - 1, currentDate.dayOfMonth)
        nextUpdateCalendar.timeInMillis = sourceUiState.sourceDetails.nextUpdateTime()
        lastUpdatedCalendar.timeInMillis = sourceUiState.sourceDetails.lastUpdated

        val originalCategory = categoryRepository.getSingleCategory(sourceUiState.sourceDetails.categoryId).first()
        val categories = categoryRepository.getAllCategoriesOfABillType(originalCategory!!.isABill).first()
        val sources = sourceRepository.getAllSourcesOrderedByCategoryId().first()

        while (presentCalendar > nextUpdateCalendar){
            val sourceDeets = sourceUiState.sourceDetails.toSource()

            if (nextUpdateCalendar.get(Calendar.MONTH) > lastUpdatedCalendar.get(Calendar.MONTH) ||
                nextUpdateCalendar.get(Calendar.YEAR) > lastUpdatedCalendar.get(Calendar.YEAR)){
                var category = categories.find {
                        it.name == originalCategory.name &&
                        it.month == nextUpdateCalendar.get(Calendar.MONTH) + 1 &&
                        it.year == nextUpdateCalendar.get(Calendar.YEAR)}
                if (category == null) {
                    category = Category(
                        name = originalCategory.name,
                        month = nextUpdateCalendar.get(Calendar.MONTH) + 1,
                        year = nextUpdateCalendar.get(Calendar.YEAR),
                        isABill = originalCategory.isABill,
                    )
                    categoryRepository.insertCategory(category)
                    category = checkNotNull(categoryRepository.getSingleCategory(category.name).first())
                }
                val nextMonthsSource = sources.find { it.name == sourceDeets.name && it.categoryId == category.id }
                if (nextMonthsSource == null) {
                    val newSource = sourceDeets.copy(
                        id = 0,
                        amount = sourceDeets.originalAmount,
                        month = nextUpdateCalendar.get(Calendar.MONTH) + 1,
                        year = nextUpdateCalendar.get(Calendar.YEAR),
                        lastUpdated = nextUpdateCalendar.timeInMillis,
                        categoryId = category.id
                    )
                    sourceRepository.insertSource(newSource)
                    sourceUiState = SourceUiState(sourceRepository.getLastMadeSource().first().toDetails())
                } else {
                    sourceUiState = SourceUiState(nextMonthsSource.toDetails().copy())
                }
            } else {
                sourceUiState = SourceUiState(sourceDeets.toDetails().copy(
                    amount = (sourceDeets.amount + sourceDeets.originalAmount).toString(),
                    lastUpdated = nextUpdateCalendar.timeInMillis
                ))
                sourceRepository.updateSource(sourceUiState.sourceDetails.toSource())
            }
            lastUpdatedCalendar.timeInMillis = sourceUiState.sourceDetails.lastUpdated
            nextUpdateCalendar.timeInMillis = sourceUiState.sourceDetails.nextUpdateTime()
        }

    }

    suspend fun saveSource() {
        if (validateEntry() && sourceId == 0) {
            sourceRepository.insertSource(sourceUiState.sourceDetails.toSource())
        } else if (validateEntry() && sourceId != 0) {
            sourceRepository.updateSource(sourceUiState.sourceDetails.toSource())
        }
    }
}

