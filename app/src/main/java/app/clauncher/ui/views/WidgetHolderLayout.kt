package app.clauncher.ui.views

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import app.clauncher.data.Constants

class WidgetHolderLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var onWidgetLongPress: (() -> Unit)? = null

    private val handler = Handler(Looper.getMainLooper())
    private val slop = ViewConfiguration.get(context).scaledTouchSlop
    private var downX = 0f
    private var downY = 0f
    private var intercepting = false

    private val longPressRunnable = Runnable {
        if (!intercepting) {
            intercepting = true
            try {
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            } catch (_: Exception) {
            }
            try {
                parent?.requestDisallowInterceptTouchEvent(true)
            } catch (_: Exception) {
            }
            onWidgetLongPress?.invoke()
        }
    }

    private fun cancelPending() {
        handler.removeCallbacks(longPressRunnable)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                intercepting = false
                downX = ev.x
                downY = ev.y
                cancelPending()
                handler.postDelayed(longPressRunnable, Constants.LONG_PRESS_DELAY_MS)
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = ev.x - downX
                val dy = ev.y - downY
                if (dx * dx + dy * dy > slop * slop) {
                    cancelPending()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                cancelPending()
                intercepting = false
            }
        }
        return intercepting
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancelPending()
    }
}
