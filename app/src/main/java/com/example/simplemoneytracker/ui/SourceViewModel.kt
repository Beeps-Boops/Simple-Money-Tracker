package com.example.simplemoneytracker.ui

import android.icu.util.Calendar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplemoneytracker.ui.data.CategoryRepository
import com.example.simplemoneytracker.ui.data.Source
import com.example.simplemoneytracker.ui.data.SourceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate

class SourceViewModel(
    private val sourceRepository: SourceRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(){

    val categoryId: Int = checkNotNull(savedStateHandle[SourceDestination.CATEGORY_ID])
    val year: Int = checkNotNull(savedStateHandle[SourceDestination.YEAR])
    val month: Int = checkNotNull(savedStateHandle[SourceDestination.MONTH])

    var sourceUiState by mutableStateOf(SourceUiState())
        private set

    var showDeleteSourceWarning by mutableStateOf(false)
        private set

    var title by mutableStateOf("")
        private set

    var checkBoxForDeleteAll by mutableStateOf(false)
        private set

    fun getTitle(){
        viewModelScope.launch {
            categoryRepository.getSingleCategory(categoryId).collect { category ->
                title = category!!.name
            }
        }
    }

    val getSourcesForThisCategory: StateFlow<SourceUiList> =
            sourceRepository.getAllSourcesInACategory(categoryId).map { SourceUiList(it) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = SourceUiList()
                )

    fun loadSourceFromDatabase(id: Int) {
        viewModelScope.launch {
            sourceRepository.getSingleSource(id).collect { source ->
                sourceUiState = SourceUiState(source?.toDetails() ?: SourceDetails())
            }
        }
    }


    fun resetSourceState() {
        sourceUiState = SourceUiState()
    }

    fun getFormattedMoney(money: Double): String {
        return NumberFormat.getCurrencyInstance().format(money)
    }

    fun toggleCheckBox() {
        checkBoxForDeleteAll = !checkBoxForDeleteAll
    }

    fun attemptToDeleteSource(){
        showDeleteSourceWarning = true
    }

    fun cancelDeleteSource(){
        showDeleteSourceWarning = false
    }

    fun acceptedDeleteSource(){
        showDeleteSourceWarning = false
    }

    fun deleteSource() {
        viewModelScope.launch {
            if (sourceUiState.sourceDetails.repeats > 0 && checkBoxForDeleteAll) {
                val sources = sourceRepository.getAllOtherIncarnationsOfASource(
                    name = sourceUiState.sourceDetails.name,
                    date = sourceUiState.sourceDetails.date,
                    originalAmount = sourceUiState.sourceDetails.originalAmount
                ).first()
                sources.forEach {
                    sourceRepository.deleteSource(it)
                }
            } else {
                sourceRepository.deleteSource(sourceUiState.sourceDetails.toSource())
            }
            resetSourceState()
        }
    }

}

data class SourceUiList(val sources: List<Source> = listOf())

data class SourceUiState(
    val sourceDetails: SourceDetails = SourceDetails(),
)

data class SourceDetails(
    val id: Int = 0,
    val name: String = "",
    val month: Int = LocalDate.now().monthValue,
    val year: Int = LocalDate.now().year,
    val repeats: Int = 0,
    val amount: String = "",
    val originalAmount: Double = 0.0,
    val date: Long = Calendar.getInstance().timeInMillis,
    val lastUpdated: Long = Calendar.getInstance().timeInMillis,
    val categoryId: Int = 0,
)

fun SourceDetails.nextUpdateTime(): Long {
    val calender = Calendar.getInstance()
    calender.timeInMillis = this.lastUpdated
    val adder = when(this.repeats){
        0 -> 0
        1 -> 1
        2 -> 7
        3 -> 14
        else -> -1
    }
    if (adder != -1)
        calender.add(Calendar.DATE, adder)
    else
        calender.add(Calendar.MONTH, 1)
    return calender.timeInMillis
}


fun SourceDetails.toSource(): Source {
    return Source(
        id = id,
        name = name,
        month = month,
        year = year,
        repeats = repeats,
        amount = amount.toDoubleOrNull() ?: 0.0,
        originalAmount = originalAmount,
        date = date,
        lastUpdated = lastUpdated,
        categoryId = categoryId,
    )
}

fun Source.toDetails(): SourceDetails {
    return SourceDetails(
        id = id,
        name = name,
        month = month,
        year = year,
        repeats = repeats,
        amount = amount.toString(),
        originalAmount = originalAmount,
        date = date,
        lastUpdated = lastUpdated,
        categoryId = categoryId
    )
}