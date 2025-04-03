package app.clauncher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.clauncher.data.AppModel
import app.clauncher.data.Constants
import app.clauncher.data.PrefsDataStore // Import PrefsDataStore
import app.clauncher.data.repository.AppRepository
import app.clauncher.helper.PermissionManager
import app.clauncher.helper.getUserHandleFromString
import app.clauncher.ui.events.EventsManager
import app.clauncher.ui.state.AppDrawerScreenState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import app.clauncher.ui.state.HomeScreenState
import app.clauncher.ui.state.SettingsScreenState
import kotlinx.coroutines.runBlocking

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
    internal val prefsDataStore by lazy { PrefsDataStore(appContext) }
    private val appRepository by lazy { AppRepository(appContext, prefsDataStore) }
    private val permissionManager by lazy { PermissionManager(appContext) }

    private val _homeScreenState = MutableStateFlow(HomeScreenState())
    val homeScreenState: StateFlow<HomeScreenState> = _homeScreenState

    private val _appDrawerScreenState = MutableStateFlow(AppDrawerScreenState())
    val appDrawerScreenState: StateFlow<AppDrawerScreenState> = _appDrawerScreenState

    private val _settingsScreenState = MutableStateFlow(SettingsScreenState())
    val settingsScreenState: StateFlow<SettingsScreenState> = _settingsScreenState


    // State flows for reactive UI updates
    private val _homeAppsNum = MutableStateFlow(0) // Default value, will be updated from DataStore
    val homeAppsNum: StateFlow<Int> = _homeAppsNum

    private val _dateTimeVisibility = MutableStateFlow(0) // Default value, will be updated from DataStore
    val dateTimeVisibility: StateFlow<Int> = _dateTimeVisibility

    private val _homeAlignment = MutableStateFlow(0) // Default value, will be updated from DataStore
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

    // Events system
    internal val _eventsManager = EventsManager()
    val events = _eventsManager.events

    // Single event triggers REMOVE
    // val showDialog = SingleLiveEvent<String>() REMOVE
    // val checkForMessages = SingleLiveEvent<Unit?>() REMOVE
    // val resetLauncherLiveData = SingleLiveEvent<Unit?>() REMOVE
    val launcherResetFailed = MutableStateFlow(false)

    // View state for error handling
    private val _viewState =
        MutableStateFlow<ViewState>(ViewState.Success(Unit))
    val viewState: StateFlow<ViewState> = _viewState

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        _viewState.value = ViewState.Error(exception.message ?: "An error occurred")
    }

    init {
        collectPreferences()
    }

    private fun collectPreferences() {
        viewModelScope.launch {
            launch {
                prefsDataStore.homeAppsNum.collectLatest {
                    _homeAppsNum.value = it
                }
            }
            launch {
                prefsDataStore.dateTimeVisibility.collectLatest {
                    _dateTimeVisibility.value = it
                }
            }
            launch {
                prefsDataStore.homeAlignment.collectLatest {
                    _homeAlignment.value = it
                }
            }
        }
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
            viewModelScope.launch {
                prefsDataStore.setHomeAppsNum(_homeAppsNum.value)
                _homeAppsNum.value = _homeAppsNum.value
            }
        }
    }

    /**
     * Toggle date and time visibility
     */
    fun toggleDateTime() {
        viewModelScope.launch {
            prefsDataStore.setDateTimeVisibility(_dateTimeVisibility.value)
            _dateTimeVisibility.value = _dateTimeVisibility.value
        }
    }

    /**
     * Update visibility of apps
     *
     * @param show Whether to show apps
     */
    fun updateShowApps(show: Boolean) {
        // prefs.toggleAppVisibility = show REMOVE
        // loadApps() // Reload apps with new visibility
    }

    /**
     * Update home screen alignment
     *
     * @param gravity The alignment gravity (START, CENTER, END)
     */
    fun updateHomeAlignment(gravity: Int) {
        viewModelScope.launch {
            prefsDataStore.setHomeAlignment(gravity)
            _homeAlignment.value = gravity
        }
    }

//TODO: Fix later
//    fun isAppHidden(app: AppModel): Boolean {
//        val appKey = "${app.appPackage}/${app.user}"
//        return runBlocking { prefsDataStore.hiddenApps.collectLatest {
//            it.contains(appKey)
//        } }
//    }

    /**
     * Handle selected app action based on the flag
     *
     * @param appModel The selected app
     * @param flag The action flag (launch, hide, set as home app, set as clock app, etc.)
     */
    fun selectedApp(appModel: AppModel, flag: Int) {
        when (flag) {
            Constants.FLAG_LAUNCH_APP, Constants.FLAG_HIDDEN_APPS -> {
                launchApp(appModel)
            }
            Constants.FLAG_SET_HOME_APP_1 -> {
                viewModelScope.launch {
                    prefsDataStore.setAppName1(appModel.appLabel)
                    prefsDataStore.setAppPackage1(appModel.appPackage)
                    prefsDataStore.setAppUser1(appModel.user.toString())
                    prefsDataStore.setAppActivityClassName1(appModel.activityClassName)
                    refreshHome(false)
                }
            }
            Constants.FLAG_SET_HOME_APP_2 -> {
                viewModelScope.launch {
                    prefsDataStore.setAppName2(appModel.appLabel)
                    prefsDataStore.setAppPackage2(appModel.appPackage)
                    prefsDataStore.setAppUser2(appModel.user.toString())
                    prefsDataStore.setAppActivityClassName2(appModel.activityClassName)
                    refreshHome(false)
                }
            }
            Constants.FLAG_SET_HOME_APP_3 -> {
                viewModelScope.launch {
                    prefsDataStore.setAppName3(appModel.appLabel)
                    prefsDataStore.setAppPackage3(appModel.appPackage)
                    prefsDataStore.setAppUser3(appModel.user.toString())
                    prefsDataStore.setAppActivityClassName3(appModel.activityClassName)
                    refreshHome(false)
                }
            }
            Constants.FLAG_SET_HOME_APP_4 -> {
                viewModelScope.launch {
                    prefsDataStore.setAppName4(appModel.appLabel)
                    prefsDataStore.setAppPackage4(appModel.appPackage)
                    prefsDataStore.setAppUser4(appModel.user.toString())
                    prefsDataStore.setAppActivityClassName4(appModel.activityClassName)
                    refreshHome(false)
                }
            }
            Constants.FLAG_SET_HOME_APP_5 -> {
                viewModelScope.launch {
                    prefsDataStore.setAppName5(appModel.appLabel)
                    prefsDataStore.setAppPackage5(appModel.appPackage)
                    prefsDataStore.setAppUser5(appModel.user.toString())
                    prefsDataStore.setAppActivityClassName5(appModel.activityClassName)
                    refreshHome(false)
                }
            }
            Constants.FLAG_SET_HOME_APP_6 -> {
                viewModelScope.launch {
                    prefsDataStore.setAppName6(appModel.appLabel)
                    prefsDataStore.setAppPackage6(appModel.appPackage)
                    prefsDataStore.setAppUser6(appModel.user.toString())
                    prefsDataStore.setAppActivityClassName6(appModel.activityClassName)
                    refreshHome(false)
                }
            }
            Constants.FLAG_SET_HOME_APP_7 -> {
                viewModelScope.launch {
                    prefsDataStore.setAppName7(appModel.appLabel)
                    prefsDataStore.setAppPackage7(appModel.appPackage)
                    prefsDataStore.setAppUser7(appModel.user.toString())
                    prefsDataStore.setAppActivityClassName7(appModel.activityClassName)
                    refreshHome(false)
                }
            }
            Constants.FLAG_SET_HOME_APP_8 -> {
                viewModelScope.launch {
                    prefsDataStore.setAppName8(appModel.appLabel)
                    prefsDataStore.setAppPackage8(appModel.appPackage)
                    prefsDataStore.setAppUser8(appModel.user.toString())
                    prefsDataStore.setAppActivityClassName8(appModel.activityClassName)
                    refreshHome(false)
                }
            }
            Constants.FLAG_SET_SWIPE_LEFT_APP -> {
                viewModelScope.launch {
                    prefsDataStore.setAppNameSwipeLeft(appModel.appLabel)
                    prefsDataStore.setAppPackageSwipeLeft(appModel.appPackage)
                    prefsDataStore.setAppUserSwipeLeft(appModel.user.toString())
                    prefsDataStore.setAppActivityClassNameSwipeLeft(appModel.activityClassName)
                    updateSwipeApps()
                }
            }
            Constants.FLAG_SET_SWIPE_RIGHT_APP -> {
                viewModelScope.launch {
                    prefsDataStore.setAppNameSwipeRight(appModel.appLabel)
                    prefsDataStore.setAppPackageSwipeRight(appModel.appPackage)
                    prefsDataStore.setAppUserSwipeRight(appModel.user.toString())
                    prefsDataStore.setAppActivityClassNameSwipeRight(appModel.activityClassName)
                    updateSwipeApps()
                }
            }
            Constants.FLAG_SET_CLOCK_APP -> {
                viewModelScope.launch {
                    prefsDataStore.setClockAppPackage(appModel.appPackage)
                    prefsDataStore.setClockAppUser(appModel.user.toString())
                    prefsDataStore.setClockAppClassName(appModel.activityClassName)
                }
            }
            Constants.FLAG_SET_CALENDAR_APP -> {
                viewModelScope.launch {
                    prefsDataStore.setCalendarAppPackage(appModel.appPackage)
                    prefsDataStore.setCalendarAppUser(appModel.user.toString())
                    prefsDataStore.setCalendarAppClassName(appModel.activityClassName)
                }
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
        viewModelScope.launch {
            prefsDataStore.appPackageSwipeLeft.collectLatest {
                if (it.isNotEmpty()) {
                    prefsDataStore.appNameSwipeLeft.collectLatest { appName ->
                        prefsDataStore.appPackageSwipeLeft.collectLatest { packageName ->
                            prefsDataStore.appUserSwipeLeft.collectLatest { userString ->
                                val userHandle = getUserHandleFromString(appContext, userString)
                                val app = AppModel(
                                    appLabel = appName,
                                    key = null,
                                    appPackage = packageName,
                                    activityClassName = runBlocking { prefsDataStore.appActivityClassNameSwipeLeft.collectLatest { it } }.toString(),
                                    user = userHandle
                                )
                                launchApp(app)
                            }
                        }
                    }
                }
            }
        }
    }


    fun launchSwipeRightApp() {
        viewModelScope.launch {
            prefsDataStore.appPackageSwipeRight.collectLatest {
                if (it.isNotEmpty()) {
                    prefsDataStore.appNameSwipeRight.collectLatest { appName ->
                        prefsDataStore.appPackageSwipeRight.collectLatest { packageName ->
                            prefsDataStore.appUserSwipeRight.collectLatest { userString ->
                                val userHandle = getUserHandleFromString(appContext, userString)
                                val app = AppModel(
                                    appLabel = appName,
                                    key = null,
                                    appPackage = packageName,
                                    activityClassName = runBlocking { prefsDataStore.appActivityClassNameSwipeRight.collectLatest { it } }.toString(),
                                    user = userHandle
                                )
                                launchApp(app)
                            }
                        }
                    }
                }
            }
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
// TODO       appRepository.toggleAppHidden(app)
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
        var appName : String? = null
        var appPackage : String? = null
        var activityClassName : String? = null
        var userString : String? = null

        runBlocking {
            when (position) {
                1 -> {
                    prefsDataStore.appName1.collectLatest { appName = it }
                    prefsDataStore.appPackage1.collectLatest { appPackage = it }
                    prefsDataStore.appActivityClassName1.collectLatest { activityClassName = it }
                    prefsDataStore.appUser1.collectLatest { userString = it }
                }
                2 -> {
                    prefsDataStore.appName2.collectLatest { appName = it }
                    prefsDataStore.appPackage2.collectLatest { appPackage = it }
                    prefsDataStore.appActivityClassName2.collectLatest { activityClassName = it }
                    prefsDataStore.appUser2.collectLatest { userString = it }
                }
                3 -> {
                    prefsDataStore.appName3.collectLatest { appName = it }
                    prefsDataStore.appPackage3.collectLatest { appPackage = it }
                    prefsDataStore.appActivityClassName3.collectLatest { activityClassName = it }
                    prefsDataStore.appUser3.collectLatest { userString = it }
                }
                4 -> {
                    prefsDataStore.appName4.collectLatest { appName = it }
                    prefsDataStore.appPackage4.collectLatest { appPackage = it }
                    prefsDataStore.appActivityClassName4.collectLatest { activityClassName = it }
                    prefsDataStore.appUser4.collectLatest { userString = it }
                }
                5 -> {
                    prefsDataStore.appName5.collectLatest { appName = it }
                    prefsDataStore.appPackage5.collectLatest { appPackage = it }
                    prefsDataStore.appActivityClassName5.collectLatest { activityClassName = it }
                    prefsDataStore.appUser5.collectLatest { userString = it }
                }
                6 -> {
                    prefsDataStore.appName6.collectLatest { appName = it }
                    prefsDataStore.appPackage6.collectLatest { appPackage = it }
                    prefsDataStore.appActivityClassName6.collectLatest { activityClassName = it }
                    prefsDataStore.appUser6.collectLatest { userString = it }
                }
                7 -> {
                    prefsDataStore.appName7.collectLatest { appName = it }
                    prefsDataStore.appPackage7.collectLatest { appPackage = it }
                    prefsDataStore.appActivityClassName7.collectLatest { activityClassName = it }
                    prefsDataStore.appUser7.collectLatest { userString = it }
                }
                8 -> {
                    prefsDataStore.appName8.collectLatest { appName = it }
                    prefsDataStore.appPackage8.collectLatest { appPackage = it }
                    prefsDataStore.appActivityClassName8.collectLatest { activityClassName = it }
                    prefsDataStore.appUser8.collectLatest { userString = it }
                }
                else -> {
                    appName = null
                    appPackage = null
                    activityClassName = null
                    userString = null
                }
            }
        }

        appName ?: return null
        appPackage ?: return null
        userString ?: return null

        val userHandle = getUserHandleFromString(appContext, userString)

        return AppModel(
            appLabel = appName!!,
            key = null,
            appPackage = appPackage!!,
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
        viewModelScope.launch {
            prefsDataStore.clockAppPackage.collectLatest { clockAppPackage ->
                if (clockAppPackage.isNotEmpty()) {
                    prefsDataStore.clockAppUser.collectLatest { clockAppUser ->
                        val userHandle = getUserHandleFromString(appContext, clockAppUser)
                        val app = AppModel(
                            appLabel = "Clock",
                            key = null,
                            appPackage = clockAppPackage,
                            activityClassName = runBlocking { prefsDataStore.clockAppClassName.collectLatest { it } }.toString(),
                            user = userHandle
                        )
                        launchApp(app)
                    }
                }
            }
        }
    }

    /**
     * Open the configured calendar app
     */
    fun openCalendarApp() {
        viewModelScope.launch {
            prefsDataStore.calendarAppPackage.collectLatest { calendarAppPackage ->
                if (calendarAppPackage.isNotEmpty()) {
                    prefsDataStore.calendarAppUser.collectLatest { calendarAppUser ->
                        val userHandle = getUserHandleFromString(appContext, calendarAppUser)
                        val app = AppModel(
                            appLabel = "Calendar",
                            key = null,
                            appPackage = calendarAppPackage,
                            activityClassName = runBlocking { prefsDataStore.calendarAppClassName.collectLatest { it } }.toString(),
                            user = userHandle
                        )
                        launchApp(app)
                    }
                }
            }
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
            val needsRefresh = false // TODO Update logic
            /*val needsRefresh = arrayOf(
                prefs.appPackage1, prefs.appPackage2,
                prefs.appPackage3, prefs.appPackage4,
                prefs.appPackage5, prefs.appPackage6,
                prefs.appPackage7, prefs.appPackage8,
                prefs.appPackageSwipeLeft, prefs.appPackageSwipeRight,
                prefs.clockAppPackage, prefs.calendarAppPackage
            ).any { it == packageName }*/

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