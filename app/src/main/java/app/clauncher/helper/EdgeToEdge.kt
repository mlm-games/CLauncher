package app.clauncher.helper

import android.app.Activity
import android.view.View
import android.view.ViewGroup
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
) {
    val initialLeft = paddingLeft
    val initialTop = paddingTop
    val initialRight = paddingRight
    val initialBottom = paddingBottom
    onSystemBarInsets { v, bars ->
        v.setPadding(
            if (applyLeft) maxOf(initialLeft, bars.left) else initialLeft,
            if (applyTop) maxOf(initialTop, bars.top) else initialTop,
            if (applyRight) maxOf(initialRight, bars.right) else initialRight,
            if (applyBottom) maxOf(initialBottom, bars.bottom) else initialBottom,
        )
    }
}

fun View.applySystemBarMargins(
    applyTop: Boolean = true,
    applyBottom: Boolean = true,
    applyLeft: Boolean = true,
    applyRight: Boolean = true,
) {
    val lp = layoutParams as? ViewGroup.MarginLayoutParams ?: return
    val initialStart = lp.marginStart
    val initialTop = lp.topMargin
    val initialEnd = lp.marginEnd
    val initialBottom = lp.bottomMargin
    onSystemBarInsets { v, bars ->
        val params = v.layoutParams as? ViewGroup.MarginLayoutParams ?: return@onSystemBarInsets
        if (applyLeft) params.marginStart = maxOf(initialStart, bars.left)
        if (applyTop) params.topMargin = maxOf(initialTop, bars.top)
        if (applyRight) params.marginEnd = maxOf(initialEnd, bars.right)
        if (applyBottom) params.bottomMargin = maxOf(initialBottom, bars.bottom)
        v.layoutParams = params
    }
}

private fun View.onSystemBarInsets(action: (View, androidx.core.graphics.Insets) -> Unit) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        action(
            v,
            insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
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
