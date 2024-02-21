package com.example.simplemoneytracker.ui

import android.icu.util.Calendar
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplemoneytracker.AppViewModelProvider
import com.example.simplemoneytracker.R
import com.example.simplemoneytracker.ui.data.Category
import com.example.simplemoneytracker.ui.data.CategoryRepository
import com.example.simplemoneytracker.ui.data.SourceRepository
import com.example.simplemoneytracker.ui.navigation.NavigationDestination
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

object LoadingPageDestination : NavigationDestination {
    override val route = "loading"
}

@Composable
fun LoadingPage(
    viewModel: LoadingPageViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToHomePage: () -> Unit = {}
){
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        coroutineScope.launch {
            while (true){
                viewModel.rotateImage()
                delay(60)
            }
        }
        coroutineScope.launch {
            viewModel.updateSourcesToNow()
        }.invokeOnCompletion {
            navigateToHomePage()
        }
    }

    Scaffold {innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
        ){
            Column(modifier = Modifier.align(Alignment.Center)) {
                Image(
                    painter = painterResource(id = R.drawable.little_guy_foreground),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .rotate(viewModel.rotation)
                )
                Spacer(modifier = Modifier.padding(12.dp))
                Text(text = stringResource(id = R.string.loading),
                    modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

class LoadingPageViewModel(
    private val sourceRepository: SourceRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    var rotation by mutableFloatStateOf(0f)

    fun rotateImage(){
        rotation += 5f
    }

    suspend fun updateSourcesToNow(
        categoryRepository: CategoryRepository = this.categoryRepository,
        sourceRepository: SourceRepository = this.sourceRepository
    ){
        val date = LocalDate.now()
        val presentCalendar = Calendar.getInstance()
        val nextUpdateCalendar = Calendar.getInstance()
        val lastUpdatedCalendar = Calendar.getInstance()
        presentCalendar.set(date.year, date.monthValue - 1, date.dayOfMonth)

        val categories = categoryRepository.getAllCategoriesOrderedByBillType().first()
        val sources = sourceRepository.getAllSourcesOrderedByCategoryId().first()

        sources.filter { it.repeats != 0
        }.forEach { sourceFromList ->
            var currentSource = sourceFromList
            nextUpdateCalendar.timeInMillis = currentSource.toDetails().nextUpdateTime()
            lastUpdatedCalendar.timeInMillis = currentSource.lastUpdated
            val originalCategory = checkNotNull(categories.find { it.id == currentSource.categoryId })

            while (presentCalendar > nextUpdateCalendar){
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
                    val nextMonthsSource = sources.find { it.name == currentSource.name && it.categoryId == category.id }
                    if (nextMonthsSource == null) {
                        val newSource = currentSource.copy(
                            id = 0,
                            amount = currentSource.originalAmount,
                            month = nextUpdateCalendar.get(Calendar.MONTH) + 1,
                            year = nextUpdateCalendar.get(Calendar.YEAR),
                            lastUpdated = nextUpdateCalendar.timeInMillis,
                            categoryId = category.id
                        )
                        sourceRepository.insertSource(newSource)
                        currentSource = sourceRepository.getLastMadeSource().first()
                    } else {
                        break
                    }
                } else {
                    currentSource.amount += currentSource.originalAmount
                    currentSource.lastUpdated = nextUpdateCalendar.timeInMillis
                    sourceRepository.updateSource(currentSource)
                }
                lastUpdatedCalendar.timeInMillis = currentSource.lastUpdated
                nextUpdateCalendar.timeInMillis = currentSource.toDetails().nextUpdateTime()
            }
        }
    }
}