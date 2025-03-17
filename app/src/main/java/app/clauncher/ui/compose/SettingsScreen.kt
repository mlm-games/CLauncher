import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
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



// Reusable Components
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = title)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title)
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}

class SettingsViewModel : ViewModel() {
    private val prefs: Prefs,
    private val context: Context
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    data class SettingsUiState(
        val homeAppsNum: Int = 0,
        val showApps: Boolean = true,
        val autoShowKeyboard: Boolean = false,
        val theme: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        // Add other state properties
    )

    data class SettingsState(
        val isSystemFont: Boolean = false,
        val statusBarVisible: Boolean = true,
        val dateTimeVisibility: Int = Constants.DateTime.ON,
        val homeAppsNum: Int = 0,
        val textSizeScale: Float = Constants.TextSize.FOUR,
        val appTheme: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        val homeAlignment: Int = Gravity.CENTER,
        val swipeLeftEnabled: Boolean = false,
        val swipeRightEnabled: Boolean = false,
        val autoShowKeyboard: Boolean = false,
        val toggleAppVisibility: Boolean = true,
        val plainWallpaper: Boolean = false
    )

    init {
        _uiState.update {
            it.copy(
                isSystemFont = prefs.useSystemFont,
                statusBarVisible = prefs.showStatusBar,
                // ... initialize other states
            )
        }
    }

    fun toggleSystemFont() {
        viewModelScope.launch {
            prefs.useSystemFont = !prefs.useSystemFont
            _uiState.update { it.copy(isSystemFont = prefs.useSystemFont) }
        }
    }

    fun toggleStatusBar() {
        viewModelScope.launch {
            prefs.showStatusBar = !prefs.showStatusBar
            _uiState.update { it.copy(statusBarVisible = prefs.showStatusBar) }
        }
    }

    fun updateHomeAppsNum(num: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(homeAppsNum = num) }
        }
    }

    // Add other update methods
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
    viewModel: SettingsViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LazyColumn {
        // System Font
        item {
            SettingsToggleItem(
                title = stringResource(R.string.system_font),
                isChecked = state.isSystemFont,
                onToggle = { viewModel.toggleSystemFont() }
            )
        }

        // Status Bar
        item {
            SettingsToggleItem(
                title = stringResource(R.string.status_bar),
                isChecked = state.statusBarVisible,
                onToggle = { viewModel.toggleStatusBar() }
            )
        }

        // Theme Selection
        item {
            SettingsDropDownItem(
                title = stringResource(R.string.theme),
                selectedOption = when(state.appTheme) {
                    AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
                    AppCompatDelegate.MODE_NIGHT_NO -> "Light"
                    else -> "System"
                },
                options = listOf("Light", "Dark", "System"),
                onOptionSelected = { viewModel.updateTheme(it) }
            )
        }

        // Swipe Actions
        item {
            SwipeActionsSection(
                leftEnabled = state.swipeLeftEnabled,
                rightEnabled = state.swipeRightEnabled,
                onLeftToggle = { viewModel.toggleSwipeLeft() },
                onRightToggle = { viewModel.toggleSwipeRight() }
            )
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