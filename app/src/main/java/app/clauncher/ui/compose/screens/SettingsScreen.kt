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
import app.clauncher.helper.isClauncherDefault
import app.clauncher.ui.compose.dialogs.AlignmentPickerDialog
import app.clauncher.ui.compose.dialogs.DateTimeVisibilityDialog
import app.clauncher.ui.compose.dialogs.NumberPickerDialog
import app.clauncher.ui.compose.dialogs.SwipeDownActionDialog
import app.clauncher.ui.compose.dialogs.TextSizeDialog
import app.clauncher.ui.compose.dialogs.ThemePickerDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHiddenApps: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefsDataStore = remember { viewModel.prefsDataStore }

    // Collect preference flows as state
    val homeAppsNum by viewModel.homeAppsNum.collectAsState()
    val homeAlignment by viewModel.homeAlignment.collectAsState()
    val dateTimeVisibility by viewModel.dateTimeVisibility.collectAsState()

    // Additional state collections
    val showAppNames by prefsDataStore.showAppNames.collectAsState(initial = true)
    val autoShowKeyboard by prefsDataStore.autoShowKeyboard.collectAsState(initial = true)
    val appTheme by prefsDataStore.appTheme.collectAsState(initial = AppCompatDelegate.MODE_NIGHT_YES)
    val textSizeScale by prefsDataStore.textSizeScale.collectAsState(initial = 1.0f)
    val useSystemFont by prefsDataStore.useSystemFont.collectAsState(initial = true)
    val homeBottomAlignment by prefsDataStore.homeBottomAlignment.collectAsState(initial = false)
    val statusBar by prefsDataStore.statusBar.collectAsState(initial = false)
    val swipeLeftEnabled by prefsDataStore.swipeLeftEnabled.collectAsState(initial = true)
    val swipeRightEnabled by prefsDataStore.swipeRightEnabled.collectAsState(initial = true)
    val appNameSwipeLeft by prefsDataStore.appNameSwipeLeft.collectAsState(initial = "Camera")
    val appNameSwipeRight by prefsDataStore.appNameSwipeRight.collectAsState(initial = "Phone")
    val swipeDownAction by prefsDataStore.swipeDownAction.collectAsState(initial = Constants.SwipeDownAction.NOTIFICATIONS)

    var showNumberPicker by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }
    var showAlignmentPicker by remember { mutableStateOf(false) }
    var showDateTimePicker by remember { mutableStateOf(false) }
    var showTextSizePicker by remember { mutableStateOf(false) }
    var showSwipeDownPicker by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Dialogs
    NumberPickerDialog(
        show = showNumberPicker,
        currentValue = homeAppsNum,
        range = 0..8,
        onDismiss = { showNumberPicker = false },
        onValueSelected = { newValue ->
            coroutineScope.launch {
                prefsDataStore.setHomeAppsNum(newValue)
                viewModel.refreshHome(true)
            }
        }
    )

    ThemePickerDialog(
        show = showThemePicker,
        currentTheme = appTheme,
        onDismiss = { showThemePicker = false },
        onThemeSelected = { newTheme ->
            coroutineScope.launch {
                if (appTheme != newTheme) {
                    prefsDataStore.setAppTheme(newTheme)
                    AppCompatDelegate.setDefaultNightMode(newTheme)
                    (context as? Activity)?.recreate()
                }
            }
            showThemePicker = false
        }
    )

    AlignmentPickerDialog(
        show = showAlignmentPicker,
        currentAlignment = homeAlignment,
        onDismiss = { showAlignmentPicker = false },
        onAlignmentSelected = { alignment ->
            coroutineScope.launch {
                prefsDataStore.setHomeAlignment(alignment)
                viewModel.updateHomeAlignment(alignment)
            }
        }
    )

    DateTimeVisibilityDialog(
        show = showDateTimePicker,
        currentVisibility = dateTimeVisibility,
        onDismiss = { showDateTimePicker = false },
        onVisibilitySelected = { visibility ->
            coroutineScope.launch {
                prefsDataStore.setDateTimeVisibility(visibility)
                viewModel.toggleDateTime()
            }
        }
    )

    TextSizeDialog(
        show = showTextSizePicker,
        currentSize = textSizeScale,
        onDismiss = { showTextSizePicker = false },
        onSizeSelected = { size ->
            coroutineScope.launch {
                prefsDataStore.setTextSizeScale(size)
                // Need to recreate activity to apply text size change
                (context as? Activity)?.recreate()
            }
        }
    )

    SwipeDownActionDialog(
        show = showSwipeDownPicker,
        currentAction = swipeDownAction,
        onDismiss = { showSwipeDownPicker = false },
        onActionSelected = { action ->
            coroutineScope.launch {
                prefsDataStore.setSwipeDownAction(action)
            }
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
                        subtitle = "$homeAppsNum apps",
                        onClick = { showNumberPicker = true }
                    )

                    SettingsToggle(
                        title = "Show Apps",
                        isChecked = showAppNames,
                        onCheckedChange = {
                            coroutineScope.launch {
                                prefsDataStore.setShowAppNames(it)
                                viewModel.updateShowApps(it)
                            }
                        }
                    )

                    SettingsToggle(
                        title = "Auto Show Keyboard",
                        isChecked = autoShowKeyboard,
                        onCheckedChange = {
                            coroutineScope.launch {
                                prefsDataStore.setAutoShowKeyboard(it)
                            }
                        }
                    )
                }
            }

            item {
                SettingsSection(title = "Appearance") {
                    SettingsItem(
                        title = "Theme",
                        subtitle = when(appTheme) {
                            AppCompatDelegate.MODE_NIGHT_NO -> "Light"
                            AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
                            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> "System"
                            else -> "Dark"
                        },
                        onClick = { showThemePicker = true }
                    )

                    SettingsItem(
                        title = "Text Size",
                        subtitle = when(textSizeScale) {
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
                        isChecked = useSystemFont,
                        onCheckedChange = {
                            coroutineScope.launch {
                                prefsDataStore.setUseSystemFont(it)
                                // Would need activity recreation to apply font change
                                (context as? Activity)?.recreate()
                            }
                        }
                    )
                }
            }

            item {
                SettingsSection(title = "Layout") {
                    SettingsItem(
                        title = "Alignment",
                        subtitle = when(homeAlignment) {
                            Gravity.START -> "Left"
                            Gravity.CENTER -> "Center"
                            Gravity.END -> "Right"
                            else -> "Center"
                        },
                        onClick = { showAlignmentPicker = true },
                        onLongClick = {
                            // Set app label alignment to match home alignment
                            coroutineScope.launch {
                                prefsDataStore.setAppLabelAlignment(homeAlignment)
                            }
                        }
                    )

                    SettingsToggle(
                        title = "Bottom Alignment",
                        isChecked = homeBottomAlignment,
                        onCheckedChange = {
                            coroutineScope.launch {
                                prefsDataStore.setHomeBottomAlignment(it)
                                viewModel.updateHomeAlignment(homeAlignment)
                            }
                        }
                    )

                    SettingsToggle(
                        title = "Show Status Bar",
                        isChecked = statusBar,
                        onCheckedChange = {
                            coroutineScope.launch {
                                prefsDataStore.setStatusBar(it)
                                if (context is Activity) {
                                    if (it) {
                                        context.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                                    } else {
                                        context.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
                                    }
                                }
                            }
                        }
                    )

                    SettingsItem(
                        title = "Date & Time",
                        subtitle = when(dateTimeVisibility) {
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
                        subtitle = if (swipeLeftEnabled) appNameSwipeLeft ?: "Not set" else "Disabled",
                        onClick = {
                            if (swipeLeftEnabled) {
                                // Navigate to app selection
                                // This would typically navigate to app drawer with a selection mode
                                // For now, just toggle the enabled state
                                coroutineScope.launch {
                                    prefsDataStore.setSwipeLeftEnabled(!swipeLeftEnabled)
                                }
                            }
                        },
                        onLongClick = {
                            // Toggle swipe left enabled
                            coroutineScope.launch {
                                prefsDataStore.setSwipeLeftEnabled(!swipeLeftEnabled)
                            }
                        }
                    )

                    SettingsItem(
                        title = "Swipe Right App",
                        subtitle = if (swipeRightEnabled) appNameSwipeRight ?: "Not set" else "Disabled",
                        onClick = {
                            if (swipeRightEnabled) {
                                // Navigate to app selection
                                // This would typically navigate to app drawer with a selection mode
                                // For now, just toggle the enabled state
                                coroutineScope.launch {
                                    prefsDataStore.setSwipeRightEnabled(!swipeRightEnabled)
                                }
                            }
                        },
                        onLongClick = {
                            coroutineScope.launch {
                                prefsDataStore.setSwipeRightEnabled(!swipeRightEnabled)
                            }
                        }
                    )

                    // Swipe Down Action
                    SettingsItem(
                        title = "Swipe Down Action",
                        subtitle = when(swipeDownAction) {
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
                            // Use events manager instead of LiveData
//                            viewModel._eventsManager.emitEvent("resetLauncher") //FIXME:Same as below
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
                            // Use events manager instead of LiveData
//                            viewModel._eventsManager.emitEvent("showAboutDialog")  FIXME:Suspend function 'suspend fun emitEvent(event: UiEvent): Unit' should be called only from a coroutine or another suspend function.
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