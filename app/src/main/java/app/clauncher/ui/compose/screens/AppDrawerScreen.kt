package app.clauncher.ui.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.clauncher.MainViewModel
import app.clauncher.data.AppModel
import app.clauncher.data.Constants
import app.clauncher.helper.openSearch
import app.clauncher.helper.openUrl
import app.clauncher.ui.compose.components.AppItem

@Composable
fun AppDrawerScreen(
    viewModel: MainViewModel,
    onAppClick: (AppModel) -> Unit
) {
    val context = LocalContext.current
    val appList by viewModel.appList.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var filteredApps by remember { mutableStateOf(emptyList<AppModel>()) }

    // Update filtered apps when app list or search query changes
    LaunchedEffect(appList, searchQuery) {
        filteredApps = if (searchQuery.isEmpty()) {
            appList ?: emptyList()
        } else {
            appList?.filter {
                it.appLabel.contains(searchQuery, ignoreCase = true)
            } ?: emptyList()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search bar
        AppDrawerSearch(
            searchQuery = searchQuery,
            onSearchChanged = { query ->
                searchQuery = query
            },
            onSearchSubmitted = { query ->
                if (query.startsWith("!")) {
                    // DuckDuckGo search
                    context.openUrl(Constants.URL_DUCK_SEARCH + query.replace(" ", "%20"))
                } else if (filteredApps.isEmpty()) {
                    // web search
                    openSearch(context)
                } else {
                    // Launch first app in filtered list
                    filteredApps.firstOrNull()?.let { onAppClick(it) }
                }
            }
        )

        // App list
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredApps) { app ->
                AppItem(
                    app = app,
                    onClick = { onAppClick(app) },
                    onLongClick = {
                        viewModel.toggleAppHidden(app)
                    }
                )
            }
        }
    }
}

@Composable
fun AppDrawerSearch(
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    onSearchSubmitted: (String) -> Unit = {}
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChanged,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text("Search apps...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        singleLine = true,
        keyboardActions = KeyboardActions(
            onDone = { onSearchSubmitted(searchQuery) }
        )
    )
}