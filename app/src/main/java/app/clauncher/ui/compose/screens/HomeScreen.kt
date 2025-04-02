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
import app.clauncher.data.AppModel
import app.clauncher.data.Constants
import app.clauncher.helper.expandNotificationDrawer
import app.clauncher.helper.getUserHandleFromString
import app.clauncher.helper.openCameraApp
import app.clauncher.helper.openDialerApp
import app.clauncher.helper.openSearch
import app.clauncher.ui.compose.util.detectSwipeGestures
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToAppDrawer: () -> Unit,
    onNavigateToSettings: () -> Unit
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


    Box(
        modifier = Modifier
            .fillMaxSize()
            .detectSwipeGestures(
                onSwipeUp = { onNavigateToAppDrawer() },
                onSwipeDown = {
                    if (prefs.swipeDownAction == Constants.SwipeDownAction.NOTIFICATIONS) {
                        expandNotificationDrawer(context)
                    } else {
                        openSearch(context)
                    }
                },
                onSwipeLeft = {
                    if (prefs.swipeLeftEnabled) {
                        viewModel.launchSwipeLeftApp()
                    }
                },
                onSwipeRight = {
                    if (prefs.swipeRightEnabled) {
                        // Implement right swipe app launch
                        // Similar to launchSwipeLeftApp() in ViewModel
                    }
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
                                        onTap = {
                                            val clockAppModel = AppModel(
                                                appLabel = "Clock",
                                                key = null,
                                                appPackage = prefs.clockAppPackage ?: "",
                                                activityClassName = prefs.clockAppClassName,
                                                user = getUserHandleFromString(context, prefs.clockAppUser ?: "")
                                            )
                                            viewModel.launchApp(clockAppModel)
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
            val appName = when (i) {
                1 -> prefs.appName1
                2 -> prefs.appName2
                3 -> prefs.appName3
                4 -> prefs.appName4
                5 -> prefs.appName5
                6 -> prefs.appName6
                7 -> prefs.appName7
                8 -> prefs.appName8
                else -> ""
            }

            val appPackage = when (i) {
                1 -> prefs.appPackage1
                2 -> prefs.appPackage2
                3 -> prefs.appPackage3
                4 -> prefs.appPackage4
                5 -> prefs.appPackage5
                6 -> prefs.appPackage6
                7 -> prefs.appPackage7
                8 -> prefs.appPackage8
                else -> ""
            }

            val appUser = when (i) {
                1 -> prefs.appUser1
                2 -> prefs.appUser2
                3 -> prefs.appUser3
                4 -> prefs.appUser4
                5 -> prefs.appUser5
                6 -> prefs.appUser6
                7 -> prefs.appUser7
                8 -> prefs.appUser8
                else -> ""
            }

            if (!appName.isNullOrEmpty() && !appPackage.isNullOrEmpty()) {
                HomeAppItem(
                    appName = appName,
                    appPackage = appPackage,
                    appUser = appUser ?: "",
                    onClick = { viewModel.selectedApp(
                        AppModel(
                            appLabel = appName,
                            key = null,
                            appPackage = appPackage,
                            activityClassName = prefs.getAppActivityClassName(i),
                            user = getUserHandleFromString(context, appUser ?: "")
                        ),
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