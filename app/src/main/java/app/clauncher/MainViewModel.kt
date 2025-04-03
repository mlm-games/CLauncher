package app.clauncher

import android.app.Application
import android.content.Context
import android.os.UserHandle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.clauncher.data.AppModel
import app.clauncher.data.Constants
import app.clauncher.data.Prefs
import app.clauncher.data.repository.AppRepository
import app.clauncher.helper.PermissionManager
import app.clauncher.helper.SingleLiveEvent
import app.clauncher.helper.getUserHandleFromString
import app.clauncher.ui.state.AppDrawerScreenState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import app.clauncher.ui.state.HomeScreenState
import app.clauncher.ui.state.SettingsScreenState

/**
 * MainViewModel is the primary ViewModel for CLauncher that manages
app state and user interactions.
 *
 * It handles:
 * - Loading and filtering of installed applications
 * - Managing user preferences
 * - App launching
 * - Hidden apps functionality
 * - Home screen configuration
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext by lazy { application.applicationContext }
    private val prefs by lazy { Prefs(appContext) }
    private val appRepository by lazy { AppRepository(appContext, prefs) }
    private val permissionManager by lazy { PermissionManager(appContext) }

    private val _homeScreenState = MutableStateFlow(HomeScreenState())
    val homeScreenState: StateFlow<HomeScreenState> = _homeScreenState

    private val _appDrawerScreenState = MutableStateFlow(AppDrawerScreenState())
    val appDrawerScreenState: StateFlow<AppDrawerScreenState> = _appDrawerScreenState

    private val _settingsScreenState = MutableStateFlow(SettingsScreenState())
    val settingsScreenState: StateFlow<SettingsScreenState> = _settingsScreenState


    // State flows for reactive UI updates
    private val _homeAppsNum = MutableStateFlow(prefs.homeAppsNum)
    val homeAppsNum: StateFlow<Int> = _homeAppsNum

    private val _dateTimeVisibility = MutableStateFlow(prefs.dateTimeVisibility)
    val dateTimeVisibility: StateFlow<Int> = _dateTimeVisibility

    private val _homeAlignment = MutableStateFlow(prefs.homeAlignment)
    val homeAlignment: StateFlow<Int> = _homeAlignment

    // Use repository's StateFlow
    val appList = appRepository.appList
    val hiddenApps = appRepository.hiddenApps

    // App launching state
    private val _appLaunchState =
        MutableStateFlow<AppLaunchState>(AppLaunchState.Idle)
    val appLaunchState: StateFlow<AppLaunchState> = _appLaunchState

    // Pagination state for app list
    private val _paginatedApps = MutableStateFlow<List<AppModel>>(emptyList())
    val paginatedApps: StateFlow<List<AppModel>> = _paginatedApps

    private var currentPage = 0
    private var isLastPage = false
    private var searchJob: kotlinx.coroutines.Job? = null

    // Single event triggers
    val showDialog = SingleLiveEvent<String>()
    val checkForMessages = SingleLiveEvent<Unit?>()
    val resetLauncherLiveData = SingleLiveEvent<Unit?>()
    val launcherResetFailed = MutableStateFlow(false)

    // View state for error handling
    private val _viewState =
        MutableStateFlow<ViewState>(ViewState.Success(Unit))
    val viewState: StateFlow<ViewState> = _viewState

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        _viewState.value = ViewState.Error(exception.message ?: "An error occurred")
    }

    /**
     * Sealed class to represent the current view state
     */
    sealed class ViewState {
        data class Success(val data: Any) : ViewState()
        data class Error(val message: String) : ViewState()
    }

    /**
     * Sealed class to represent the current app launch state
     */
    sealed class AppLaunchState {
        object Idle : AppLaunchState()
        object Loading : AppLaunchState()
        object Success : AppLaunchState()
        data class Error(val message: String) : AppLaunchState()
    }

    /**
     * Handle first open of the app
     */
    fun firstOpen(value: Boolean) {
        _viewState.value = ViewState.Success(value)
    }

    /**
     * Refresh the home screen, optionally updating the app count
     *
     * @param appCountUpdated Whether the number of apps has changed
     */
    fun refreshHome(appCountUpdated: Boolean) {
        if (appCountUpdated) {
            _homeAppsNum.value = prefs.homeAppsNum
        }
    }

    /**
     * Toggle date and time visibility
     */
    fun toggleDateTime() {
        _dateTimeVisibility.value = prefs.dateTimeVisibility
    }

    /**
     * Update visibility of apps
     *
     * @param show Whether to show apps
     */
    fun updateShowApps(show: Boolean) {
        prefs.toggleAppVisibility = show
        loadApps() // Reload apps with new visibility
    }

    /**
     * Update home screen alignment
     *
     * @param gravity The alignment gravity (START, CENTER, END)
     */
    fun updateHomeAlignment(gravity: Int) {
        prefs.homeAlignment = gravity
        _homeAlignment.value = gravity
    }

    fun isAppHidden(app: AppModel): Boolean {
        val appKey = "${app.appPackage}/${app.user}"
        return prefs.hiddenApps.contains(appKey)
    }

    /**
     * Handle selected app action based on the flag
     *
     * @param appModel The selected app
     * @param flag The action flag (launch, hide, set as home app, etc.)
     */
    fun selectedApp(appModel: AppModel, flag: Int) {
        when (flag) {
            Constants.FLAG_LAUNCH_APP, Constants.FLAG_HIDDEN_APPS -> {
                launchApp(appModel)
            }
            Constants.FLAG_SET_HOME_APP_1 -> {
                prefs.appName1 = appModel.appLabel
                prefs.appPackage1 = appModel.appPackage
                prefs.appUser1 = appModel.user.toString()
                prefs.appActivityClassName1 = appModel.activityClassName
                refreshHome(false)
            }
            Constants.FLAG_SET_HOME_APP_2 -> {
                prefs.appName2 = appModel.appLabel
                prefs.appPackage2 = appModel.appPackage
                prefs.appUser2 = appModel.user.toString()
                prefs.appActivityClassName2 = appModel.activityClassName
                refreshHome(false)
            }
            Constants.FLAG_SET_HOME_APP_3 -> {
                prefs.appName3 = appModel.appLabel
                prefs.appPackage3 = appModel.appPackage
                prefs.appUser3 = appModel.user.toString()
                prefs.appActivityClassName3 = appModel.activityClassName
                refreshHome(false)
            }
            Constants.FLAG_SET_HOME_APP_4 -> {
                prefs.appName4 = appModel.appLabel
                prefs.appPackage4 = appModel.appPackage
                prefs.appUser4 = appModel.user.toString()
                prefs.appActivityClassName4 = appModel.activityClassName
                refreshHome(false)
            }
            Constants.FLAG_SET_HOME_APP_5 -> {
                prefs.appName5 = appModel.appLabel
                prefs.appPackage5 = appModel.appPackage
                prefs.appUser5 = appModel.user.toString()
                prefs.appActivityClassName5 = appModel.activityClassName
                refreshHome(false)
            }
            Constants.FLAG_SET_HOME_APP_6 -> {
                prefs.appName6 = appModel.appLabel
                prefs.appPackage6 = appModel.appPackage
                prefs.appUser6 = appModel.user.toString()
                prefs.appActivityClassName6 = appModel.activityClassName
                refreshHome(false)
            }
            Constants.FLAG_SET_HOME_APP_7 -> {
                prefs.appName7 = appModel.appLabel
                prefs.appPackage7 = appModel.appPackage
                prefs.appUser7 = appModel.user.toString()
                prefs.appActivityClassName7 = appModel.activityClassName
                refreshHome(false)
            }
            Constants.FLAG_SET_HOME_APP_8 -> {
                prefs.appName8 = appModel.appLabel
                prefs.appPackage8 = appModel.appPackage
                prefs.appUser8 = appModel.user.toString()
                prefs.appActivityClassName8 = appModel.activityClassName
                refreshHome(false)
            }
            Constants.FLAG_SET_SWIPE_LEFT_APP -> {
                prefs.appNameSwipeLeft = appModel.appLabel
                prefs.appPackageSwipeLeft = appModel.appPackage
                prefs.appUserSwipeLeft = appModel.user.toString()
                prefs.appActivityClassNameSwipeLeft = appModel.activityClassName
                updateSwipeApps()
            }
            Constants.FLAG_SET_SWIPE_RIGHT_APP -> {
                prefs.appNameSwipeRight = appModel.appLabel
                prefs.appPackageSwipeRight = appModel.appPackage
                prefs.appUserSwipeRight = appModel.user.toString()
                prefs.appActivityClassNameRight = appModel.activityClassName
                updateSwipeApps()
            }
            Constants.FLAG_SET_CLOCK_APP -> {
                prefs.clockAppPackage = appModel.appPackage
                prefs.clockAppUser = appModel.user.toString()
                prefs.clockAppClassName = appModel.activityClassName
            }
            Constants.FLAG_SET_CALENDAR_APP -> {
                prefs.calendarAppPackage = appModel.appPackage
                prefs.calendarAppUser = appModel.user.toString()
                prefs.calendarAppClassName = appModel.activityClassName
            }
        }
    }

    /**
     * Update swipe apps configuration
     */
    fun updateSwipeApps() {
        // TODO: Trigger UI update
    }

    /**
     * Launch an app
     *
     * @param appModel The app to launch
     */
    fun launchApp(appModel: AppModel) {
        viewModelScope.launch(coroutineExceptionHandler) {
            _appLaunchState.value = AppLaunchState.Loading

            try {
                appRepository.launchApp(appModel)
                _appLaunchState.value = AppLaunchState.Success
            } catch (e: Exception) {
                val errorMessage = "Failed to launch ${appModel.appLabel}: ${e.message}"
                _appLaunchState.value = AppLaunchState.Error(errorMessage)
                _viewState.value = ViewState.Error(errorMessage)
            }
        }
    }


    fun launchSwipeLeftApp() {
        if (prefs.appPackageSwipeLeft?.isNotEmpty() == true) {
            val app = prefs.appNameSwipeLeft?.let {
                prefs.appPackageSwipeLeft?.let { packageName ->
                    prefs.appUserSwipeLeft?.let { userString ->
                        getUserHandleFromString(appContext, userString)
                    }?.let { userHandle ->
                        AppModel(
                            appLabel = it,
                            key = null,
                            appPackage = packageName,
                            activityClassName =
                                prefs.appActivityClassNameSwipeLeft,
                            user = userHandle
                        )
                    }
                }
            }
            app?.let { launchApp(it) }
        }
    }


    fun launchSwipeRightApp() {
        if (prefs.appPackageSwipeRight?.isNotEmpty() == true) {
            val app = prefs.appNameSwipeRight?.let {
                prefs.appPackageSwipeRight?.let { packageName ->
                    prefs.appUserSwipeRight?.let { userString ->
                        getUserHandleFromString(appContext, userString)
                    }?.let { userHandle ->
                        AppModel(
                            appLabel = it,
                            key = null,
                            appPackage = packageName,
                            activityClassName = prefs.appActivityClassNameRight,
                            user = userHandle
                        )
                    }
                }
            }
            app?.let { launchApp(it) }
        }
    }

    /**
     * Load all apps
     */
    fun loadApps() {
        viewModelScope.launch(SupervisorJob() + coroutineExceptionHandler) {
            supervisorScope {
                try {
                    withContext(Dispatchers.Default) {
                        appRepository.loadApps()
                    }
                } catch (e: Exception) {
                    _viewState.value = ViewState.Error("Failed to load apps: ${e.message}")
                }
            }
        }
    }

    /**
     * Load hidden apps
     */
    fun getHiddenApps() {
        viewModelScope.launch(SupervisorJob() + coroutineExceptionHandler) {
            supervisorScope {
                try {
                    withContext(Dispatchers.Default) {
                        appRepository.loadHiddenApps()
                    }
                } catch (e: Exception) {
                    _viewState.value = ViewState.Error("Failed to load hidden apps: ${e.message}")
                }
            }
        }
    }

    /**
     * Toggle app hidden state
     *
     * @param app The app to toggle
     */
    fun toggleAppHidden(app: AppModel) {
        appRepository.toggleAppHidden(app)
        // Refresh lists after toggling
        loadApps()
        getHiddenApps()
    }

    /**
     * Load next page of apps (for pagination)
     */
    fun loadNextPage() {
        if (isLastPage) return

        viewModelScope.launch {
            val newApps = appRepository.loadAppsPaginated(currentPage)
            if (newApps.isEmpty()) {
                isLastPage = true
            } else {
                currentPage++
                _paginatedApps.value = _paginatedApps.value + newApps
            }
        }
    }

    /**
     * Refresh app list
     */
    fun refreshAppList() {
        currentPage = 0
        isLastPage = false
        _paginatedApps.value = emptyList()
        loadNextPage()
    }

    /**
     * Search apps by query
     *
     * @param query The search query
     */
    fun searchApps(query: String) {
        // Cancel previous search job if still running
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            // Add delay to avoid searching on every character
            delay(300)
            try {
                if (query.isBlank()) {
                    refreshAppList()
                } else {
                    val results = withContext(Dispatchers.Default) {
                        appRepository.searchApps(query)
                    }
                    _paginatedApps.value = results
                }
            } catch (e: Exception) {
                _viewState.value = ViewState.Error("Search failed:${e.message}")
            }
        }
    }

    /**
     * Check if required permissions are granted
     */
    fun checkRequiredPermissions(): Boolean {
        return permissionManager.hasUsageStatsPermission() &&
                permissionManager.hasAccessibilityPermission()
    }

    /**
     * Request usage stats permission
     */
    fun requestUsageStatsPermission() {
        permissionManager.openUsageAccessSettings()
    }

    /**
     * Request accessibility permission
     */
    fun requestAccessibilityPermission() {
        permissionManager.openAccessibilitySettings()
    }

    /**
     * Create an AppModel for a home app at the specified position
     *
     * @param position The position of the app (1-8)
     * @return The AppModel or null if not set
     */
    fun getHomeAppModel(position: Int): AppModel? {
        val appName = when (position) {
            1 -> prefs.appName1
            2 -> prefs.appName2
            3 -> prefs.appName3
            4 -> prefs.appName4
            5 -> prefs.appName5
            6 -> prefs.appName6
            7 -> prefs.appName7
            8 -> prefs.appName8
            else -> null
        } ?: return null

        val appPackage = when (position) {
            1 -> prefs.appPackage1
            2 -> prefs.appPackage2
            3 -> prefs.appPackage3
            4 -> prefs.appPackage4
            5 -> prefs.appPackage5
            6 -> prefs.appPackage6
            7 -> prefs.appPackage7
            8 -> prefs.appPackage8
            else -> null
        } ?: return null

        val activityClassName = when (position) {
            1 -> prefs.appActivityClassName1
            2 -> prefs.appActivityClassName2
            3 -> prefs.appActivityClassName3
            4 -> prefs.appActivityClassName4
            5 -> prefs.appActivityClassName5
            6 -> prefs.appActivityClassName6
            7 -> prefs.appActivityClassName7
            8 -> prefs.appActivityClassName8
            else -> null
        }

        val userString = when (position) {
            1 -> prefs.appUser1
            2 -> prefs.appUser2
            3 -> prefs.appUser3
            4 -> prefs.appUser4
            5 -> prefs.appUser5
            6 -> prefs.appUser6
            7 -> prefs.appUser7
            8 -> prefs.appUser8
            else -> null
        } ?: return null

        val userHandle = getUserHandleFromString(appContext, userString)

        return AppModel(
            appLabel = appName,
            key = null,
            appPackage = appPackage,
            activityClassName = activityClassName,
            user = userHandle
        )
    }

    /**
     * Launch a home app at the specified position
     *
     * @param position The position of the app (1-8)
     */
    fun launchHomeApp(position: Int) {
        getHomeAppModel(position)?.let { launchApp(it) }
    }

    /**
     * Open the configured clock app
     */
    fun openClockApp() {
        if (prefs.clockAppPackage?.isNotEmpty() == true) {
            val userHandle = getUserHandleFromString(appContext,
                prefs.clockAppUser ?: "")
            val app = AppModel(
                appLabel = "Clock",
                key = null,
                appPackage = prefs.clockAppPackage ?: "",
                activityClassName = prefs.clockAppClassName,
                user = userHandle
            )
            launchApp(app)
        }
    }

    /**
     * Open the configured calendar app
     */
    fun openCalendarApp() {
        if (prefs.calendarAppPackage?.isNotEmpty() == true) {
            val userHandle = getUserHandleFromString(appContext,
                prefs.calendarAppUser ?: "")
            val app = AppModel(
                appLabel = "Calendar",
                key = null,
                appPackage = prefs.calendarAppPackage ?: "",
                activityClassName = prefs.calendarAppClassName,
                user = userHandle
            )
            launchApp(app)
        }
    }

    /**
     * Handle app uninstall or update events
     *
     * @param packageName The package name of the app
     * @param isRemoved Whether the app was removed
     */
    fun handleAppEvent(packageName: String, isRemoved: Boolean) {
        viewModelScope.launch {
            // Check if this was a home app
            val needsRefresh = arrayOf(
                prefs.appPackage1, prefs.appPackage2,
                prefs.appPackage3, prefs.appPackage4,
                prefs.appPackage5, prefs.appPackage6,
                prefs.appPackage7, prefs.appPackage8,
                prefs.appPackageSwipeLeft, prefs.appPackageSwipeRight,
                prefs.clockAppPackage, prefs.calendarAppPackage
            ).any { it == packageName }

            // Reload app list
            loadApps()

            // Update home screen if needed
            if (needsRefresh) {
                refreshHome(false)
            }
        }
    }

    /**
     * Clear app cache and reload data
     */
    fun clearCache() {
        viewModelScope.launch {
            appRepository.clearCache()
            loadApps()
        }
    }

    /**
     * Clean up resources when ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        viewModelScope.cancel()
    }
}
