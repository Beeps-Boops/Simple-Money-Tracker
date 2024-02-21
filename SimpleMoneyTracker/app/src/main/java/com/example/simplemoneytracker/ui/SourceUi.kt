package com.example.simplemoneytracker.ui

import android.icu.util.Calendar
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplemoneytracker.AppViewModelProvider
import com.example.simplemoneytracker.R
import com.example.simplemoneytracker.ui.navigation.NavigationDestination
import java.time.LocalDate

object SourceDestination : NavigationDestination {
    override val route = "source/{categoryId}/{year}/{month}"
    const val CATEGORY_ID = "categoryId"
    const val YEAR = "year"
    const val MONTH = "month"
    const val ROUTE_WITH_ARGS = "source/"
}

@Composable
fun SourcePage(
    navigateToAddItemScreen: (Int, Int, Int) -> Unit,
    navigateToEditItemScreen: (Int, Int, Int, Int) -> Unit,
    navigateToPreviousScreen: () -> Unit,
    viewModel: SourceViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val sourcesForThisCategory by viewModel.getSourcesForThisCategory.collectAsState()

    fun getSource(): SourceDetails{
        return viewModel.sourceUiState.sourceDetails
    }
    LaunchedEffect(key1 = true) {
        viewModel.getTitle()
    }

    Scaffold(
        topBar = {SmtTopAppBar(
            title = viewModel.title,
            icon = Icons.Filled.ArrowBack,
            onActionButtonClick = navigateToPreviousScreen
        )},
        floatingActionButton = {
            FloatingActionButton(onClick = { navigateToAddItemScreen(viewModel.categoryId, viewModel.year, viewModel.month) },
            content = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) })
        },
        floatingActionButtonPosition = FabPosition.Center
    ) {innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(sourcesForThisCategory.sources) {source ->
                SourceUiDetails(
                    year = viewModel.year,
                    month = viewModel.month,
                    sourceDetails = source.toDetails(),
                    showDeleteSourceWarning = viewModel::attemptToDeleteSource,
                    getFormattedMoney = viewModel::getFormattedMoney,
                    loadSingleSource = viewModel::loadSourceFromDatabase,
                    navigateToEditItemScreen = navigateToEditItemScreen,
                )
            }
        }
        if (viewModel.showDeleteSourceWarning){
            DeleteSourceWarning(
                sourceDetails = getSource(),
                onCancelButtonClick = {
                    viewModel.cancelDeleteSource()
                    viewModel.resetSourceState()
                },
                onDeleteButtonClick = {
                    viewModel.deleteSource()
                    viewModel.acceptedDeleteSource()
                },
                onCheckBoxClick = {
                    viewModel.toggleCheckBox()
                },
                isChecked = viewModel.checkBoxForDeleteAll
            )
        }
    }
}

@Composable
fun SourceUiDetails(
    modifier: Modifier = Modifier,
    year: Int = 0,
    month: Int = 0,
    sourceDetails: SourceDetails = SourceDetails(),
    showDeleteSourceWarning: () -> Unit = {},
    getFormattedMoney: (Double) -> String = {""},
    loadSingleSource: (Int) -> Unit = {},
    navigateToEditItemScreen: (Int, Int, Int, Int) -> Unit = { _, _, _, _ ->},
){
    @Composable
    fun getUpdateDate(sourceDetails: SourceDetails): String{
        if (sourceDetails.repeats == 0) {
            return stringResource(id = R.string.never_update)
        } else {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = sourceDetails.nextUpdateTime()
            val date = LocalDate.now()
            val currentCalendar = Calendar.getInstance()
            currentCalendar.set(date.year, date.monthValue - 1, date.dayOfMonth)
            if (calendar.get(Calendar.MONTH) + 1 > sourceDetails.month &&
                currentCalendar > calendar)
                return stringResource(id = R.string.finished_update)
            return "${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
        }
    }
    ElevatedCard(modifier = modifier
        .fillMaxWidth()
        .padding(dimensionResource(id = R.dimen.padding_8dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
        ) {
        Column {
            Row {
                Text(text = sourceDetails.name,
                    fontSize = 25.sp,
                    modifier = Modifier
                        .padding(start = dimensionResource(id = R.dimen.padding_12dp)))
                Spacer(Modifier.weight(1f))
                Text(text = "(${getFormattedMoney(sourceDetails.originalAmount)})",
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(end = dimensionResource(id = R.dimen.padding_8dp)))
                Text(text = getFormattedMoney(sourceDetails.amount.toDouble()),
                    fontSize = 25.sp,
                    modifier = Modifier
                        .padding(end = dimensionResource(id = R.dimen.padding_8dp)))
            }
            Spacer(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_4dp)))
            Row {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(
                            start = dimensionResource(id = R.dimen.padding_12dp),
                            top = dimensionResource(id = R.dimen.padding_8dp)
                        )
                        .clickable {
                            loadSingleSource(sourceDetails.id)
                            showDeleteSourceWarning()
                        }
                )
                Icon(imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(
                            start = dimensionResource(id = R.dimen.padding_40dp),
                            top = dimensionResource(id = R.dimen.padding_8dp)
                        )
                        .clickable {
                            navigateToEditItemScreen(
                                sourceDetails.id,
                                sourceDetails.categoryId,
                                year,
                                month
                            )
                        }
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(id = R.string.next_update) + "\n${getUpdateDate(sourceDetails)}",
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(end = dimensionResource(id = R.dimen.padding_8dp)))
            }
        }
    }
}

//@Preview
//@Composable
//fun SourceCardPreview(){
//    SimpleMoneyTrackerTheme {
//        SourceUiDetails(
//            sourceDetails = SourceDetails(
//                name = "test",
//                amount = "17.38",
//                originalAmount = 17.38,
//            )
//        )
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteSourceWarning(
    sourceDetails: SourceDetails,
    isChecked: Boolean = false,
    onDeleteButtonClick: () -> Unit = {},
    onCancelButtonClick: () -> Unit = {},
    onCheckBoxClick: () -> Unit = {}
){
    AlertDialog(
        onDismissRequest = onCancelButtonClick,
        modifier = Modifier
    ) {
        Card(
            modifier = Modifier
        ) {
            Text(
                text = stringResource(id = R.string.delete_source_title) +
                        "\n\n${sourceDetails.name}?",
                textAlign = TextAlign.Center,
                fontSize = 26.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = dimensionResource(id = R.dimen.padding_8dp))
            )
            if(sourceDetails.repeats > 0) {
                Spacer(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_8dp)))
                Row(modifier = Modifier
                    .padding(
                        start = dimensionResource(id = R.dimen.padding_8dp),
                        end = dimensionResource(id = R.dimen.padding_8dp)
                    )
                    .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
                    .clickable { onCheckBoxClick() }
                ) {
                    Checkbox(checked = isChecked, onCheckedChange = { onCheckBoxClick() })
                    Text(text = stringResource(id = R.string.delete_source_checkbox),
                        textAlign = TextAlign.Start,
                        fontSize = 20.sp)
                }
                if (isChecked.not()) {
                    Text(
                        text = stringResource(id = R.string.delete_source_warning),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_8dp))
                    )
                }

            }
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
//fun DeleteSourcePreview(){
//    SimpleMoneyTrackerTheme {
//        DeleteSourceWarning(
//            SourceDetails(
//                name = "Food",
//                repeats = 1
//            )
//        )
//    }
//}