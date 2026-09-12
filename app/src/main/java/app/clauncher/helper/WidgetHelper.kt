package app.clauncher.helper

import android.app.Activity
import android.app.ActivityOptions
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Build
import android.util.Log


class WidgetHelper(
    private val context: Context,
    private val appWidgetManager: AppWidgetManager,
    private val appWidgetHost: AppWidgetHost
) {
    companion object {
        private const val TAG = "WidgetHelper"
    }

    fun hasConfigurationActivity(widgetId: Int): Boolean {
        return try {
            appWidgetManager.getAppWidgetInfo(widgetId)?.configure != null
        } catch (e: Exception) {
            Log.e(TAG, "Error checking widget configuration: ${e.message}")
            false
        }
    }

    fun needsConfiguration(widgetId: Int): Boolean = hasConfigurationActivity(widgetId)

    fun isReconfigurable(widgetId: Int): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        return try {
            val info = appWidgetManager.getAppWidgetInfo(widgetId) ?: return false
            info.configure != null &&
                (info.widgetFeatures and
                    android.appwidget.AppWidgetProviderInfo.WIDGET_FEATURE_RECONFIGURABLE) != 0
        } catch (e: Exception) {
            Log.e(TAG, "Error checking reconfigurable: ${e.message}")
            false
        }
    }

    private fun buildConfigureOptions(): android.os.Bundle? {
        return if (Build.VERSION.SDK_INT >= 34) {
            try {
                ActivityOptions.makeBasic()
                    .setPendingIntentBackgroundActivityStartMode(
                        ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                    )
                    .toBundle()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to create ActivityOptions for API 34+", e)
                null
            }
        } else {
            null
        }
    }

    fun startWidgetConfiguration(
        activity: Activity,
        widgetId: Int,
        requestCode: Int
    ): Boolean {
        return try {
            val providerInfo = appWidgetManager.getAppWidgetInfo(widgetId)
            if (providerInfo?.configure == null) {
                Log.w(TAG, "Widget $widgetId has no configure activity")
                return false
            }
            appWidgetHost.startAppWidgetConfigureActivityForResult(
                activity,
                widgetId,
                0,
                requestCode,
                buildConfigureOptions()
            )
            true
        } catch (e: android.content.ActivityNotFoundException) {
            Log.e(TAG, "Configure activity not found for widget $widgetId", e)
            false
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException starting widget configuration", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start widget configuration for widget $widgetId", e)
            false
        }
    }
}
