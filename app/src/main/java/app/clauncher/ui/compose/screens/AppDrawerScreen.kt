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
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.clauncher.MainViewModel
import app.clauncher.data.AppModel
import app.clauncher.data.Constants
import app.clauncher.data.Prefs
import app.clauncher.helper.openSearch
import app.clauncher.ui.compose.AppDrawerSearch
import kotlinx.coroutines.delay
import androidx.core.net.toUri

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawerScreen(
    viewModel: MainViewModel,
    onAppClick: (AppModel) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    val appList by viewModel.appList.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var filteredApps by remember { mutableStateOf(appList) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Selected app for context menu
    var selectedApp by remember { mutableStateOf<AppModel?>(null) }
    var showContextMenu by remember { mutableStateOf(false) }

    // Load apps when screen is shown
    LaunchedEffect(Unit) {
        viewModel.loadApps()
    }

    // Update filtered apps when search query changes
    LaunchedEffect(searchQuery, appList) {
        filteredApps = if (searchQuery.isEmpty()) {
            appList
        } else {
            appList.filter {
                it.appLabel.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Auto-focus search field and show keyboard
    LaunchedEffect(Unit) {
        if (prefs.autoShowKeyboard) {
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

        // Show loading indicator if app list is empty and not filtered
        if (appList.isEmpty() && searchQuery.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (filteredApps.isEmpty() && searchQuery.isNotEmpty()) {
            // Show "no results" message for empty search results
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No apps found matching \"$searchQuery\"")

                    Button(
                        onClick = {
                            if (searchQuery.startsWith("!")) {
                                val searchUrl = Constants.URL_DUCK_SEARCH + searchQuery.substring(1).replace(" ", "%20")
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
        } else {
            // Show app list
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(
                    items = filteredApps,
                    key = { app -> app.getKey() }
                ) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    // If there's only one app in search results and user presses enter,
                                    // launch that app directly
                                    if (filteredApps.size == 1 && searchQuery.isNotEmpty()) {
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
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (prefs.toggleAppVisibility && app.appIcon != null) {
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
            }
        }
    }

    // App context menu
    if (showContextMenu && selectedApp != null) {
        val app = selectedApp!!
        val isHidden = viewModel.hiddenApps.collectAsState().value.any { it.getKey() == app.getKey() }

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
                        icon = Icons.Default.Create //TODO: if (isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff
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
                        // Add to home screen logic
                        // Find an empty slot
                        for (i in 1..prefs.homeAppsNum) {
                            val isSlotEmpty = when (i) {
                                1 -> prefs.appPackage1.isNullOrEmpty()
                                2 -> prefs.appPackage2.isNullOrEmpty()
                                3 -> prefs.appPackage3.isNullOrEmpty()
                                4 -> prefs.appPackage4.isNullOrEmpty()
                                5 -> prefs.appPackage5.isNullOrEmpty()
                                6 -> prefs.appPackage6.isNullOrEmpty()
                                7 -> prefs.appPackage7.isNullOrEmpty()
                                8 -> prefs.appPackage8.isNullOrEmpty()
                                else -> false
                            }

                            if (isSlotEmpty) {
                                viewModel.selectedApp(app, i) // Use position as flag
                                break
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