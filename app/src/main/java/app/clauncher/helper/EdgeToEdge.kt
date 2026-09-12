package app.clauncher.helper

import android.app.Activity
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

fun AppCompatActivity.setupEdgeToEdge() {
    enableEdgeToEdge()
}

fun Activity.updateSystemBarAppearance(isDarkTheme: Boolean) {
    val controller = WindowCompat.getInsetsController(window, window.decorView)
    controller.isAppearanceLightStatusBars = !isDarkTheme
    controller.isAppearanceLightNavigationBars = !isDarkTheme
}

fun View.applySystemBarInsets(
    applyTop: Boolean = true,
    applyBottom: Boolean = true,
    applyLeft: Boolean = true,
    applyRight: Boolean = true,
    consumeIme: Boolean = false,
) {
    val initialLeft = paddingLeft
    val initialTop = paddingTop
    val initialRight = paddingRight
    val initialBottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        val ime = if (consumeIme) {
            insets.getInsets(WindowInsetsCompat.Type.ime())
        } else null
        val bottomInset = if (ime != null) maxOf(bars.bottom, ime.bottom) else bars.bottom
        v.setPadding(
            initialLeft + if (applyLeft) bars.left else 0,
            initialTop + if (applyTop) bars.top else 0,
            initialRight + if (applyRight) bars.right else 0,
            initialBottom + if (applyBottom) bottomInset else 0,
        )
        insets
    }
    ViewCompat.requestApplyInsets(this)
}

fun Activity.setStatusBarVisibleCompat(visible: Boolean) {
    val controller = WindowCompat.getInsetsController(window, window.decorView)
    // Don't touch navigation behavior, only show/hide status bars per user pref.
    controller.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
    if (visible) controller.show(WindowInsetsCompat.Type.statusBars())
    else controller.hide(WindowInsetsCompat.Type.statusBars())
}
