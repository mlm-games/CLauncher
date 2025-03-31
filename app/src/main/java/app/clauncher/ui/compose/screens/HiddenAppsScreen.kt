package app.clauncher.ui.compose.screens
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import app.clauncher.MainViewModel
//import app.clauncher.ui.compose.components.AppItem
//
//@Composable
//fun HiddenAppsScreen(
//    viewModel: MainViewModel,
//    onNavigateBack: () -> Unit
//) {
//    val hiddenApps by viewModel.hiddenApps.collectAsState()
//
//    LaunchedEffect(Unit) {
//        viewModel.getHiddenApps()
//    }
//
//    Scaffold(
//        topBar = {
//            SmallTopAppBar(
//                title = { Text("Hidden Apps") },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(
//                            imageVector = Icons.Default.ArrowBack,
//                            contentDescription = "Back"
//                        )
//                    }
//                }
//            )
//        }
//    ) { paddingValues ->
//        if (hiddenApps.isNullOrEmpty()) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "No hidden apps",
//                    style = MaterialTheme.typography.bodyLarge
//                )
//            }
//        } else {
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues)
//            ) {
//                items(hiddenApps ?: emptyList()) { app ->
//                    AppItem(
//                        app = app,
//                        onClick = {
//                            //TODO: open app
//                        },
//                        onLongClick = {
//                            // Unhide app
////TODO                            viewModel.toggleAppHidden(app)
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun SmallTopAppBar(title: () -> Unit, navigationIcon: () -> Unit) {
//    TODO()
//}
