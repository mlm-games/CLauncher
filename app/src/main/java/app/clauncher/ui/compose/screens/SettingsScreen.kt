package app.clauncher.ui.compose.screens

import android.view.Gravity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.clauncher.MainViewModel
import app.clauncher.data.Constants
import app.clauncher.data.Prefs
import app.clauncher.ui.compose.dialogs.NumberPickerDialog
import app.clauncher.ui.compose.dialogs.ThemePickerDialog

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }

    var showNumberPicker by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }
    var showAlignmentPicker by remember { mutableStateOf(false) }
    var showDateTimePicker by remember { mutableStateOf(false) }
    var showTextSizePicker by remember { mutableStateOf(false) }
    var showSwipeDownPicker by remember { mutableStateOf(false) }

    NumberPickerDialog(
        show = showNumberPicker,
        currentValue = prefs.homeAppsNum,
        range = 0..8,
        onDismiss = { showNumberPicker = false },
        onValueSelected = { newValue ->
            prefs.homeAppsNum = newValue
            viewModel.refreshHome(true)
        }
    )

    ThemePickerDialog(
        show = showThemePicker,
        currentTheme = prefs.appTheme,
        onDismiss = { showThemePicker = false },
        onThemeSelected = { newTheme ->
            prefs.appTheme = newTheme
            AppCompatDelegate.setDefaultNightMode(newTheme)
        }
    )

 //   TODO("Other dialogs")

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // General Settings
        item {
            SettingsSection(title = "General") {
                // Home Apps Number
                SettingsItem(
                    title = "Home Apps Number",
                    subtitle = "${prefs.homeAppsNum} apps",
                    onClick = {
                        // Show number picker dialog
                    }
                )

                // App Visibility
                SettingsToggle(
                    title = "Show Apps",
                    isChecked = prefs.toggleAppVisibility,
                    onCheckedChange = {
                        prefs.toggleAppVisibility = it
                        viewModel.updateShowApps(it)
                    }
                )

                // Auto Show Keyboard
                SettingsToggle(
                    title = "Auto Show Keyboard",
                    isChecked = prefs.autoShowKeyboard,
                    onCheckedChange = {
                        prefs.autoShowKeyboard = it
                    }
                )
            }
        }

        // Appearance Settings
        item {
            SettingsSection(title = "Appearance") {
                // Theme Selector
                SettingsItem(
                    title = "Theme",
                    subtitle = when(prefs.appTheme) {
                        AppCompatDelegate.MODE_NIGHT_NO -> "Light"
                        AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
                        else -> "System"
                    },
                    onClick = {
                        // Show theme selection dialog
                    }
                )

                // Text Size
                SettingsItem(
                    title = "Text Size",
                    subtitle = when(prefs.textSizeScale) {
                        Constants.TextSize.ONE -> "1"
                        Constants.TextSize.TWO -> "2"
                        Constants.TextSize.THREE -> "3"
                        Constants.TextSize.FOUR -> "4"
                        Constants.TextSize.FIVE -> "5"
                        Constants.TextSize.SIX -> "6"
                        Constants.TextSize.SEVEN -> "7"
                        else -> "4"
                    },
                    onClick = {
                        // Show text size selection
                    }
                )

                // System Font
                SettingsToggle(
                    title = "Use System Font",
                    isChecked = prefs.useSystemFont,
                    onCheckedChange = {
                        prefs.useSystemFont = it
                        // Would need activity recreation to apply font change
                    }
                )
            }
        }

        // Layout Settings
        item {
            SettingsSection(title = "Layout") {
                // Alignment
                SettingsItem(
                    title = "Alignment",
                    subtitle = when(prefs.homeAlignment) {
                        Gravity.START -> "Left"
                        Gravity.CENTER -> "Center"
                        Gravity.END -> "Right"
                        else -> "Center"
                    },
                    onClick = {
                        // Show alignment options dialog
                    },
                    onLongClick = {
                        // Set app label alignment to match home alignment
                        prefs.appLabelAlignment = prefs.homeAlignment
                    }
                )

                // Bottom Alignment
                SettingsToggle(
                    title = "Bottom Alignment",
                    isChecked = prefs.homeBottomAlignment,
                    onCheckedChange = {
                        prefs.homeBottomAlignment = it
                        viewModel.updateHomeAlignment(prefs.homeAlignment)
                    }
                )

                // Status Bar
                SettingsToggle(
                    title = "Show Status Bar",
                    isChecked = prefs.showStatusBar,
                    onCheckedChange = {
                        prefs.showStatusBar = it
                        // Apply status bar visibility change
                    }
                )

                // Date Time
                SettingsItem(
                    title = "Date & Time",
                    subtitle = when(prefs.dateTimeVisibility) {
                        Constants.DateTime.DATE_ONLY -> "Date only"
                        Constants.DateTime.ON -> "On"
                        else -> "Off"
                    },
                    onClick = {
                        // Show date time options dialog
                    }
                )
            }
        }

        // Gestures Settings
        item {
            SettingsSection(title = "Gestures") {
                // Swipe Left App
                SettingsItem(
                    title = "Swipe Left App",
                    subtitle = if (prefs.swipeLeftEnabled) prefs.appNameSwipeLeft else "Disabled",
                    onClick = {
                        // Navigate to app selection if enabled
                    },
                    onLongClick = {
                        // Toggle swipe left enabled
                        prefs.swipeLeftEnabled = !prefs.swipeLeftEnabled
                    }
                )

                // Swipe Right App
                SettingsItem(
                    title = "Swipe Right App",
                    subtitle = if (prefs.swipeRightEnabled) prefs.appNameSwipeRight else "Disabled",
                    onClick = {
                        // Navigate to app selection if enabled
                    },
                    onLongClick = {
                        // Toggle swipe right enabled
                        prefs.swipeRightEnabled = !prefs.swipeRightEnabled
                    }
                )

                // Swipe Down Action
                SettingsItem(
                    title = "Swipe Down Action",
                    subtitle = when(prefs.swipeDownAction) {
                        Constants.SwipeDownAction.NOTIFICATIONS -> "Notifications"
                        else -> "Search"
                    },
                    onClick = {
                        // Show swipe down action options
                    }
                )
            }
        }

        // System Settings
        item {
            SettingsSection(title = "System") {
                // Default Launcher
                SettingsItem(
                    title = "Set as Default Launcher",
                    onClick = {
                        viewModel.resetLauncherLiveData.call()
                    }
                )

                // Hidden Apps
                SettingsItem(
                    title = "Hidden Apps",
                    onClick = {
                        // Navigate to hidden apps screen
                    }
                )

                // App Info
                SettingsItem(
                    title = "App Info",
                    onClick = {
                        // Open app info in system settings
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun SettingsToggle(
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}