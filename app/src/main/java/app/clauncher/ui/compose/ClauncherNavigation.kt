package app.clauncher.ui.compose

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import app.clauncher.MainViewModel
import app.clauncher.data.AppModel
import app.clauncher.data.Constants
import app.clauncher.data.Navigation
import app.clauncher.ui.compose.screens.AppDrawerScreen
import app.clauncher.ui.compose.screens.HiddenAppsScreen
import app.clauncher.ui.compose.screens.HomeScreen
import app.clauncher.ui.compose.screens.SettingsScreen
import app.clauncher.ui.compose.util.SystemUIController
import app.clauncher.ui.events.UiEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CLauncherNavigation(
    viewModel: MainViewModel,
    currentScreen: String,
    onScreenChange: (String) -> Unit
) {
    val context = LocalContext.current
    val preferences by viewModel.prefsDataStore.preferences.collectAsState(initial = null)

    preferences?.let {
        SystemUIController(showStatusBar = it.statusBar)
    }

    // for UI events
    LaunchedEffect(key1 = viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UiEvent.NavigateToAppDrawer -> {
                    onScreenChange(Navigation.APP_DRAWER)
                }
                is UiEvent.NavigateToSettings -> {
                    onScreenChange(Navigation.SETTINGS)
                }
                is UiEvent.NavigateToHiddenApps -> {
                    onScreenChange(Navigation.HIDDEN_APPS)
                }
                is UiEvent.NavigateBack -> {
                    onScreenChange(Navigation.HOME)
                }
                is UiEvent.ShowToast -> {
                    // Show toast message
                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // Handle other events, presently nothin.
                }
            }
        }
    }

    when (currentScreen) {
        Navigation.HOME -> {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToAppDrawer = {
                    onScreenChange(Navigation.APP_DRAWER)
                },
                onNavigateToSettings = {
                    onScreenChange(Navigation.SETTINGS)
                }
            )
        }
        Navigation.APP_DRAWER -> {
            AppDrawerScreen(
                viewModel = viewModel,
                onAppClick = { app ->
                    viewModel.launchApp(app)
                    onScreenChange(Navigation.HOME)
                }
            )
        }
        Navigation.SETTINGS -> {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    onScreenChange(Navigation.HOME)
                },
                onNavigateToHiddenApps = {
                    onScreenChange(Navigation.HIDDEN_APPS)
                }
            )
        }
        Navigation.HIDDEN_APPS -> {
            HiddenAppsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    onScreenChange(Navigation.SETTINGS)
                }
            )
        }
    }
}