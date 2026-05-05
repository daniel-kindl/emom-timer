package com.emomtimer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.emomtimer.ui.session.ActiveSessionScreen
import com.emomtimer.ui.settings.SettingsScreen
import com.emomtimer.ui.setup.SetupScreen

private const val ROUTE_SETUP = "setup"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_SESSION = "session/{totalDurationMillis}/{intervalMillis}"

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_SETUP) {

        composable(ROUTE_SETUP) {
            SetupScreen(
                onStartSession = { totalMs, intervalMs ->
                    navController.navigate("session/$totalMs/$intervalMs")
                },
                onOpenSettings = { navController.navigate(ROUTE_SETTINGS) },
            )
        }

        composable(ROUTE_SETTINGS) {
            SettingsScreen(onNavigateUp = { navController.navigateUp() })
        }

        composable(
            route = ROUTE_SESSION,
            arguments = listOf(
                navArgument("totalDurationMillis") { type = NavType.LongType },
                navArgument("intervalMillis") { type = NavType.LongType },
            ),
        ) {
            ActiveSessionScreen(
                onSessionFinished = {
                    navController.popBackStack(ROUTE_SETUP, inclusive = false)
                },
            )
        }
    }
}
