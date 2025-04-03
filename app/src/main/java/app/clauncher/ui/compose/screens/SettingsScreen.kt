package app.clauncher.ui.compose.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.clauncher.MainViewModel
import app.clauncher.data.Constants
import app.clauncher.data.Prefs
import app.clauncher.helper.isClauncherDefault
import app.clauncher.ui.compose.dialogs.AlignmentPickerDialog
import app.clauncher.ui.compose.dialogs.DateTimeVisibilityDialog
import app.clauncher.ui.compose.dialogs.NumberPickerDialog
import app.clauncher.ui.compose.dialogs.SwipeDownActionDialog
import app.clauncher.ui.compose.dialogs.TextSizeDialog
import app.clauncher.ui.compose.dialogs.ThemePickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHiddenApps: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }

    var showNumberPicker by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }
    var showAlignmentPicker by remember { mutableStateOf(false) }
    var showDateTimePicker by remember { mutableStateOf(false) }
    var showTextSizePicker by remember { mutableStateOf(false) }
    var showSwipeDownPicker by remember { mutableStateOf(false) }

    // Dialogs
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
            if (prefs.appTheme != newTheme) {
                prefs.appTheme = newTheme
                AppCompatDelegate.setDefaultNightMode(newTheme)
                (context as? Activity)?.recreate()
            }
            showThemePicker = false
        }
    )


    AlignmentPickerDialog(
        show = showAlignmentPicker,
        currentAlignment = prefs.homeAlignment,
        onDismiss = { showAlignmentPicker = false },
        onAlignmentSelected = { alignment ->
            prefs.homeAlignment = alignment
            viewModel.updateHomeAlignment(alignment)
        }
    )

    DateTimeVisibilityDialog(
        show = showDateTimePicker,
        currentVisibility = prefs.dateTimeVisibility,
        onDismiss = { showDateTimePicker = false },
        onVisibilitySelected = { visibility ->
            prefs.dateTimeVisibility = visibility
            viewModel.toggleDateTime()
        }
    )

    TextSizeDialog(
        show = showTextSizePicker,
        currentSize = prefs.textSizeScale,
        onDismiss = { showTextSizePicker = false },
        onSizeSelected = { size ->
            prefs.textSizeScale = size
            // Need to recreate activity to apply text size change
            (context as? Activity)?.recreate()
        }
    )

    SwipeDownActionDialog(
        show = showSwipeDownPicker,
        currentAction = prefs.swipeDownAction,
        onDismiss = { showSwipeDownPicker = false },
        onActionSelected = { action ->
            prefs.swipeDownAction = action
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                SettingsSection(title = "General") {
                    SettingsItem(
                        title = "Home Apps Number",
                        subtitle = "${prefs.homeAppsNum} apps",
                        onClick = { showNumberPicker = true }
                    )

                    SettingsToggle(
                        title = "Show Apps",
                        isChecked = prefs.toggleAppVisibility,
                        onCheckedChange = {
                            prefs.toggleAppVisibility = it
                            viewModel.updateShowApps(it)
                        }
                    )

                    SettingsToggle(
                        title = "Auto Show Keyboard",
                        isChecked = prefs.autoShowKeyboard,
                        onCheckedChange = {
                            prefs.autoShowKeyboard = it
                        }
                    )
                }
            }

            item {
                SettingsSection(title = "Appearance") {
                    SettingsItem(
                        title = "Theme",
                        subtitle = when(prefs.appTheme) {
                            AppCompatDelegate.MODE_NIGHT_NO -> "Light"
                            AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
                            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> "System"
                            else -> "Dark"
                        },
                        onClick = { showThemePicker = true }
                    )

                    SettingsItem(
                        title = "Text Size",
                        subtitle = when(prefs.textSizeScale) {
                            Constants.TextSize.ONE -> "1 (Smallest)"
                            Constants.TextSize.TWO -> "2"
                            Constants.TextSize.THREE -> "3"
                            Constants.TextSize.FOUR -> "4 (Default)"
                            Constants.TextSize.FIVE -> "5"
                            Constants.TextSize.SIX -> "6"
                            Constants.TextSize.SEVEN -> "7 (Largest)"
                            else -> "4 (Default)"
                        },
                        onClick = { showTextSizePicker = true }
                    )

                    SettingsToggle(
                        title = "Use System Font",
                        isChecked = prefs.useSystemFont,
                        onCheckedChange = {
                            prefs.useSystemFont = it
                            // Would need activity recreation to apply font change
                            (context as? Activity)?.recreate()
                        }
                    )
                }
            }

            item {
                SettingsSection(title = "Layout") {
                    SettingsItem(
                        title = "Alignment",
                        subtitle = when(prefs.homeAlignment) {
                            Gravity.START -> "Left"
                            Gravity.CENTER -> "Center"
                            Gravity.END -> "Right"
                            else -> "Center"
                        },
                        onClick = { showAlignmentPicker = true },
                        onLongClick = {
                            // Set app label alignment to match home alignment
                            prefs.appLabelAlignment = prefs.homeAlignment
                        }
                    )

                    SettingsToggle(
                        title = "Bottom Alignment",
                        isChecked = prefs.homeBottomAlignment,
                        onCheckedChange = {
                            prefs.homeBottomAlignment = it
                            viewModel.updateHomeAlignment(prefs.homeAlignment)
                        }
                    )

                    SettingsToggle(
                        title = "Show Status Bar",
                        isChecked = prefs.showStatusBar,
                        onCheckedChange = {
                            prefs.showStatusBar = it
                            if (context is Activity) {
                                if (it) {
                                    context.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                                } else {
                                    context.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
                                }
                            }
                        }
                    )

                    SettingsItem(
                        title = "Date & Time",
                        subtitle = when(prefs.dateTimeVisibility) {
                            Constants.DateTime.DATE_ONLY -> "Date only"
                            Constants.DateTime.ON -> "On"
                            else -> "Off"
                        },
                        onClick = { showDateTimePicker = true }
                    )
                }
            }

            item {
                SettingsSection(title = "Gestures") {
                    // Swipe Left App
                    SettingsItem(
                        title = "Swipe Left App",
                        subtitle = if (prefs.swipeLeftEnabled) prefs.appNameSwipeLeft ?: "Not set" else "Disabled",
                        onClick = {
                            if (prefs.swipeLeftEnabled) {
                                // Navigate to app selection
                                // This would typically navigate to app drawer with a selection mode
                                // For now, just toggle the enabled state
                                prefs.swipeLeftEnabled = !prefs.swipeLeftEnabled
                            }
                        },
                        onLongClick = {
                            // Toggle swipe left enabled
                            prefs.swipeLeftEnabled = !prefs.swipeLeftEnabled
                        }
                    )

                    SettingsItem(
                        title = "Swipe Right App",
                        subtitle = if (prefs.swipeRightEnabled) prefs.appNameSwipeRight ?: "Not set" else "Disabled",
                        onClick = {
                            if (prefs.swipeRightEnabled) {
                                // Navigate to app selection
                                // This would typically navigate to app drawer with a selection mode
                                // For now, just toggle the enabled state
                                prefs.swipeRightEnabled = !prefs.swipeRightEnabled
                            }
                        },
                        onLongClick = {
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
                        onClick = { showSwipeDownPicker = true }
                    )
                }
            }

            item {
                SettingsSection(title = "System") {
                    SettingsItem(
                        title = "Set as Default Launcher",
                        subtitle = if (isClauncherDefault(context)) "CLauncher is default" else "CLauncher is not default",
                        onClick = {
//                            viewModel.resetLauncherLiveData.call()
                        }
                    )

                    SettingsItem(
                        title = "Hidden Apps",
                        onClick = {
                            onNavigateToHiddenApps()
                        }
                    )

                    SettingsItem(
                        title = "App Info",
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    )

                    SettingsItem(
                        title = "About CLauncher",
                        subtitle = "Version ${context.packageManager.getPackageInfo(context.packageName, 0).versionName}",
                        onClick = {
//                            viewModel.showDialog.value = Constants.Dialog.ABOUT
                        }
                    )
                }
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
            .pointerInput(Unit) {
                if (onLongClick != null) {
                    detectTapGestures(
                        onLongPress = { onLongClick() },
                        onTap = { onClick() }
                    )
                }
            }
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
            .clickable { onCheckedChange(!isChecked) }
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