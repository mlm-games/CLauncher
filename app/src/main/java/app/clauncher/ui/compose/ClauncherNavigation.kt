package app.clauncher.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.constraintlayout.compose.SwipeDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.clauncher.MainViewModel
import app.clauncher.data.Constants
import app.clauncher.ui.compose.screens.AppDrawerScreen
import app.clauncher.ui.compose.screens.HiddenAppsScreen
import app.clauncher.ui.compose.screens.HomeScreen
import app.clauncher.ui.compose.screens.SettingsScreen

@Composable
fun CLauncherNavigation(
    viewModel: MainViewModel,
    currentScreen: String,
    onScreenChange: (String) -> Unit
) {
    val navController = rememberNavController()

    LaunchedEffect(currentScreen) {
        navController.navigate(currentScreen) {
            popUpTo(navController.graph.startDestinationId)
            launchSingleTop = true
        }



    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onOpenAppDrawer = { onScreenChange("appDrawer") },
                onOpenSettings = { onScreenChange("settings") }
            )
        }

        composable("appDrawer") {
            AppDrawerScreen(
                viewModel = viewModel,
                onAppClick = { app ->
                    viewModel.selectedApp(app, Constants.FLAG_LAUNCH_APP)
                    onScreenChange("home")
                }
            )
        }




        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { onScreenChange("home") }
            )
        }

        composable("hiddenApps") {
            HiddenAppsScreen(
                viewModel = viewModel,
                onNavigateBack = { onScreenChange("settings") }
            )
        }
    }
}