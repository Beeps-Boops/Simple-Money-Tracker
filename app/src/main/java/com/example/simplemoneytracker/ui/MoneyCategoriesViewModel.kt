package com.example.simplemoneytracker.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplemoneytracker.ui.data.Category
import com.example.simplemoneytracker.ui.data.CategoryRepository
import com.example.simplemoneytracker.ui.data.SourceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class CategoriesViewModel(
    private val categoryRepository: CategoryRepository,
    private val sourceRepository: SourceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var uiCategoryState by mutableStateOf(CategoryState())
        private set
    var isUserAddingNewCategory by mutableStateOf(false)
        private set
    var isUserDeletingACategory by mutableStateOf(false)
        private set
    var showTutorial by mutableStateOf(false)
        private set

    val isABill: Boolean = checkNotNull(savedStateHandle[CategoryDestination.IS_CATEGORY_A_BILL])
    val year: Int = checkNotNull(savedStateHandle[CategoryDestination.YEAR])
    val month: Int = checkNotNull(savedStateHandle[CategoryDestination.MONTH])

    val categoryCardUiState: StateFlow<CategoryUiList> =
        categoryRepository.getAllCategoriesOfABillType(isABill, year, month).map { CategoryUiList(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = CategoryUiList()
            )

    val sourceCardUiState: StateFlow<SourceUiList> =
        sourceRepository.getAllSourcesOrderedByCategoryId(year, month).map { SourceUiList(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SourceUiList()
            )

    fun loadCategoryFromDatabase(id: Int) {
        viewModelScope.launch {
            categoryRepository.getSingleCategory(id).collect { category ->
                uiCategoryState = CategoryState(category?.toDetails() ?: CategoryDetails())
            }
        }
    }

    fun toggleTutorial() {
        showTutorial = !showTutorial
    }

    fun createANewCategory(){
        isUserAddingNewCategory = true
    }

    fun updateACategory(){
        isUserAddingNewCategory = true
    }

    fun finishedNewCategory(){
        isUserAddingNewCategory = false
    }

    fun deletingACategory(){
        this.isUserDeletingACategory = true
    }

    fun cancelDeleteACategory(){
        this.isUserDeletingACategory = false
    }

    fun categoryWillBeDeleted(){
        this.isUserDeletingACategory = false
    }

    fun resetUiCategoryState() {
        this.uiCategoryState = CategoryState()
    }

    fun setTypeOfCategory(categoryDetails: CategoryDetails) {
        uiCategoryState = CategoryState(categoryDetails = categoryDetails)
    }

    fun onCategoryNameChanged(categoryDetails: CategoryDetails) {
        if (categoryDetails.name.length <= 15) {
            uiCategoryState = CategoryState(categoryDetails = categoryDetails)
        }
    }

    private fun validateInput(categoryState: CategoryDetails = uiCategoryState.categoryDetails): Boolean {
        return categoryState.name.isNotBlank()
    }

    suspend fun deleteCategoryAndContainedSources(
        categoryRepository: CategoryRepository = this.categoryRepository,
        sourceRepository: SourceRepository = this.sourceRepository,
        category: Category = uiCategoryState.categoryDetails.toCategory()
    ){
        val sources = sourceRepository.getAllSourcesInACategory(categoryId = category.id)
            .map { SourceUiList(it) }
        categoryRepository.deleteCategory(category)
        sources.collect { sourceList ->
            sourceList.sources.forEach { source ->
                sourceRepository.deleteSource(source)
            }
        }
    }

    suspend fun saveCategory() {
        val id = uiCategoryState.categoryDetails.id
        if (validateInput() && id == 0) {
            uiCategoryState = CategoryState(uiCategoryState.categoryDetails.copy(year = year, month = month))
            categoryRepository.insertCategory(uiCategoryState.categoryDetails.toCategory())
        } else if (validateInput() && id != 0){
            categoryRepository.updateCategory(uiCategoryState.categoryDetails.toCategory())
        }
    }
}

data class CategoryUiList(val categories: List<Category> = listOf())

data class CategoryState(
    val categoryDetails: CategoryDetails = CategoryDetails()
)

data class CategoryDetails(
    val id: Int = 0,
    val name: String = "",
    val month: Int = LocalDate.now().monthValue,
    val year: Int = LocalDate.now().year,
    val isABill: Boolean = false,
)

fun CategoryDetails.toCategory(): Category = Category(
    id = id,
    name = name,
    month = month,
    year = year,
    isABill = isABill
)

fun Category.toDetails(): CategoryDetails = CategoryDetails(
    id = id,
    name = name,
    month = month,
    year = year,
    isABill = isABill
)