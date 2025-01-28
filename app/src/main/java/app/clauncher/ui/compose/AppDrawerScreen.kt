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
    var filteredApps by remember { mutableStateOf(emptyList<AppModel>()) }
    
    Column {
        AppDrawerSearch(
            onSearch = { query ->
                filteredApps = viewModel.appList.value?.filter { 
                    it.appLabel.contains(query, ignoreCase = true) 
                } ?: emptyList()
            }
        )
        
        LazyColumn {
            items(filteredApps) { app ->
                AppItem(
                    app = app,
                    onClick = onAppClick
                )
            }
        }
    }
}