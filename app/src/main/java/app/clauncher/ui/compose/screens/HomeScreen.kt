package app.clauncher.ui.compose.screens

import android.view.Gravity
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
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
import app.clauncher.helper.isPackageInstalled
import app.clauncher.helper.openAlarmApp
import app.clauncher.helper.openCalendar
import app.clauncher.ui.compose.util.detectSwipeGestures
import app.clauncher.ui.events.UiEvent
import app.clauncher.ui.state.HomeScreenUiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToAppDrawer: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.homeScreenState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

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

    // Error handling
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            viewModel.emitEvent(UiEvent.ShowToast(it))
            viewModel.clearError()
        }
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .detectSwipeGestures(
                onSwipeUp = { onNavigateToAppDrawer() },
                onSwipeDown = { expandNotificationDrawer(context) },
                onSwipeLeft = { viewModel.launchSwipeLeftApp() },
                onSwipeRight = { viewModel.launchSwipeRightApp() }
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { /* Lock phone functionality */ },
                    onLongPress = { onNavigateToSettings() },
                    onTap = { /* Check for messages */ }
                )
            }
    ) {
        // Column for date/time and apps
        Column(
            modifier = Modifier
                .align(
                    when (uiState.homeAlignment) {
                        Gravity.START -> Alignment.CenterStart
                        Gravity.END -> Alignment.CenterEnd
                        else -> Alignment.Center
                    }
                )
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = when (uiState.homeAlignment) {
                Gravity.START -> Alignment.Start
                Gravity.END -> Alignment.End
                else -> Alignment.CenterHorizontally
            },
            verticalArrangement = if (uiState.homeBottomAlignment)
                Arrangement.Bottom else Arrangement.Center
        ) {
            // Date and time section
            if (uiState.showDateTime) {
                DateTimeSection(
                    showTime = uiState.showTime,
                    showDate = uiState.showDate,
                    currentDate = currentDate.value,
                    dateText = dateText,
                    homeAlignment = uiState.homeAlignment,
                    onTimeClick = { viewModel.openClockApp() },
                    onDateClick = { viewModel.openCalendarApp() }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Home apps section
            HomeApps(
                homeAppsNum = uiState.homeAppsNum,
                homeApps = uiState.homeApps.filterNotNull(),
                alignment = uiState.homeAlignment,
                onAppClick = { app -> viewModel.launchApp(app) }
            )
        }
    }
}

@Composable
private fun DateTimeSection(
    showTime: Boolean,
    showDate: Boolean,
    currentDate: Date,
    dateText: String,
    homeAlignment: Int,
    onTimeClick: () -> Unit,
    onDateClick: () -> Unit
) {
    Column(
        horizontalAlignment = when (homeAlignment) {
            Gravity.START -> Alignment.Start
            Gravity.END -> Alignment.End
            else -> Alignment.CenterHorizontally
        }
    ) {
        if (showTime) {
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentDate),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onTimeClick() },
                        onLongPress = { /* Select clock app */ }
                    )
                }
            )
        }

        if (showDate) {
            Text(
                text = dateText,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onDateClick() },
                        onLongPress = { /* Select calendar app */ }
                    )
                }
            )
        }
    }
}

@Composable
private fun HomeApps(
    homeAppsNum: Int,
    homeApps: List<AppModel>,
    alignment: Int,
    onAppClick: (AppModel) -> Unit
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
        for (i in 0 until homeAppsNum) {
            if (i < homeApps.size) {
                val app = homeApps[i]
                val isInstalled = remember(app.appPackage, app.user) {
                    isPackageInstalled(
                        context,
                        app.appPackage,
                        app.user.toString()
                    )
                }

                if (isInstalled) {
                    Text(
                        text = app.appLabel,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = when (alignment) {
                            Gravity.START -> TextAlign.Start
                            Gravity.END -> TextAlign.End
                            else -> TextAlign.Center
                        },
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .pointerInput(app) {
                                detectTapGestures(
                                    onTap = { onAppClick(app) },
                                    onLongPress = { /* Navigate to app selection */ }
                                )
                            }
                    )
                }
            } else {
                // Empty slot
                Text(
                    text = "•••",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = when (alignment) {
                        Gravity.START -> TextAlign.Start
                        Gravity.END -> TextAlign.End
                        else -> TextAlign.Center
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}