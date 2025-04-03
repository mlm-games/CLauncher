package app.clauncher.helper

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import app.clauncher.R
import app.clauncher.data.PrefsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyAccessibilityService : AccessibilityService() {
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onServiceConnected() {
        serviceScope.launch {
            val prefsDataStore = PrefsDataStore(applicationContext)
            prefsDataStore.updatePreference { it.copy(lockMode = true) }
        }
        super.onServiceConnected()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        try {
            val source: AccessibilityNodeInfo = event.source ?: return
            if ((source.className == "android.widget.FrameLayout") and
                (source.contentDescription == getString(R.string.lock_layout_description))
            )
                performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        } catch (e: Exception) {
            return
        }
    }

    override fun onInterrupt() {
        // Not needed
    }
}