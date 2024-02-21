package com.example.simplemoneytracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import com.example.simplemoneytracker.R
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplemoneytracker.AppViewModelProvider
import com.example.simplemoneytracker.MainActivity
import com.example.simplemoneytracker.ui.navigation.NavigationDestination
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

object HomeDestination : NavigationDestination {
    override val route = "home"
}

enum class Months(val number: Int) {
    January(1),
    February(2),
    March(3),
    April(4),
    May(5),
    June(6),
    July(7),
    August(8),
    September(9),
    October(10),
    November(11),
    December(12);

    companion object {
        fun fromNumber(number: Int): Months? {
            return entries.find { it.number == number }
        }
        fun getAllAsNumbers(): List<Int> {
            return entries.map { it.number }
        }
    }
}


@Composable
fun HomePage(
    navigateToCategory: (String, Int, Int) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
    ){


    val colors: List<Color> = listOf(
        Color(colorResource(id = R.color.red_soft).toArgb()),
        Color(colorResource(id = R.color.green_soft).toArgb())
    )
    val allCategoriesUiState by viewModel.allCategoriesUiState.collectAsState()
    val allSourcesUiState by viewModel.allSourcesUiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()


    fun updateValuesOnDateChange(){
        val categoriesForThisMonth =
            CategoryUiList(allCategoriesUiState.categories.filter { it.month == viewModel.dateToUse.month && it.year == viewModel.dateToUse.year })
        val sourcesForThisMonth =
            SourceUiList(allSourcesUiState.sources.filter { it.month == viewModel.dateToUse.month && it.year == viewModel.dateToUse.year })

        val income = viewModel.getTotalIncomeAmount(sourcesForThisMonth, categoriesForThisMonth)
        val expense = viewModel.getTotalExpenseAmount(sourcesForThisMonth, categoriesForThisMonth)
        val totalMoney = income + expense
        viewModel.netMoney = income - expense
        viewModel.pieValues = viewModel.getPercentages(total = totalMoney, income = income, expense = expense)
    }

    LaunchedEffect(key1 = true) {
        coroutineScope.launch {
            delay(200)
            updateValuesOnDateChange()
        }
    }

    val activity = LocalContext.current as MainActivity

    Scaffold(
        topBar = {
            SmtTopAppBar(
                onActionButtonClick = { activity.finish() },
                onTutorialButtonClick = viewModel::toggleTutorial,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        if (!viewModel.displayDatePicker) {
            if (viewModel.displayTutorial)
                ClickableBackground(onClick = { viewModel.toggleTutorial() }) {}
            MainPage(
                modifier = Modifier.padding(innerPadding),
                month = viewModel.dateToUse.month,
                year = viewModel.dateToUse.year,
                netMoney = viewModel.netMoney,
                toggleDatePicker = viewModel::toggleDatePicker,
                getFormattedMoney = viewModel::getFormattedMoney,
                navigateToCategory = navigateToCategory,
                colors = colors,
                pieChartValues = viewModel.pieValues,
                showTutorial = viewModel.displayTutorial
            )
        } else {
            DatePicker(
                stopDisplayingDatePicker = viewModel::toggleDatePicker,
                getSelectedDate = viewModel::getCurrentDateToUse,
                updateMonth = viewModel::updateMonth,
                updateYear = viewModel::updateYear,
                updateValuesOnDateChange = { updateValuesOnDateChange() },
                modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
fun MainPage(
    modifier: Modifier = Modifier,
    month: Int = 1,
    year: Int = 2024,
    netMoney: Double = 17.38,
    toggleDatePicker: () -> Unit = {},
    getFormattedMoney: (Double) -> String = { _ -> ""},
    navigateToCategory: (String, Int, Int) -> Unit = { _, _, _ -> },
    pieChartValues: List<Float> = listOf(25f, 75f),
    colors: List<Color> = listOf(Color.Red, Color.Green),
    showTutorial: Boolean = true,
    ){

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .padding(
                        start = dimensionResource(id = R.dimen.padding_16dp),
                        end = dimensionResource(id = R.dimen.padding_16dp),
                        top = dimensionResource(id = R.dimen.padding_16dp)
                    ),
            ) {
                Text(
                    text = Months.fromNumber(month).toString(),
                    fontSize = 30.sp,
                    modifier = Modifier
                        .padding(
                            dimensionResource(id = R.dimen.padding_16dp)
                        )
                        .clickable { toggleDatePicker() }
                )
            }
            Card(
                modifier = Modifier
                    .padding(
                        start = dimensionResource(id = R.dimen.padding_16dp),
                        end = dimensionResource(id = R.dimen.padding_16dp),
                        top = dimensionResource(id = R.dimen.padding_16dp)
                    ),
            ) {
                Text(
                    text = year.toString(),
                    fontSize = 30.sp,
                    modifier = Modifier
                        .padding(
                            dimensionResource(id = R.dimen.padding_16dp)
                        )
                        .clickable { toggleDatePicker() }
                )
            }
        }
        Box(
            contentAlignment = Alignment.TopCenter,
        ) {
            ElevatedCard(
                modifier = Modifier
                    .padding(
                        bottom = dimensionResource(id = R.dimen.padding_40dp),
                        top = dimensionResource(id = R.dimen.padding_40dp)
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                PieChart(
                    values = pieChartValues,
                    colors = colors,
                    modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.padding_16dp))
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                if (showTutorial) {
                    HelperCard(
                        direction = Directions.TOP,
                        text = R.string.tutorial_month_selection_left,
                        modifier = Modifier
                            .padding(
                                start = dimensionResource(id = R.dimen.padding_16dp),
                                end = dimensionResource(id = R.dimen.padding_8dp)
                            )
                    )
                    HelperCard(
                        direction = Directions.TOP,
                        text = R.string.tutorial_month_selection_right,
                        modifier = Modifier
                            .padding(
                                start = dimensionResource(id = R.dimen.padding_8dp),
                                end = dimensionResource(id = R.dimen.padding_16dp)
                            )
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            ElevatedCard(
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(100.dp)
                        .background(
                            color = colorResource(
                                id = if (netMoney >= 0) R.color.green_soft else R.color.red_soft
                            )
                        )
                ) {
                    Text(
                        text = getFormattedMoney(netMoney),
                        fontSize = 40.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxSize()
        ) {

            ElevatedButton(
                onClick = { navigateToCategory("false", year, month) },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .width(dimensionResource(id = R.dimen.button_width))
                    .height(dimensionResource(id = R.dimen.button_height)),
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 5.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.button_income),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
            ElevatedButton(
                onClick = { navigateToCategory("true", year, month) },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .width(dimensionResource(id = R.dimen.button_width))
                    .height(dimensionResource(id = R.dimen.button_height)),
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 5.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.button_expenses),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

//@Preview
//@Composable
//fun MainPagePreview(){
//    SimpleMoneyTrackerTheme {
//        MainPage()
//    }
//}


@Composable
fun PieChart(
    modifier: Modifier = Modifier,
    values: List<Float> = listOf(35f, 50f),
    colors: List<Color> = listOf(
        Color(colorResource(id = R.color.green_soft).toArgb()),
        Color(colorResource(id = R.color.red_soft).toArgb())),
    size: Dp = 200.dp
) {
    // Sum of all the values
    val sumOfValues = values.sum()

    // Calculate each proportion value
    val proportions = values.map {
        it * 100 / sumOfValues
    }

    // Convert each proportion to angle
    val sweepAngles = proportions.map {
        it * 360 / 100
    }

    Canvas(
        modifier = modifier
            .size(size = size)
    ) {
        var startAngle = -90f

        for (i in sweepAngles.indices) {
            drawArc(
                color = colors[i],
                startAngle = startAngle,
                sweepAngle = sweepAngles[i],
                useCenter = true
            )
            startAngle += sweepAngles[i]
        }
    }
}

@Composable
fun DatePicker(
    modifier: Modifier = Modifier,
    stopDisplayingDatePicker: () -> Unit = {},
    getSelectedDate: () -> DateToUse = { DateToUse(year = 0, month = 0)},
    updateMonth: (Int) -> Unit = { _ -> },
    updateYear: (Int) -> Unit = { _ -> },
    updateValuesOnDateChange: () -> Unit = {},
    ){
    fun createYearList(): List<Int> {
        val date = LocalDate.now()
        return (2020..date.year).toList().reversed()
    }
    Column(modifier = modifier.fillMaxSize()) {
        Row {
            LazyColumn(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_8dp))
            ) {
                items(items = Months.getAllAsNumbers()) { month ->
                    DatePickerCard(
                        getSelectedDate = getSelectedDate,
                        updateMonth = updateMonth,
                        updateYear = updateYear,
                        isGettingMonth = true,
                        month = month
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            LazyColumn(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_8dp))
            ) {
                items(items = createYearList()) { year ->
                    DatePickerCard(
                        getSelectedDate = getSelectedDate,
                        updateMonth = updateMonth,
                        updateYear = updateYear,
                        isGettingYear = true,
                        year = year
                    )
                }
            }
        }
        Button(
            onClick = {
                updateValuesOnDateChange()
                stopDisplayingDatePicker()
                      },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(dimensionResource(id = R.dimen.padding_16dp))
                .height(dimensionResource(id = R.dimen.padding_60dp))
        ) {

            Text(
                text = stringResource(id = R.string.button_set_date),
                fontSize = 24.sp
            )
        }
    }
}

@Composable
fun DatePickerCard(
    getSelectedDate: () -> DateToUse,
    updateMonth: (Int) -> Unit,
    updateYear: (Int) -> Unit,
    isGettingMonth: Boolean = false,
    month: Int = 0,
    isGettingYear: Boolean = false,
    year: Int = 0,
){
    val text = if (isGettingMonth) Months.fromNumber(month).toString() else year.toString()
    
    fun getColor(): Color {
        return if (isGettingMonth && month == getSelectedDate().month) {
            Color.Green
        } else if (isGettingYear && year == getSelectedDate().year) {
            Color.Green
        } else {
            Color.Gray
        }
    }
    Card(
        modifier = Modifier
            .padding(dimensionResource(id = R.dimen.padding_4dp))
            .width(dimensionResource(id = R.dimen.padding_160dp))
            .clickable {
                if (isGettingMonth)
                    updateMonth(month)
                else
                    updateYear(year)
            },
        colors = CardDefaults.cardColors(
            containerColor = getColor(),
            contentColor = Color.Black
        )
    ) {
        Text(
            text = text,
            fontSize = 28.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
    }
}

//@Preview
//@Composable
//fun PreviewDatePicker(){
//    SimpleMoneyTrackerTheme {
//        DatePicker()
//    }
//}
