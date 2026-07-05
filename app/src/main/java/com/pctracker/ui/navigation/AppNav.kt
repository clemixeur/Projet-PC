package com.pctracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pctracker.ui.home.HomeScreen
import com.pctracker.ui.links.ProductLinksScreen
import com.pctracker.ui.settings.SettingsScreen

private const val ROUTE_HOME = "home"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_LINKS = "links/{componentId}"

@Composable
fun AppNav() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_HOME) {
        composable(ROUTE_HOME) {
            HomeScreen(
                onOpenSettings = { navController.navigate(ROUTE_SETTINGS) },
                onOpenLinks = { componentId -> navController.navigate("links/$componentId") }
            )
        }
        composable(ROUTE_SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            ROUTE_LINKS,
            arguments = listOf(navArgument("componentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val componentId = backStackEntry.arguments?.getLong("componentId") ?: 0L
            ProductLinksScreen(componentId = componentId, onBack = { navController.popBackStack() })
        }
    }
}
