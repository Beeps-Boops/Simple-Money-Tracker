package com.example.simplemoneytracker.ui

import android.icu.util.Calendar
import android.widget.CalendarView
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplemoneytracker.AppViewModelProvider
import com.example.simplemoneytracker.R
import com.example.simplemoneytracker.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch
import java.time.LocalDate

object ItemEntryDestination : NavigationDestination {
    override val route = "sourceEntry/{sourceId}/{categoryId}/{year}/{month}"
    const val SOURCE_ID = "sourceId"
    const val CATEGORY_ID = "categoryId"
    const val YEAR = "year"
    const val MONTH = "month"
    const val ROUTE_WITH_ARGS = "sourceEntry/"
}


@Composable
fun ItemEntryPage(
    modifier: Modifier = Modifier,
    navigateToPreviousScreen: () -> Unit = {},
    viewModel: ItemEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    fun getSourceDetails(): SourceDetails {
        return viewModel.sourceUiState.sourceDetails
    }
    LaunchedEffect(key1 = true) {
        if (viewModel.sourceId != 0) {
            viewModel.loadSourceFromDatabase()
        } else {
            viewModel.setCategoryId()
            viewModel.setTodaysDateForCalendar()
        }
    }

    Scaffold(
        topBar = {
            SmtTopAppBar(
                icon = Icons.Filled.ArrowBack,
                onActionButtonClick = navigateToPreviousScreen
            )
        }
    ) {innerPadding ->
        ElevatedCard(modifier = modifier
            .padding(innerPadding)
            .fillMaxWidth()
        ) {
            if (!viewModel.displayCalendar) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(id = R.string.item_entry_name_of_source))
                    TextField(
                        value = getSourceDetails().name,
                        onValueChange = { viewModel.onSourceUpdate(getSourceDetails().copy(name = it)) },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        maxLines = 1,
                        modifier = Modifier
                            .border(1.dp, Color.Black, RoundedCornerShape(32.dp))
                            .clip(RoundedCornerShape(32.dp))
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.padding(12.dp))
                    Text(text = stringResource(id = R.string.item_entry_amount))
                    TextField(
                        value = getSourceDetails().amount,
                        onValueChange = { viewModel.onSourceUpdate(getSourceDetails().copy(amount = it, originalAmount = it.toDoubleOrNull() ?: 0.0)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        maxLines = 1,
                        modifier = Modifier
                            .border(1.dp, Color.Black, RoundedCornerShape(32.dp))
                            .clip(RoundedCornerShape(32.dp))
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.padding(12.dp))
                    Text(text = stringResource(id = R.string.item_entry_frequency_title))
                    Spacer(modifier = Modifier.padding(12.dp))
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = getSourceDetails().repeats.equals(0),
                            onClick = { viewModel.onSourceUpdate(getSourceDetails().copy(repeats = 0)) }
                        )
                        Text(
                            text = stringResource(id = R.string.item_entry_frequency_once),
                            modifier = Modifier
                                .clickable { viewModel.onSourceUpdate(getSourceDetails().copy(repeats = 0)) }
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        RadioButton(
                            selected = getSourceDetails().repeats.equals(3),
                            onClick = { viewModel.onSourceUpdate(getSourceDetails().copy(repeats = 3)) }
                        )
                        Text(
                            text = stringResource(id = R.string.item_entry_frequency_biweekly),
                            modifier = Modifier
                                .padding(end = dimensionResource(id = R.dimen.padding_80dp))
                                .width(dimensionResource(id = R.dimen.padding_80dp))
                                .clickable {
                                    viewModel.onSourceUpdate(
                                        getSourceDetails().copy(
                                            repeats = 3
                                        )
                                    )
                                }
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = getSourceDetails().repeats.equals(1),
                            onClick = { viewModel.onSourceUpdate(getSourceDetails().copy(repeats = 1)) }
                        )
                        Text(
                            text = stringResource(id = R.string.item_entry_frequency_daily),
                            modifier = Modifier
                                .clickable { viewModel.onSourceUpdate(getSourceDetails().copy(repeats = 1)) }
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        RadioButton(
                            selected = getSourceDetails().repeats.equals(4),
                            onClick = { viewModel.onSourceUpdate(getSourceDetails().copy(repeats = 4)) }
                        )
                        Text(
                            text = stringResource(id = R.string.item_entry_frequency_monthly),
                            modifier = Modifier
                                .padding(end = dimensionResource(id = R.dimen.padding_80dp))
                                .width(dimensionResource(id = R.dimen.padding_80dp))
                                .clickable {
                                    viewModel.onSourceUpdate(
                                        getSourceDetails().copy(
                                            repeats = 4
                                        )
                                    )
                                }
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = getSourceDetails().repeats.equals(2),
                            onClick = { viewModel.onSourceUpdate(getSourceDetails().copy(repeats = 2)) }
                        )
                        Text(
                            text = stringResource(id = R.string.item_entry_frequency_weekly),
                            modifier = Modifier
                                .clickable { viewModel.onSourceUpdate(getSourceDetails().copy(repeats = 2)) }
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = {viewModel.displayCalendar = true},
                            modifier = Modifier.padding(end = dimensionResource(id = R.dimen.padding_80dp))
                        ) {
                            Text(text = viewModel.dateTextUiState)
                        }
                    }
                    Spacer(modifier = Modifier.padding(12.dp))
                }
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val date = LocalDate.now()
                            if (date.monthValue > viewModel.sourceUiState.sourceDetails.month ||
                                date.year > viewModel.sourceUiState.sourceDetails.year
                                && viewModel.sourceUiState.sourceDetails.repeats > 0){
                                viewModel.displayCreationWarning()
                            } else {
                                viewModel.saveSource()
                                viewModel.resetSourceState()
                                navigateToPreviousScreen()
                            }
                        }
                    },
                    modifier = Modifier
                        .size(width = 80.dp, height = 60.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = dimensionResource(id = R.dimen.padding_16dp))
                ) {
                    Text(text = stringResource(id = R.string.button_add))
                }
            } else {
                CalendarDisplay(viewModel = viewModel)
            }
        }
        if (viewModel.displayCreateSourceWarning){
            CreateSourceWarning(
                onCreateButtonClick = {
                    coroutineScope.launch {
                        viewModel.saveSource()
                        viewModel.loadLastMadeSource()
                        viewModel.updateNewlyMadeSourceAmount()
                        viewModel.hideCreationWarning()
                        navigateToPreviousScreen()
                    }
                },
                onCancelButtonClick = viewModel::hideCreationWarning
            )
        }
    }
}

@Composable
fun CalendarDisplay(
    viewModel: ItemEntryViewModel
){
    val calendar = Calendar.getInstance()
    val context = LocalContext.current
    val calendarView = CalendarView(context)
    val date = LocalDate.now()
    LaunchedEffect(key1 = true) {
        calendar.set(viewModel.year, viewModel.month - 1, 1)
        calendarView.minDate = calendar.timeInMillis
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendarView.maxDate = calendar.timeInMillis
        calendar.set(viewModel.year, viewModel.month - 1,
            if (viewModel.isCurrentDate())
                date.dayOfMonth
            else
                1
        )
        calendarView.date = calendar.timeInMillis
    }

    Column(modifier = Modifier
        .fillMaxWidth()
    ) {
        AndroidView(
            factory = {calendarView},
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            update = {view ->
                view.setOnDateChangeListener { _, year, month, day ->
                    viewModel.onDateChange(year, month, day)
                }
            }
        )
        Button(
            onClick = { viewModel.displayCalendar = false },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {
            Text(text = stringResource(id = R.string.button_set_date))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSourceWarning(
    onCreateButtonClick: () -> Unit = {},
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
                text = stringResource(id = R.string.item_entry_copy_warning_title) + "\n\n" + stringResource(id = R.string.item_entry_copy_warning_content),
                textAlign = TextAlign.Center,
                fontSize = 26.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = dimensionResource(id = R.dimen.padding_8dp))
            )
            Spacer(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_4dp)))
            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Button(
                    onClick = onCreateButtonClick,
                    modifier = Modifier
                        .padding(
                            bottom = dimensionResource(id = R.dimen.padding_16dp)
                        )
                ) {
                    Text(text = stringResource(id = R.string.button_create))
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
//fun CreateSourcePreview(){
//    SimpleMoneyTrackerTheme {
//        CreateSourceWarning(
//        )
//    }
//}