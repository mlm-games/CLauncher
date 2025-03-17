package app.clauncher.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import app.clauncher.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import app.clauncher.data.AppModel

@Composable
fun AppDrawerScreen(
    viewModel: MainViewModel = viewModel(),
    onAppClick: (AppModel) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filteredApps by remember { mutableStateOf(emptyList<AppModel>()) }

    LaunchedEffect(viewModel.appList.value) {
        // Initialize filtered apps with the full list when it changes
        filteredApps = viewModel.appList.value ?: emptyList()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        AppDrawerSearch(
            searchQuery = searchQuery,
            onSearchChanged = { query ->
                searchQuery = query
                filteredApps = viewModel.appList.value?.filter {
                    it.appLabel.contains(query, ignoreCase = true)
                } ?: emptyList()
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredApps) { app ->
                AppItem(
                    app = app,
                    onClick = onAppClick
                )
            }
        }
    }
}