package app.clauncher.ui.compose.screens

import android.view.Gravity
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.clauncher.MainViewModel
import app.clauncher.data.AppModel
import app.clauncher.data.Constants
import app.clauncher.helper.expandNotificationDrawer
import app.clauncher.helper.getUserHandleFromString
import app.clauncher.helper.openAlarmApp
import app.clauncher.helper.openCalendar
import app.clauncher.helper.openDialerApp
import app.clauncher.helper.openSearch
import app.clauncher.ui.compose.util.detectSwipeGestures
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToAppDrawer: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    // val prefs = remember { app.clauncher.data.Prefs(context) }

    // States
    val homeAppsNum by viewModel.homeAppsNum.collectAsState() // Observe from ViewModel
    val dateTimeVisibility by viewModel.dateTimeVisibility.collectAsState() // Observe from ViewModel

    val showDateTime = dateTimeVisibility != Constants.DateTime.OFF
    val showTime = Constants.DateTime.isTimeVisible(dateTimeVisibility)
    val showDate = Constants.DateTime.isDateVisible(dateTimeVisibility)

    // Home Alignment
    val homeAlignment by viewModel.homeAlignment.collectAsState()

    // Format date
    val currentDate = remember { mutableStateOf(Date()) }
    val dateFormat = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
    val dateText = dateFormat.format(currentDate.value).replace(".,", ",")

    // Time updater effect
    LaunchedEffect(key1 = Unit) {
        while(true) {
            currentDate.value = Date()
            kotlinx.coroutines.delay(60000) // Update every minute
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .detectSwipeGestures(
                onSwipeUp = { onNavigateToAppDrawer() },
                onSwipeDown = {
                    expandNotificationDrawer(context)
                },
                onSwipeLeft = {
                    viewModel.launchSwipeLeftApp()
                },
                onSwipeRight = {
                    viewModel.launchSwipeRightApp()
                }
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // Lock phone functionality
                    },
                    onLongPress = {
                        onNavigateToSettings()
                    },
                    onTap = {
                        // Check for messages
                    }
                )
            }
    ) {
        // Column for date/time and apps
        Column(
            modifier = Modifier
                .align(
                    when (homeAlignment) {
                        Gravity.START -> Alignment.CenterStart
                        Gravity.END -> Alignment.CenterEnd
                        else -> Alignment.Center
                    }
                )
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = when (homeAlignment) {
                Gravity.START -> Alignment.Start
                Gravity.END -> Alignment.End
                else -> Alignment.CenterHorizontally
            },
            verticalArrangement = /*if (prefs.homeBottomAlignment) // TODO Bottom Alignment
                Arrangement.Bottom else */ Arrangement.Center
        ) {
            // Date and time section
            if (showDateTime) {
                Column(
                    horizontalAlignment = when (homeAlignment) {
                        Gravity.START -> Alignment.Start
                        Gravity.END -> Alignment.End
                        else -> Alignment.CenterHorizontally
                    }
                ) {
                    if (showTime) {
                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault())
                                .format(currentDate.value),
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            openAlarmApp(context)
                                        },
                                        onLongPress = { /* TODO Select clock app */ }
                                    )
                                }
                        )
                    }

                    if (showDate) {
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = { openCalendar(context) },
                                        onLongPress = { /* Select calendar app */ }
                                    )
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Home apps section
            HomeApps(
                viewModel = viewModel,
                homeAppsNum = homeAppsNum,
                alignment = homeAlignment
            )
        }
    }
}

@Composable
private fun HomeApps(
    viewModel: MainViewModel,
    homeAppsNum: Int,
    alignment: Int
) {
    val context = LocalContext.current

    Column(
        horizontalAlignment = when (alignment) {
            Gravity.START -> Alignment.Start
            Gravity.END -> Alignment.End
            else -> Alignment.CenterHorizontally
        }
    ) {
        // Generate app items based on homeAppsNum
        for (i in 1..homeAppsNum) {
            val appModel = viewModel.getHomeAppModel(i)
            if (appModel != null) {
                HomeAppItem(
                    appModel = appModel,
                    onClick = { viewModel.selectedApp(
                        appModel,
                        Constants.FLAG_LAUNCH_APP
                    ) },
                    onLongClick = { /* Navigate to app selection */ },
                    textAlign = when (alignment) {
                        Gravity.START -> TextAlign.Start
                        Gravity.END -> TextAlign.End
                        else -> TextAlign.Center
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeAppItem(
    appModel: AppModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    textAlign: TextAlign
) {
    val context = LocalContext.current
    val isInstalled = remember(appModel.appPackage, appModel.user) {
        app.clauncher.helper.isPackageInstalled(context, appModel.appPackage, appModel.user.toString())
    }

    if (isInstalled ) {
        Text(
            text = appModel.appLabel,
            style = MaterialTheme.typography.titleLarge,
            textAlign = textAlign,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = { onLongClick() }
                    )
                }
        )
    }
}