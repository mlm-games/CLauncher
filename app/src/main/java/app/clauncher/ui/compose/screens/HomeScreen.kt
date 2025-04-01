package app.clauncher.ui.compose.screens

import android.view.Gravity
import androidx.compose.foundation.gestures.detectDragGestures
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
import app.clauncher.data.Constants
import app.clauncher.helper.expandNotificationDrawer
import app.clauncher.helper.openCameraApp
import app.clauncher.helper.openDialerApp
import app.clauncher.helper.openSearch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onOpenAppDrawer: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { app.clauncher.data.Prefs(context) }

    // States
    val homeAppsNum = prefs.homeAppsNum
    val showDateTime = prefs.dateTimeVisibility != Constants.DateTime.OFF
    val showTime = Constants.DateTime.isTimeVisible(prefs.dateTimeVisibility)
    val showDate = Constants.DateTime.isDateVisible(prefs.dateTimeVisibility)

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

//    LaunchedEffect(swipeState.currentValue) {
//        when (swipeState.currentValue) {
//            1 -> { // Swipe left
//                if (prefs.swipeLeftEnabled) {
//                    viewModel.launchSwipeLeftApp()
//                }
//            }
//            2 -> { // Swipe right
//                if (prefs.swipeRightEnabled) {
//                    viewModel.launchSwipeRightApp()
//                }
//            }
//        }
//        swipeState.animateTo(0)
//    }


    // Main layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // Lock phone functionality
                    },
                    onLongPress = {
                        onOpenSettings()
                    },
                    onTap = {
                        // Check for messages
                        viewModel.checkForMessages.call()
                    }
                )
            }
    ) {
        // Column for date/time and apps
        Column(
            modifier = Modifier
                .align(
                    when (prefs.homeAlignment) {
                        Gravity.START -> Alignment.CenterStart
                        Gravity.END -> Alignment.CenterEnd
                        else -> Alignment.Center
                    }
                )
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = when (prefs.homeAlignment) {
                Gravity.START -> Alignment.Start
                Gravity.END -> Alignment.End
                else -> Alignment.CenterHorizontally
            },
            verticalArrangement = if (prefs.homeBottomAlignment)
                Arrangement.Bottom else Arrangement.Center
        ) {
            // Date and time section
            if (showDateTime) {
                Column(
                    horizontalAlignment = when (prefs.homeAlignment) {
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
                                        onTap = { TODO("viewModel.openClockApp(context)") },
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
                                        onTap = { TODO("viewModel.openCalendarApp(context)") },
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
                prefs = prefs,
                homeAppsNum = homeAppsNum,
                alignment = prefs.homeAlignment
            )
        }

        @Composable
        fun GestureHandler(
            onSwipeUp: () -> Unit,
            onSwipeDown: () -> Unit,
            onSwipeLeft: () -> Unit,
            onSwipeRight: () -> Unit
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val (x, y) = dragAmount
                            when {
                                abs(x) > abs(y) && x > 0 -> onSwipeRight()  // Swipe right
                                abs(x) > abs(y) && x < 0 -> onSwipeLeft()   // Swipe left
                                abs(y) > abs(x) && y > 0 -> onSwipeDown()   // Swipe down
                                abs(y) > abs(x) && y < 0 -> onSwipeUp()     // Swipe up
                            }
                        }
                    }
            )
        }
    }
}

@Composable
private fun HomeApps(
    viewModel: MainViewModel,
    prefs: app.clauncher.data.Prefs,
    homeAppsNum: Int,
    alignment: Int
) {
    Column(
        horizontalAlignment = when (alignment) {
            Gravity.START -> Alignment.Start
            Gravity.END -> Alignment.End
            else -> Alignment.CenterHorizontally
        }
    ) {
        if (homeAppsNum >= 1) {
            prefs.appName1?.let {
                prefs.appPackage1?.let { it1 ->
                    prefs.appUser1?.let { it2 ->
                        HomeAppItem(
                            appName = it,
                            appPackage = it1,
                            appUser = it2,
                            onClick = { TODO("viewModel.launchHomeApp(1)") },
                            onLongClick = { /* Show app selection */ },
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

        // Add similar blocks for apps 2-8, checking homeAppsNum
        // ...
    }
}

@Composable
private fun HomeAppItem(
    appName: String,
    appPackage: String,
    appUser: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    textAlign: TextAlign
) {
    val context = LocalContext.current
    val isInstalled = remember(appPackage, appUser) {
        app.clauncher.helper.isPackageInstalled(context, appPackage, appUser)
    }

    if (isInstalled && appName.isNotEmpty()) {
        Text(
            text = appName,
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

@Composable
private fun GestureHandler(
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    // Implement gesture detection for the entire screen
    // Note: This would need a custom implementation or a library like accompanist
}