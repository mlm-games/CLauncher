package app.clauncher.ui.compose.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import app.clauncher.ui.compose.BackHandler
import app.clauncher.ui.compose.dialogs.AlignmentPickerDialog
import app.clauncher.ui.compose.dialogs.DateTimeVisibilityDialog
import app.clauncher.ui.compose.dialogs.NumberPickerDialog
import app.clauncher.ui.compose.dialogs.SwipeDownActionDialog
import app.clauncher.ui.compose.dialogs.TextSizeDialog
import app.clauncher.ui.compose.dialogs.ThemePickerDialog
import app.clauncher.ui.compose.util.SystemUIController
import app.clauncher.ui.compose.util.updateStatusBarVisibility
import app.clauncher.ui.events.AppSelectionType
import app.clauncher.ui.events.UiEvent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHiddenApps: () -> Unit = {}
) {

    BackHandler(onBack = onNavigateBack)
    val context = LocalContext.current
    val uiState by viewModel.settingsScreenState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Dialog states
    var showNumberPicker by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }
    var showAlignmentPicker by remember { mutableStateOf(false) }
    var showDateTimePicker by remember { mutableStateOf(false) }
    var showTextSizePicker by remember { mutableStateOf(false) }
    var showSwipeDownPicker by remember { mutableStateOf(false) }

    // Dialogs
    NumberPickerDialog(
        show = showNumberPicker,
        currentValue = uiState.homeAppsNum,
        range = 0..8,
        onDismiss = { showNumberPicker = false },
        onValueSelected = { newValue ->
            coroutineScope.launch {
                viewModel.prefsDataStore.setHomeAppsNum(newValue)
                viewModel.refreshHome(true)
            }
        }
    )

    ThemePickerDialog(
        show = showThemePicker,
        currentTheme = uiState.appTheme,
        onDismiss = { showThemePicker = false },
        onThemeSelected = { newTheme ->
            coroutineScope.launch {
                if (uiState.appTheme != newTheme) {
                    viewModel.prefsDataStore.setAppTheme(newTheme)
                    AppCompatDelegate.setDefaultNightMode(newTheme)
                    (context as? Activity)?.recreate()
                }
            }
        }
    )

    AlignmentPickerDialog(
        show = showAlignmentPicker,
        currentAlignment = uiState.homeAlignment,
        onDismiss = { showAlignmentPicker = false },
        onAlignmentSelected = { alignment ->
            coroutineScope.launch {
                viewModel.prefsDataStore.setHomeAlignment(alignment)
                viewModel.updateHomeAlignment(alignment)
            }
        }
    )

    DateTimeVisibilityDialog(
        show = showDateTimePicker,
        currentVisibility = uiState.dateTimeVisibility,
        onDismiss = { showDateTimePicker = false },
        onVisibilitySelected = { visibility ->
            coroutineScope.launch {
                viewModel.prefsDataStore.setDateTimeVisibility(visibility)
                viewModel.toggleDateTime()
            }
        }
    )

    TextSizeDialog(
        show = showTextSizePicker,
        currentSize = uiState.textSizeScale,
        onDismiss = { showTextSizePicker = false },
        onSizeSelected = { size ->
            coroutineScope.launch {
                viewModel.prefsDataStore.setTextSizeScale(size)
                (context as? Activity)?.recreate()
            }
        }
    )

    SwipeDownActionDialog(
        show = showSwipeDownPicker,
        currentAction = uiState.swipeDownAction,
        onDismiss = { showSwipeDownPicker = false },
        onActionSelected = { action ->
            coroutineScope.launch {
                viewModel.prefsDataStore.updatePreference { it.copy(swipeDownAction = action) }
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
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${uiState.error}")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                SettingsSection(title = "General") {
                    SettingsItem(
                        title = "Home Apps Number",
                        subtitle = "${uiState.homeAppsNum} apps",
                        onClick = { showNumberPicker = true }
                    )

                    SettingsToggle(
                        title = "Show Apps",
                        isChecked = uiState.showAppNames,
                        onCheckedChange = { newValue ->
                            coroutineScope.launch {
                                viewModel.prefsDataStore.setShowAppNames(newValue)
                                viewModel.updateSettingsState()
                                viewModel.updateShowApps(newValue)
                            }
                        }
                    )


                    SettingsToggle(
                        title = "Auto Show Keyboard",
                        isChecked = uiState.autoShowKeyboard,
                        onCheckedChange = {
                            coroutineScope.launch {
                                viewModel.prefsDataStore.setAutoShowKeyboard(it)
                                viewModel.updateSettingsState()
                            }
                        }
                    )

                    SettingsToggle(
                        title = "Show Hidden Apps While Searching",
                        isChecked = uiState.showHiddenAppsOnSearch,
                        onCheckedChange = {
                            coroutineScope.launch {
                                viewModel.prefsDataStore.setShowHiddenAppsOnSearch(it)
                                viewModel.updateSettingsState()
                            }
                        }
                    )

                    SettingsToggle(
                        title = "Auto Open Single Matches",
                        isChecked = uiState.autoOpenFilteredApp,
                        onCheckedChange = {
                            coroutineScope.launch {
                                viewModel.prefsDataStore.setAutoOpenFilteredApp(it)
                                viewModel.updateSettingsState()
                            }
                        }
                    )
                }
            }

            item {
                SettingsSection(title = "Appearance") {
                    SettingsItem(
                        title = "Theme",
                        subtitle = uiState.themeText,
                        onClick = { showThemePicker = true }
                    )

                    SettingsItem(
                        title = "Text Size",
                        subtitle = uiState.textSizeText,
                        onClick = { showTextSizePicker = true }
                    )

                    SettingsToggle(
                        title = "Use System Font",
                        isChecked = uiState.useSystemFont,
                        onCheckedChange = {
                            coroutineScope.launch {
                                viewModel.prefsDataStore.setUseSystemFont(it)
                                viewModel.updateSettingsState()
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
                        subtitle = uiState.alignmentText,
                        onClick = { showAlignmentPicker = true },
                        onLongClick = {
                            coroutineScope.launch {
                                viewModel.prefsDataStore.updatePreference { prefs ->
                                    prefs.copy(appLabelAlignment = uiState.homeAlignment)
                                }
                            }
                        }
                    )

                    SettingsToggle(
                        title = "Bottom Alignment",
                        isChecked = uiState.homeBottomAlignment,
                        onCheckedChange = {
                            coroutineScope.launch {
                                viewModel.prefsDataStore.setHomeBottomAlignment(it)
                                viewModel.updateSettingsState()
                                viewModel.updateHomeAlignment(uiState.homeAlignment)
                            }
                        }
                    )

                    SettingsToggle(
                        title = "Show Status Bar",
                        isChecked = uiState.statusBar,
                        onCheckedChange = {
                            coroutineScope.launch {
                                viewModel.prefsDataStore.setStatusBar(it)
                                viewModel.updateSettingsState()
                                try {
                                    (context as? Activity)?.let { activity ->
                                        updateStatusBarVisibility(activity, it)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    )



                    SettingsItem(
                        title = "Date & Time",
                        subtitle = uiState.dateTimeText,
                        onClick = { showDateTimePicker = true }
                    )
                }
            }

            item {
                SettingsSection(title = "Gestures") {

                    SettingsToggle(
                        title = "Left Swipe Gesture",
                        isChecked = uiState.swipeLeftEnabled,
                        onCheckedChange = {
                            coroutineScope.launch {
                                viewModel.prefsDataStore.setSwipeLeftEnabled(it)
                                viewModel.updateSettingsState()
                            }
                        }
                    )

                    SettingsItem(
                        title = "Swipe Left App",
                        subtitle = if (uiState.swipeLeftEnabled) uiState.swipeLeftAppName ?: "Not set" else "Disabled",
                        onClick = {
                            if (uiState.swipeLeftEnabled) {
                                viewModel.emitEvent(UiEvent.NavigateToAppSelection(AppSelectionType.SWIPE_LEFT_APP))
                            }
                        },
                        onLongClick = {
                            coroutineScope.launch {
                                viewModel.prefsDataStore.updatePreference { prefs ->
                                    prefs.copy(swipeLeftEnabled = !uiState.swipeLeftEnabled)
                                }
                            }
                        }
                    )

                    SettingsToggle(
                        title = "Right Swipe Gesture",
                        isChecked = uiState.swipeRightEnabled,
                        onCheckedChange = {
                            coroutineScope.launch {
                                viewModel.prefsDataStore.setSwipeRightEnabled(it)
                                viewModel.updateSettingsState()
                            }
                        }
                    )

                    SettingsItem(
                        title = "Swipe Right App",
                        subtitle = if (uiState.swipeRightEnabled) uiState.swipeRightAppName ?: "Not set" else "Disabled",
                        onClick = {
                            if (uiState.swipeRightEnabled) {
                                viewModel.emitEvent(UiEvent.NavigateToAppSelection(AppSelectionType.SWIPE_RIGHT_APP))
                            }
                        },
                        onLongClick = {
                            coroutineScope.launch {
                                viewModel.prefsDataStore.updatePreference { prefs ->
                                    prefs.copy(swipeRightEnabled = !uiState.swipeRightEnabled)
                                }
                            }
                        }
                    )

                    // Swipe Down Action
                    SettingsItem(
                        title = "Swipe Down Action",
                        subtitle = uiState.swipeDownText,
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
                            coroutineScope.launch {
                                viewModel.emitEvent(UiEvent.ResetLauncher)
                            }
                        }
                    )

                    SettingsItem(
                        title = "Hidden Apps",
                        onClick = onNavigateToHiddenApps
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
                            coroutineScope.launch {
                                viewModel.emitEvent(UiEvent.ShowDialog(Constants.Dialog.ABOUT))
                            }
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
            .pointerInput(onLongClick) {
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
    var toggleState by remember { mutableStateOf(isChecked) }
    LaunchedEffect(isChecked) {
        toggleState = isChecked
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                toggleState = !toggleState
                onCheckedChange(toggleState)
            }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = toggleState,
            onCheckedChange = {
                toggleState = it
                onCheckedChange(it)
            }
        )
    }
}