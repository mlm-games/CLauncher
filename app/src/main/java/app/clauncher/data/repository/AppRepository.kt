package app.clauncher.data.repository

import android.content.Context
import android.content.ComponentName
import android.content.pm.LauncherApps
import android.os.UserManager
import app.clauncher.data.AppModel
import app.clauncher.data.PrefsDataStore
import app.clauncher.helper.IconCache
import app.clauncher.helper.getAppsList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Repository for app-related operations
 */
class AppRepository(
    private val context: Context,
    private val prefs: PrefsDataStore
) {
    private val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as
                LauncherApps
    private val userManager =
        context.getSystemService(Context.USER_SERVICE) as UserManager
    private val iconCache = IconCache(context)

    private val _appList = MutableStateFlow<List<AppModel>>(emptyList())
    val appList: StateFlow<List<AppModel>> = _appList

    private val _hiddenApps = MutableStateFlow<List<AppModel>>(emptyList())
    val hiddenApps: StateFlow<List<AppModel>> = _hiddenApps

    private val pageSize = 20

    /**
     * Load all visible apps
     */
    suspend fun loadApps() {
        try {
            val apps = getAppsList(context, prefs, includeRegularApps = true, includeHiddenApps = false)
            // Debug logging
            println("Loaded ${apps.size} apps")
            _appList.value = apps
        } catch (e: Exception) {
            println("Error loading apps: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Load apps with icons
     *
     * @param loadIcons Whether to load app icons
     */
    suspend fun loadAppsWithIcons(loadIcons: Boolean = true): List<AppModel> {
        val apps = getAppsList(context, prefs, includeRegularApps =
            true, includeHiddenApps = false)

        if (loadIcons) {
            return apps.map { app ->
                val icon = iconCache.getIcon(app.appPackage,
                    app.activityClassName, app.user)
                app.copy(appIcon = icon)
            }
        }

        return apps
    }

    /**
     * Load hidden apps
     */
    suspend fun loadHiddenApps() {
        val hiddenApps = getAppsList(context, prefs,
            includeRegularApps = false, includeHiddenApps = true)
        _hiddenApps.value = hiddenApps
    }

    /**
     * Toggle app hidden state
     *
     * @param app The app to toggle
     */
    suspend fun toggleAppHidden(app: AppModel) {
        try {
            val currentHiddenApps = safeGetHiddenApps().toMutableSet()
            val appKey = "${app.appPackage}/${app.user}"

            if (currentHiddenApps.contains(appKey)) {
                currentHiddenApps.remove(appKey)
            } else {
                currentHiddenApps.add(appKey)
            }

            prefs.setHiddenApps(currentHiddenApps)
            prefs.setHiddenAppsUpdated(true)

            // Refresh lists
            loadApps()
            loadHiddenApps()
        } catch (e: Exception) {
            println("Error toggling hidden app state: ${e.message}")
            e.printStackTrace()
        }
    }

    private suspend fun safeGetHiddenApps(): Set<String> {
        return try {
            prefs.hiddenApps.first()
        } catch (e: Exception) {
            println("Error accessing hidden apps: ${e.message}")
            e.printStackTrace()
            emptySet()
        }
    }

// Then use this function in loadApps, loadHiddenApps, etc.

    /**
     * Launch an app
     *
     * @param appModel The app to launch
     * @throws AppLaunchException if launch fails
     */
    suspend fun launchApp(appModel: AppModel) {
        withContext(Dispatchers.Main) {
            try {
                val component = ComponentName(
                    appModel.appPackage,
                    appModel.activityClassName ?: ""
                )
                launcherApps.startMainActivity(component,
                    appModel.user, null, null)
            } catch (e: SecurityException) {
                throw AppLaunchException("Security error launching ${appModel.appLabel}", e)
            } catch (e: NullPointerException) {
                throw AppLaunchException("App component not found for ${appModel.appLabel}", e)
            } catch (e: Exception) {
                throw AppLaunchException("Failed to launch ${appModel.appLabel}", e)
            }
        }
    }

    /**
     * Load a page of apps for pagination
     *
     * @param page The page number (0-based)
     */
    suspend fun loadAppsPaginated(page: Int): List<AppModel> {
        return withContext(Dispatchers.IO) {
            val allApps = getAppsList(context, prefs,
                includeRegularApps = true, includeHiddenApps = false)
            val startIndex = page * pageSize
            val endIndex = minOf(startIndex + pageSize, allApps.size)

            if (startIndex < allApps.size) {
                allApps.subList(startIndex, endIndex)
            } else {
                emptyList()
            }
        }
    }

    /**
     * Search apps by query
     *
     * @param query The search query
     */
    suspend fun searchApps(query: String): List<AppModel> {
        return withContext(Dispatchers.IO) {
            val allApps = getAppsList(context, prefs,
                includeRegularApps = true, includeHiddenApps = false)
            allApps.filter { it.appLabel.contains(query, ignoreCase = true) }
        }
    }

    /**
     * Clear app cache
     */
    fun clearCache() {
        iconCache.clearCache()
    }

    /**
     * Exception for app launch failures
     */
    class AppLaunchException(message: String, cause: Throwable? =
        null) : Exception(message, cause)
}