package com.example.simplemoneytracker.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplemoneytracker.AppViewModelProvider
import com.example.simplemoneytracker.R
import com.example.simplemoneytracker.ui.navigation.NavigationDestination
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat

object CategoryDestination : NavigationDestination {
    override val route = "categories/{isABill}/{year}/{month}"
    const val IS_CATEGORY_A_BILL = "isABill"
    const val YEAR = "year"
    const val MONTH = "month"
    const val ROUTE_WITH_ARGS = "categories/"
}

@Composable
fun CategoriesPage(
    navigateToSourceScreen: (Int, Int, Int) -> Unit,
    navigateToPreviousScreen: () -> Unit = {},
    viewModel: CategoriesViewModel = viewModel(factory = AppViewModelProvider.Factory)
){
    val coroutineScope = rememberCoroutineScope()
    val categoryCardUiState by viewModel.categoryCardUiState.collectAsState()
    val sourceCardUiState by viewModel.sourceCardUiState.collectAsState()

    fun getCategoryDetails(): CategoryDetails {
        return viewModel.uiCategoryState.categoryDetails
    }

    fun getTotalMoney(sourceUiList: SourceUiList, categoryId: Int): String {
        var total = 0.0
        for (source in sourceUiList.sources) {
            if (source.categoryId == categoryId) {
                total += source.amount
            }
        }
        return NumberFormat.getCurrencyInstance().format(total)
    }


    Scaffold (
        topBar = { SmtTopAppBar(
            title = if (viewModel.isABill) "Expenses" else "Income",
            icon = Icons.Filled.ArrowBack,
            onActionButtonClick = navigateToPreviousScreen,
            onTutorialButtonClick = viewModel::toggleTutorial
        ) },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.createANewCategory() },
                content = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) })
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->

        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(items = categoryCardUiState.categories) { category ->
                CategoryBar(
                    loadSingleCategory = viewModel::loadCategoryFromDatabase,
                    updateACategory = viewModel::updateACategory,
                    deleteACategory = viewModel::deletingACategory,
                    total = getTotalMoney(sourceCardUiState, category.id),
                    year = viewModel.year,
                    month = viewModel.month,
                    navigateToSourceScreen = navigateToSourceScreen,
                    category = category.toDetails()
                )
            }
        }
        if (viewModel.isUserAddingNewCategory) {
            NewCategoryTextField(
                categoryDetails = getCategoryDetails(),
                onValueChange = viewModel::onCategoryNameChanged,
                onDismissRequest = {
                    viewModel.finishedNewCategory()
                    viewModel.resetUiCategoryState()
                                   },
                onAddButtonClick = {
                    viewModel.finishedNewCategory()
                    viewModel.setTypeOfCategory(
                        viewModel.uiCategoryState.categoryDetails.copy(
                            isABill = viewModel.isABill )
                    )
                    coroutineScope.launch {
                        viewModel.saveCategory()
                        viewModel.resetUiCategoryState()
                    }
                }
            )
        }
        if (viewModel.isUserDeletingACategory){
            DeleteCategoryWarning(
                categoryDetails = getCategoryDetails(),
                onCancelButtonClick = {
                    viewModel.cancelDeleteACategory()
                    viewModel.resetUiCategoryState()
                                      },
                onDeleteButtonClick = {
                    coroutineScope.launch {
                        viewModel.categoryWillBeDeleted()
                        viewModel.deleteCategoryAndContainedSources()
                    }
                    coroutineScope.launch {
                        delay(500)
                        viewModel.resetUiCategoryState()
                    }
                }
            )
        }
        if (viewModel.showTutorial) {
            ClickableBackground(onClick = viewModel::toggleTutorial) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    HelperCard(
                        direction = Directions.BOTTOM,
                        text = R.string.tutorial_add_category,
                        screenWidth = 90,
                        modifier = Modifier
                            .align(Alignment.Bottom)
                            .padding(bottom = dimensionResource(id = R.dimen.padding_100dp))
                    )
                }
            }
        }
    }
}


@Composable
fun CategoryBar(
    modifier: Modifier = Modifier,
    loadSingleCategory: (Int) -> Unit = {},
    updateACategory: () -> Unit = {},
    deleteACategory: () -> Unit = {},
    total: String = "",
    year: Int = 0,
    month: Int = 0,
    navigateToSourceScreen: (Int, Int, Int) -> Unit = { _, _, _ ->},
    category: CategoryDetails = CategoryDetails(),
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                dimensionResource(id = R.dimen.padding_8dp)
            )
            .clickable { navigateToSourceScreen(category.id, year, month) },
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = category.name,
                    fontSize = 30.sp,
                    modifier = Modifier
                        .padding(
                            start = dimensionResource(id = R.dimen.padding_16dp),
                            top = dimensionResource(id = R.dimen.padding_12dp)
                        )
                )
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                )
                Text(
                    text = total,
                    fontSize = 30.sp,
                    modifier = Modifier
                        .padding(
                            end = dimensionResource(id = R.dimen.padding_16dp),
                            top = dimensionResource(id = R.dimen.padding_12dp)
                        )
                )
            }
            Spacer(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_4dp)))
            Row {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(
                            start = dimensionResource(id = R.dimen.padding_12dp),
                            bottom = dimensionResource(id = R.dimen.padding_8dp)
                        )
                        .clickable {
                            loadSingleCategory(category.id)
                            deleteACategory()
                        }
                )

                Icon(imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(
                            start = dimensionResource(id = R.dimen.padding_40dp),
                            bottom = dimensionResource(id = R.dimen.padding_8dp)
                        )
                        .clickable {
                            loadSingleCategory(category.id)
                            updateACategory()
                        }
                )
            }
        }
    }
}

//@Preview
//@Composable
//fun CategoryBarPreview() {
//    SimpleMoneyTrackerTheme {
//        CategoryBar(
//            category = CategoryDetails(
//                name = "Food",
//            ),
//            total = "17.38"
//        )
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCategoryTextField(
    categoryDetails: CategoryDetails,
    onValueChange: (CategoryDetails) -> Unit,
    onDismissRequest: () -> Unit = {},
    onAddButtonClick: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
    ) {
        Card {
            Text(
                text = stringResource(id = R.string.enter_category_name),
                fontSize = 26.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(
                        top = dimensionResource(id = R.dimen.padding_12dp),
                        bottom = dimensionResource(id = R.dimen.padding_12dp),
                        start = dimensionResource(id = R.dimen.padding_24dp),
                        end = dimensionResource(id = R.dimen.padding_24dp)
                    )
            )
            TextField(
                value = categoryDetails.name,
                onValueChange = { onValueChange(categoryDetails.copy(name = it)) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                maxLines = 1,
                modifier = Modifier
                    .border(1.dp, Color.Black, RoundedCornerShape(32.dp))
                    .clip(RoundedCornerShape(32.dp))
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_12dp)))
            Button(
                onClick = onAddButtonClick,
                modifier = Modifier
                    .size(width = 80.dp, height = 60.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = dimensionResource(id = R.dimen.padding_12dp))
            ) {
                Text(text = stringResource(id = R.string.button_add))
            }
        }
    }
}

//@Preview
//@Composable
//fun NewCategoryPreview(){
//    SimpleMoneyTrackerTheme {
//        NewCategoryTextField(
//            categoryDetails = CategoryDetails(),
//            onValueChange = {}
//        )
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteCategoryWarning(
    categoryDetails: CategoryDetails,
    onDeleteButtonClick: () -> Unit = {},
    onCancelButtonClick: () -> Unit = {}
){
    AlertDialog(
        onDismissRequest = onCancelButtonClick,
        modifier = Modifier
    ) {
        Card(
            modifier = Modifier
        ) {
            Text(
                text = stringResource(id = R.string.delete_category_warning) + "\n\n${categoryDetails.name}?",
                textAlign = TextAlign.Center,
                fontSize = 26.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = dimensionResource(id = R.dimen.padding_8dp))
            )
            Spacer(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_4dp)))
            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Button(
                    onClick = onDeleteButtonClick,
                    modifier = Modifier
                        .padding(
                            bottom = dimensionResource(id = R.dimen.padding_16dp)
                        )
                ) {
                    Text(text = stringResource(id = R.string.button_delete))
                }
                Spacer(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_16dp)))

                Button(
                    onClick = onCancelButtonClick,
                    modifier = Modifier
                        .padding(
                            bottom = dimensionResource(id = R.dimen.padding_16dp)
                        )
                ) {
                    Text(text = stringResource(id = R.string.button_cancel))
                }
            }
        }
    }
}

//@Preview
//@Composable
//fun DeleteCategoryPreview(){
//    SimpleMoneyTrackerTheme {
//        DeleteCategoryWarning(
//            CategoryDetails(
//                name = "Food"
//            )
//        )
//    }
//}