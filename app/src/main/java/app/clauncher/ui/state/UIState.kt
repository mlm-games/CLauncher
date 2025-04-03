package app.clauncher.ui.state

import android.os.UserHandle
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.graphics.ImageBitmap
import app.clauncher.data.AppModel
import app.clauncher.data.Constants

/**
 * Home screen UI state
 */
data class HomeScreenUiState(
    val homeAppsNum: Int = 0,
    val dateTimeVisibility: Int = Constants.DateTime.ON,
    val homeAlignment: Int = android.view.Gravity.CENTER,
    val homeBottomAlignment: Boolean = false,
    val homeApps: List<AppModel?> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val showDateTime: Boolean get() = dateTimeVisibility != Constants.DateTime.OFF
    val showTime: Boolean get() = Constants.DateTime.isTimeVisible(dateTimeVisibility)
    val showDate: Boolean get() = Constants.DateTime.isDateVisible(dateTimeVisibility)
}

/**
 * App drawer UI state
 */
data class AppDrawerUiState(
    val apps: List<AppModel> = emptyList(),
    val filteredApps: List<AppModel> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Settings screen UI state
 */
data class SettingsScreenUiState(
    val homeAppsNum: Int = 0,
    val showAppNames: Boolean = true,
    val autoShowKeyboard: Boolean = true,
    val appTheme: Int = AppCompatDelegate.MODE_NIGHT_YES,
    val textSizeScale: Float = 1.0f,
    val useSystemFont: Boolean = true,
    val autoOpenFilteredApp : Boolean = true,
    val showHiddenAppsOnSearch : Boolean = false,
    val homeAlignment: Int = android.view.Gravity.CENTER,
    val homeBottomAlignment: Boolean = false,
    val statusBar: Boolean = false,
    val dateTimeVisibility: Int = Constants.DateTime.ON,
    val swipeLeftEnabled: Boolean = true,
    val swipeRightEnabled: Boolean = true,
    val swipeLeftAppName: String = "Camera",
    val swipeRightAppName: String = "Phone",
    val swipeDownAction: Int = Constants.SwipeDownAction.NOTIFICATIONS,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val themeText: String get() = when(appTheme) {
        AppCompatDelegate.MODE_NIGHT_NO -> "Light"
        AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> "System"
        else -> "Dark"
    }

    val alignmentText: String get() = when(homeAlignment) {
        android.view.Gravity.START -> "Left"
        android.view.Gravity.CENTER -> "Center"
        android.view.Gravity.END -> "Right"
        else -> "Center"
    }

    val dateTimeText: String get() = when(dateTimeVisibility) {
        Constants.DateTime.DATE_ONLY -> "Date only"
        Constants.DateTime.ON -> "On"
        else -> "Off"
    }

    val textSizeText: String get() = when(textSizeScale) {
        Constants.TextSize.ONE -> "1 (Smallest)"
        Constants.TextSize.TWO -> "2"
        Constants.TextSize.THREE -> "3"
        Constants.TextSize.FOUR -> "4 (Default)"
        Constants.TextSize.FIVE -> "5"
        Constants.TextSize.SIX -> "6"
        Constants.TextSize.SEVEN -> "7 (Largest)"
        else -> "4 (Default)"
    }

    val swipeDownText: String get() = when(swipeDownAction) {
        Constants.SwipeDownAction.NOTIFICATIONS -> "Notifications"
        Constants.SwipeDownAction.SEARCH -> "Search"
        else -> "Notifications"
    }
}

data class AppUiModel(
    val id: String,
    val label: String,
    val packageName: String,
    val icon: ImageBitmap? = null,
    val isHidden: Boolean = false,
    val activityClassName: String? = null,
    val userHandle: UserHandle
)

data class HomeAppUiModel(
    val id: String,
    val label: String,
    val packageName: String,
    val activityClassName: String? = null,
    val userHandle: UserHandle,
    val isInstalled: Boolean = true
)