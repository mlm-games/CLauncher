package app.clauncher.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.activity.compose.BackHandler
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.clauncher.MainViewModel
import app.clauncher.data.Constants
import app.clauncher.data.Navigation
import app.clauncher.ui.compose.screens.AppDrawerScreen
import app.clauncher.ui.compose.screens.HiddenAppsScreen
import app.clauncher.ui.compose.screens.HomeScreen
import app.clauncher.ui.compose.screens.SettingsScreen

/**
 * Main navigation component for CLauncher
 *
 * Handles navigation between different screens and maintains navigation state
 */
@Composable
fun CLauncherNavigation(
    viewModel: MainViewModel,
    navController: NavHostController = rememberNavController(),
    currentScreen: String,
    onScreenChange: (String) -> Unit
) {
    // Get current route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Navigation.HOME

    LaunchedEffect(currentScreen) {
        if (currentRoute != currentScreen) {
            navController.navigate(currentScreen) {
                // Pop up to the start destination to avoid building up a stack
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                // Avoid multiple copies of the same destination
                launchSingleTop = true
                // Restore state when navigating back to a previously visited screen
                restoreState = true
            }
        }
    }

    NavHost(navController = navController, startDestination = Navigation.HOME) {
        composable(Navigation.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToAppDrawer = { onScreenChange(Navigation.APP_DRAWER) },
                onNavigateToSettings = { onScreenChange(Navigation.SETTINGS) }
            )
        }

        composable(Navigation.APP_DRAWER) {
            AppDrawerScreen(
                viewModel = viewModel,
                onAppClick = { app ->
                    viewModel.selectedApp(app, Constants.FLAG_LAUNCH_APP)
                    onScreenChange(Navigation.HOME)
                }
            )
        }

        composable(Navigation.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { onScreenChange(Navigation.HOME) },
                onNavigateToHiddenApps = { onScreenChange(Navigation.HIDDEN_APPS) }
            )
        }

        composable(Navigation.HIDDEN_APPS) {
            HiddenAppsScreen(
                viewModel = viewModel,
                onNavigateBack = { onScreenChange(Navigation.SETTINGS) }
            )
        }
    }

    // Handle back button for screens other than home
    BackHandler(enabled = currentScreen != Navigation.HOME) {
        onScreenChange(Navigation.HOME)
    }
}