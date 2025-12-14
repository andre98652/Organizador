package com.example.organizador.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.organizador.ui.viewmodel.ActivityViewModel
import com.example.organizador.ui.screens.home.HomeScreen
import com.example.organizador.ui.screens.addedit.AddEditScreen
import com.example.organizador.ui.screens.detail.DetailScreen
import com.example.organizador.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddEdit : Screen("add_edit?activityId={activityId}") {
        fun createRoute(activityId: Int? = null) = "add_edit?activityId=${activityId ?: -1}"
    }
    object Detail : Screen("detail/{activityId}") {
        fun createRoute(activityId: Int) = "detail/$activityId"
    }
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: ActivityViewModel,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        
        composable(
            route = Screen.AddEdit.route,
            arguments = listOf(navArgument("activityId") { type = NavType.IntType; defaultValue = -1 })
        ) { backStackEntry ->
            val activityId = backStackEntry.arguments?.getInt("activityId") ?: -1
            AddEditScreen(
                navController = navController,
                viewModel = viewModel,
                activityId = if (activityId == -1) null else activityId
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("activityId") { type = NavType.IntType })
        ) { backStackEntry ->
            val activityId = backStackEntry.arguments?.getInt("activityId") ?: return@composable
            DetailScreen(
                navController = navController,
                viewModel = viewModel,
                activityId = activityId
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}
