import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import app.clauncher.MainViewModel
import app.clauncher.data.Prefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    navigator: (String) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    val scope = rememberCoroutineScope()

    LazyColumn {
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
                    isChecked = prefs.showApps,
                    onCheckedChange = {
                        scope.launch {
                            prefs.showApps = it
                            viewModel.updateShowApps(it)
                        }
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
                SettingsDropDown(
                    title = "Theme",
                    options = listOf("Light", "Dark", "System"),
                    selectedOption = when(prefs.theme) {
                        AppCompatDelegate.MODE_NIGHT_NO -> "Light"
                        AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
                        else -> "System"
                    },
                    onOptionSelected = { theme ->
                        val mode = when(theme) {
                            "Light" -> AppCompatDelegate.MODE_NIGHT_NO
                            "Dark" -> AppCompatDelegate.MODE_NIGHT_YES
                            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        }
                        viewModel.updateTheme(mode)
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
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
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

class SettingsViewModel(
    private val prefs: Prefs,
    private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    data class SettingsUiState(
        val homeAppsNum: Int = 0,
        val showApps: Boolean = true,
        val autoShowKeyboard: Boolean = false,
        val theme: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        val useSystemFont: Boolean = false,
        val statusBarVisible: Boolean = true,
        val dateTimeVisibility: Int = Constants.DateTime.ON,
        val swipeLeftEnabled: Boolean = false,
        val swipeRightEnabled: Boolean = false,
        val homeAlignment: Int = Gravity.CENTER,
        val homeBottomAlignment: Boolean = false,
        val textSizeScale: Float = Constants.TextSize.FOUR
    )

    init {
        _uiState.update {
            it.copy(
                homeAppsNum = prefs.homeAppsNum,
                showApps = prefs.toggleAppVisibility,
                autoShowKeyboard = prefs.autoShowKeyboard,
                theme = prefs.appTheme,
                useSystemFont = prefs.useSystemFont,
                statusBarVisible = prefs.showStatusBar,
                dateTimeVisibility = prefs.dateTimeVisibility,
                swipeLeftEnabled = prefs.swipeLeftEnabled,
                swipeRightEnabled = prefs.swipeRightEnabled,
                homeAlignment = prefs.homeAlignment,
                homeBottomAlignment = prefs.homeBottomAlignment,
                textSizeScale = prefs.textSizeScale
            )
        }
    }

    fun toggleSystemFont() {
        viewModelScope.launch {
            prefs.useSystemFont = !prefs.useSystemFont
            _uiState.update { it.copy(useSystemFont = prefs.useSystemFont) }
        }
    }

    fun toggleStatusBar() {
        viewModelScope.launch {
            prefs.showStatusBar = !prefs.showStatusBar
            _uiState.update { it.copy(statusBarVisible = prefs.showStatusBar) }
        }
    }

    // Add other methods later for updating settings
}


@Composable
private fun SystemPermissionsSection(
    viewModel: SettingsViewModel,
    context: Context
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* Handle result */ }

    SettingsSection(title = "Permissions") {
        SettingsItem(
            title = "Default Launcher",
            subtitle = if (context.isClauncherDefault()) "Enabled" else "Disabled",
            onClick = {
                viewModel.resetLauncher()
            }
        )

        SettingsItem(
            title = "Accessibility Service",
            subtitle = if (context.isAccessServiceEnabled()) "Enabled" else "Disabled",
            onClick = {
                permissionLauncher.launch(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        )
    }
}

@Composable
private fun NumberPickerDialog(
    show: Boolean,
    current: Int,
    onDismiss: () -> Unit,
    onNumberSelected: (Int) -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select Number of Apps") },
            text = {
                NumberPicker(
                    value = current,
                    onValueChange = { onNumberSelected(it) },
                    range = 0..8
                )
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val settingsViewModel = remember {
        SettingsViewModel(Prefs(context), context)
    }
    val state by settingsViewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // General Settings
        item {
            SettingsSection(title = "General") {
                // Home Apps Number
                SettingsItem(
                    title = "Home Apps Number",
                    subtitle = "${state.homeAppsNum} apps",
                    onClick = {
                        // Show number picker dialog
                    }
                )

                // App Visibility
                SettingsToggle(
                    title = "Show Apps",
                    isChecked = state.showApps,
                    onCheckedChange = {
                        settingsViewModel.toggleAppVisibility()
                    }
                )

                // Auto Show Keyboard
                SettingsToggle(
                    title = "Auto Show Keyboard",
                    isChecked = state.autoShowKeyboard,
                    onCheckedChange = {
                        settingsViewModel.toggleKeyboard()
                    }
                )
            }
        }

        // Appearance Settings
        item {
            SettingsSection(title = "Appearance") {
                // Theme Selector
                SettingsDropDown(
                    title = "Theme",
                    options = listOf("Light", "Dark", "System"),
                    selectedOption = when(state.theme) {
                        AppCompatDelegate.MODE_NIGHT_NO -> "Light"
                        AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
                        else -> "System"
                    },
                    onOptionSelected = { theme ->
                        settingsViewModel.updateTheme(theme)
                    }
                )

                // Text Size
                SettingsItem(
                    title = "Text Size",
                    subtitle = when(state.textSizeScale) {
                        Constants.TextSize.ONE -> "1"
                        Constants.TextSize.TWO -> "2"
                        Constants.TextSize.THREE -> "3"
                        Constants.TextSize.FOUR -> "4"
                        Constants.TextSize.FIVE -> "5"
                        Constants.TextSize.SIX -> "6"
                        Constants.TextSize.SEVEN -> "7"
                        else -> "--"
                    },
                    onClick = {
                        // Show text size picker
                    }
                )

                // System Font
                SettingsToggle(
                    title = "Use System Font",
                    isChecked = state.useSystemFont,
                    onCheckedChange = {
                        settingsViewModel.toggleSystemFont()
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
                    subtitle = when(state.homeAlignment) {
                        Gravity.START -> "Left"
                        Gravity.CENTER -> "Center"
                        Gravity.END -> "Right"
                        else -> "Center"
                    },
                    onClick = {
                        // Show alignment options
                    }
                )

                // Status Bar
                SettingsToggle(
                    title = "Show Status Bar",
                    isChecked = state.statusBarVisible,
                    onCheckedChange = {
                        settingsViewModel.toggleStatusBar()
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
                    subtitle = if (state.swipeLeftEnabled) "Enabled" else "Disabled",
                    onClick = {
                        // Navigate to app selection
                    },
                    onLongClick = {
                        settingsViewModel.toggleSwipeLeft()
                    }
                )

                // Swipe Right App
                SettingsItem(
                    title = "Swipe Right App",
                    subtitle = if (state.swipeRightEnabled) "Enabled" else "Disabled",
                    onClick = {
                        // Navigate to app selection
                    },
                    onLongClick = {
                        settingsViewModel.toggleSwipeRight()
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onToggle() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title)
        Switch(
            checked = isChecked,
            onCheckedChange = { onToggle() }
        )
    }
}

@Composable
private fun SwipeActionsSection(
    leftEnabled: Boolean,
    rightEnabled: Boolean,
    onLeftToggle: () -> Unit,
    onRightToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.swipe_actions),
            style = MaterialTheme.typography.h6
        )

        SwipeActionItem(
            title = stringResource(R.string.swipe_left),
            enabled = leftEnabled,
            onToggle = onLeftToggle
        )

        SwipeActionItem(
            title = stringResource(R.string.swipe_right),
            enabled = rightEnabled,
            onToggle = onRightToggle
        )
    }
}

@Composable
fun SystemUIEffect(
    showStatusBar: Boolean
) {
    val window = (LocalContext.current as Activity).window

    DisposableEffect(showStatusBar) {
        if (showStatusBar) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.show(WindowInsets.Type.statusBars())
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.hide(WindowInsets.Type.statusBars())
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_IMMERSIVE or
                            View.SYSTEM_UI_FLAG_FULLSCREEN
            }
        }

        onDispose { }
    }
}

@Composable
fun PermissionsSection(
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* Handle result */ }

    Column {
        SettingsItem(
            title = stringResource(R.string.accessibility_service),
            onClick = {
                permissionLauncher.launch(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                )
            }
        )

        // Other permission items...
    }
}