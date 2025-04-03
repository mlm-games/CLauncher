package app.clauncher.ui.state

import androidx.compose.ui.graphics.ImageBitmap

// Base state for loading/error handling
sealed class BaseState {
    object Loading : BaseState()
    data class Error(val message: String) : BaseState()
    object Success : BaseState()
}

data class HomeScreenState(
    val homeAppsNum: Int = 0,
    val showDateTime: Boolean = true,
    val showTime: Boolean = true,
    val showDate: Boolean = true,
    val homeAlignment: Int = android.view.Gravity.CENTER,
    val homeBottomAlignment: Boolean = false,
    val homeApps: List<HomeAppUiModel> = emptyList()
)

data class AppDrawerScreenState(
    val apps: List<AppUiModel> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val showAppIcons: Boolean = true
)

data class SettingsScreenState(
    val currentTheme: Int = 0,
    val homeAppsNum: Int = 0,
    val showApps: Boolean = true,
    val autoShowKeyboard: Boolean = true,
    val homeAlignment: Int = android.view.Gravity.CENTER,
    val homeBottomAlignment: Boolean = false,
    val showStatusBar: Boolean = false,
    val dateTimeVisibility: Int = 0,
    val swipeLeftEnabled: Boolean = true,
    val swipeRightEnabled: Boolean = true,
    val textSizeScale: Float = 1.0f
)

data class AppUiModel(
    val id: String,
    val label: String,
    val packageName: String,
    val icon: ImageBitmap? = null,
    val isHidden: Boolean = false
)

data class HomeAppUiModel(
    val id: String,
    val label: String,
    val packageName: String,
    val isInstalled: Boolean = true
)