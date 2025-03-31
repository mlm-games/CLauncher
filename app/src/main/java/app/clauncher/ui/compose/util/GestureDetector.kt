package app.clauncher.ui.compose.util
//
//import androidx.compose.foundation.gestures.detectDragGestures
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.input.pointer.pointerInput
//import kotlin.math.abs
//
///**
// * Adds swipe gesture detection to a composable
// */
//fun Modifier.detectSwipeGestures(
//    onSwipeUp: () -> Unit = {},
//    onSwipeDown: () -> Unit = {},
//    onSwipeLeft: () -> Unit = {},
//    onSwipeRight: () -> Unit = {},
//    onTap: () -> Unit = {},
//    onLongPress: () -> Unit = {},
//    onDoubleTap: () -> Unit = {}
//): Modifier = this.then(
//    Modifier.pointerInput(Unit) {
//        detectDragGestures(
//            onDragEnd = { /* Handle drag end if needed */ },
//            onDragStart = { /* Handle drag start if needed */ },
//            onDrag = { change, dragAmount ->
//                change.consume()
//                val (x, y) = dragAmount
//
//                // Determine which direction has the highest magnitude
//                if (abs(x) > abs(y)) {
//                    // Horizontal swipe
//                    if (x > 0) {
//                        onSwipeRight()
//                    } else {
//                        onSwipeLeft()
//                    }
//                } else {
//                    // Vertical swipe
//                    if (y > 0) {
//                        onSwipeDown()
//                    } else {
//                        onSwipeUp()
//                    }
//                }
//            }
//        )
//    }
//)
//
///**
// * Add tap detection to a composable with customizable tap handlers
// */
//fun Modifier.detectTapGestures(
//    onTap: () -> Unit = {},
//    onDoubleTap: () -> Unit = {},
//    onLongPress: () -> Unit = {}
//): Modifier = this.then(
//    Modifier.pointerInput(Unit) {
//        androidx.compose.foundation.gestures.detectTapGestures(
//            onTap = { onTap() },
//            onDoubleTap = { onDoubleTap() },
//            onLongPress = { onLongPress() }
//        )
//    }
//)