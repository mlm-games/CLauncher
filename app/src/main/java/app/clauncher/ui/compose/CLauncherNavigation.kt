package app.clauncher.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.clauncher.MainViewModel
import app.clauncher.data.Constants

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
    }
}


@Composable
fun HomeScreen(viewModel: MainViewModel, onOpenAppDrawer: () -> Unit, onOpenSettings: () -> Unit) {
    TODO("Not yet implemented")
}
