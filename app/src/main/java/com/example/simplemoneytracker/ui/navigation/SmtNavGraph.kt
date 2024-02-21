package com.example.simplemoneytracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.simplemoneytracker.ui.CategoriesPage
import com.example.simplemoneytracker.ui.CategoryDestination
import com.example.simplemoneytracker.ui.HomeDestination
import com.example.simplemoneytracker.ui.HomePage
import com.example.simplemoneytracker.ui.ItemEntryDestination
import com.example.simplemoneytracker.ui.ItemEntryPage
import com.example.simplemoneytracker.ui.LoadingPage
import com.example.simplemoneytracker.ui.LoadingPageDestination
import com.example.simplemoneytracker.ui.SourceDestination
import com.example.simplemoneytracker.ui.SourcePage

interface NavigationDestination {
    val route: String
}

@Composable
fun SmtNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = LoadingPageDestination.route,
        modifier = modifier
    ) {
        composable(route = LoadingPageDestination.route){
            LoadingPage(
                navigateToHomePage = { navController.navigate(HomeDestination.route) }
            )
        }
        composable(
            route = HomeDestination.route,
        ) {
            HomePage(
                navigateToCategory = { isABill: String, year: Int, month: Int ->
                    navController.navigate(CategoryDestination.ROUTE_WITH_ARGS + "$isABill/$year/$month") },
            )
        }
        composable(
            route = CategoryDestination.route,
            arguments = listOf(
                navArgument("isABill"){
                    type = NavType.BoolType
                },
                navArgument("year"){
                    type = NavType.IntType
                },
                navArgument("month"){
                    type = NavType.IntType
                }
            )) {
            CategoriesPage(
                navigateToSourceScreen = { id: Int, year: Int, month: Int ->
                    navController.navigate(SourceDestination.ROUTE_WITH_ARGS + "$id/$year/$month") },
                navigateToPreviousScreen = {navController.popBackStack()}
            )
        }
        composable(
            route = SourceDestination.route,
            arguments = listOf(
                navArgument("categoryId") {
                    type = NavType.IntType
                },
                navArgument("year"){
                    type = NavType.IntType
                },
                navArgument("month"){
                    type = NavType.IntType
                }
            )
        ) {
            SourcePage(
                navigateToAddItemScreen = { categoryId: Int, year: Int, month: Int ->
                    navController.navigate(
                    goToSourceEditWithCategoryAndSource(categoryId = categoryId, year = year, month = month)
                )},
                navigateToEditItemScreen = { sourceId: Int, categoryId: Int, year: Int, month: Int ->
                    navController.navigate(
                        goToSourceEditWithCategoryAndSource(sourceId = sourceId, categoryId = categoryId, year = year, month = month)) },
                navigateToPreviousScreen = { navController.popBackStack() },
            )
        }
        composable(
            route = ItemEntryDestination.route,
            arguments = listOf(
                navArgument("sourceId") {
                    type = NavType.IntType
                },
                navArgument("categoryId") {
                    type = NavType.IntType
                },
                navArgument("year"){
                    type = NavType.IntType
                },
                navArgument("month"){
                    type = NavType.IntType
                }
            )
        ) {
            ItemEntryPage(
                navigateToPreviousScreen = { navController.popBackStack() },
            )
        }
    }
}


private fun goToSourceEditWithCategoryAndSource(sourceId: Int = 0, categoryId: Int, year: Int, month: Int): String {
    return "${ItemEntryDestination.ROUTE_WITH_ARGS}$sourceId/$categoryId/$year/$month"
}
