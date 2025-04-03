package app.clauncher.ui.compose.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import app.clauncher.MainViewModel
import app.clauncher.data.AppModel
import app.clauncher.data.Constants
import app.clauncher.helper.openSearch
import app.clauncher.ui.compose.AppDrawerSearch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawerScreen(
    viewModel: MainViewModel,
    onAppClick: (AppModel) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.appDrawerState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Collect preferences
    val preferences by viewModel.prefsDataStore.preferences.collectAsState(initial = null)
    val autoShowKeyboard = preferences?.autoShowKeyboard ?: true
    val showAppNames = preferences?.showAppNames ?: true

    // Selected app for context menu
    var selectedApp by remember { mutableStateOf<AppModel?>(null) }
    var showContextMenu by remember { mutableStateOf(false) }

    // Load apps when screen is shown
    LaunchedEffect(Unit) {
        viewModel.loadApps()
    }

    // Update search results when query changes
    LaunchedEffect(searchQuery) {
        viewModel.searchApps(searchQuery)
    }

    // Auto-focus search field and show keyboard
    LaunchedEffect(Unit) {
        if (autoShowKeyboard) {
            delay(100) // Small delay to ensure UI is ready
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search field
        AppDrawerSearch(
            searchQuery = searchQuery,
            onSearchChanged = { query -> searchQuery = query },
            modifier = Modifier.focusRequester(focusRequester)
        )

        when {
            // Loading state
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Error state
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: ${uiState.error}")
                }
            }

            // Empty app list
            uiState.apps.isEmpty() && searchQuery.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No apps found")
                }
            }

            // Empty search results
            uiState.filteredApps.isEmpty() && searchQuery.isNotEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No apps found matching \"$searchQuery\"")

                        Button(
                            onClick = {
                                if (searchQuery.startsWith("!")) {
                                    val searchUrl = Constants.URL_DUCK_SEARCH +
                                            searchQuery.substring(1).replace(" ", "%20")
                                    val intent = Intent(Intent.ACTION_VIEW, searchUrl.toUri())
                                    context.startActivity(intent)
                                } else {
                                    openSearch(context)
                                }
                            },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Search Web")
                        }
                    }
                }
            }

            // Show filtered app list
            else -> {
                val appsToShow = if (searchQuery.isEmpty()) uiState.apps else uiState.filteredApps

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(
                        items = appsToShow,
                        key = { app -> app.getKey() }
                    ) { app ->
                        AppListItem(
                            app = app,
                            showAppIcon = showAppNames,
                            onClick = {
                                // If there's only one app in search results and user presses enter,
                                // launch that app directly
                                if (appsToShow.size == 1 && searchQuery.isNotEmpty()) {
                                    onAppClick(app)
                                } else {
                                    onAppClick(app)
                                }
                            },
                            onLongClick = {
                                selectedApp = app
                                showContextMenu = true
                            }
                        )
                    }
                }
            }
        }
    }

    // App context menu
    if (showContextMenu && selectedApp != null) {
        val app = selectedApp!!
        val hiddenApps by viewModel.hiddenApps.collectAsState()
        val isHidden = hiddenApps.any { it.getKey() == app.getKey() }

        AlertDialog(
            onDismissRequest = {
                showContextMenu = false
                selectedApp = null
            },
            title = { Text(app.appLabel) },
            text = {
                Column {
                    // App actions
                    ContextMenuItem(
                        text = "Open App",
                        icon = Icons.Default.Info
                    ) {
                        onAppClick(app)
                        showContextMenu = false
                    }

                    ContextMenuItem(
                        text = if (isHidden) "Unhide App" else "Hide App",
                        icon = Icons.Default.Settings
                    ) {
                        viewModel.toggleAppHidden(app)
                        showContextMenu = false
                    }

                    ContextMenuItem(
                        text = "App Info",
                        icon = Icons.Default.Info
                    ) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", app.appPackage, null)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                        showContextMenu = false
                    }

                    ContextMenuItem(
                        text = "Add to Home Screen",
                        icon = Icons.Default.Add
                    ) {
                        coroutineScope.launch {
                            val prefs = viewModel.prefsDataStore.preferences.first()
                            for (i in 0 until prefs.homeAppsNum) {
                                val homeApp = prefs.homeApps[i]
                                if (homeApp.packageName.isEmpty()) {
                                    viewModel.selectedApp(app, Constants.FLAG_SET_HOME_APP_1 + i)
                                    break
                                }
                            }
                        }
                        showContextMenu = false
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showContextMenu = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppListItem(
    app: AppModel,
    showAppIcon: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showAppIcon && app.appIcon != null) {
            androidx.compose.foundation.Image(
                bitmap = app.appIcon,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 16.dp)
            )
        }

        Text(
            text = app.appLabel,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ContextMenuItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(end = 16.dp)
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}