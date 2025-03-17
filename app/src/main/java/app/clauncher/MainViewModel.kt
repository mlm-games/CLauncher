package app.clauncher

import android.app.AppOpsManager
import android.app.Application
import android.app.Service.USAGE_STATS_SERVICE
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.clauncher.data.AppModel
import app.clauncher.data.Constants
import app.clauncher.data.Prefs
import app.clauncher.helper.SingleLiveEvent
import app.clauncher.helper.getAppsList
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar


class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext by lazy { application.applicationContext }
    private val prefs by lazy { Prefs(appContext) }
    private val launcherApps by lazy { appContext.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps }
    //private val usageStatsManager by lazy { appContext.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager }

    val firstOpen = MutableLiveData<Boolean>()
    val refreshHome = MutableLiveData<Boolean>()
    val toggleDateTime = MutableLiveData<Unit>()
    val updateSwipeApps = MutableLiveData<Any>()
    val appList = MutableLiveData<List<AppModel>?>()
    val hiddenApps = MutableLiveData<List<AppModel>?>()
    val launcherResetFailed = MutableLiveData<Boolean>()
    val homeAppAlignment = MutableLiveData<Int>()
    val screenTimeValue = MutableLiveData<String>()

    val showDialog = SingleLiveEvent<String>()
    val checkForMessages = SingleLiveEvent<Unit?>()
    val resetLauncherLiveData = SingleLiveEvent<Unit?>()

    // View state management?
    sealed class ViewState {
        data class Success(val data: Any) : ViewState()
        data class Error(val message: String) : ViewState()
    }

    private val _viewState = MutableLiveData<ViewState>()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        _viewState.postValue(ViewState.Error(exception.message ?: "An error occurred"))
    }

    fun selectedApp(appModel: AppModel, flag: Int) {
        when (flag) {
            Constants.FLAG_LAUNCH_APP -> {
                launchApp(appModel)
            }

            Constants.FLAG_HIDDEN_APPS -> {
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

    fun firstOpen(value: Boolean) {
        firstOpen.postValue(value)
    }

    fun refreshHome(appCountUpdated: Boolean) {
        refreshHome.value = appCountUpdated
    }

    fun toggleDateTime() {
        toggleDateTime.postValue(Unit)
    }

    fun updateSwipeApps() {
        updateSwipeApps.postValue(Unit)
    }

    fun launchApp(appModel: AppModel) {
        viewModelScope.launch(coroutineExceptionHandler) {
            try {
                val component = ComponentName(appModel.appPackage, appModel.activityClassName ?: "")
                launcherApps.startMainActivity(component, appModel.user, null, null)
            } catch (e: SecurityException) {
                //handleSecurityException(e, appModel)
            } catch (e: Exception) {
                _viewState.postValue(ViewState.Error("Unable to launch ${appModel.appLabel}"))
            }
        }
    }

    fun getAppList(includeHiddenApps: Boolean = false) {
        viewModelScope.launch {
            appList.value = getAppsList(appContext, prefs, includeRegularApps = true, includeHiddenApps)
        }
    }

    fun getHiddenApps() {
        viewModelScope.launch {
            hiddenApps.value = getAppsList(appContext, prefs, includeRegularApps = false, includeHiddenApps = true)
        }
    }

//    fun isCLauncherDefault() {
//        isCLauncherDefault.value = isCLauncherDefault(appContext)
//    }

//    fun resetDefaultLauncherApp(context: Context) {
//        resetDefaultLauncher(context)
//        launcherResetFailed.value = getDefaultLauncherPackage(appContext).contains(".")
//    }

//    fun setWallpaperWorker() {
//        val constraints = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.CONNECTED)
//            .build()
//        val uploadWorkRequest = PeriodicWorkRequestBuilder<WallpaperWorker>(8, TimeUnit.HOURS)
//            .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.HOURS)
//            .setConstraints(constraints)
//            .build()
//        WorkManager
//            .getInstance(appContext)
//            .enqueueUniquePeriodicWork(
//                Constants.WALLPAPER_WORKER_NAME,
//                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
//                uploadWorkRequest
//            )
//    }
//
//    fun cancelWallpaperWorker() {
//        WorkManager.getInstance(appContext).cancelUniqueWork(Constants.WALLPAPER_WORKER_NAME)
//        prefs.dailyWallpaperUrl = ""
//        prefs.dailyWallpaper = false
//    }

    fun updateHomeAlignment(gravity: Int) {
        prefs.homeAlignment = gravity
        homeAppAlignment.value = prefs.homeAlignment
    }

//    Later try to also show the top three apps used
//    fun getScreenTimeStats() {
//        viewModelScope.launch(coroutineExceptionHandler) {
//            if (!LauncherUtils.hasUsageStatsPermission(appContext)) {
//                _viewState.postValue(ViewState.Error("Usage stats permission required"))
//                return@launch
//            }
//
//            getUsageStats()
//            _viewState.postValue(ViewState.Success("Hi"))
//        }
//    }
//
//    private suspend fun getUsageStats() = withContext(Dispatchers.IO) {
//        val calendar = Calendar.getInstance().apply {
//            set(Calendar.HOUR_OF_DAY, 0)
//            set(Calendar.MINUTE, 0)
//            set(Calendar.SECOND, 0)
//        }
//
//        usageStatsManager.queryUsageStats(
//            UsageStatsManager.INTERVAL_DAILY,
//            calendar.timeInMillis,
//            System.currentTimeMillis()
//        )
//    }

    // Add lifecycle management
    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

}

//object LauncherUtils {

//    private const val MINUTE_IN_MILLIS = 60_000L
//    private const val HOUR_IN_MILLIS = 3_600_000L
//
//    fun formatScreenTime(timeInMillis: Long): String = when {
//        timeInMillis < MINUTE_IN_MILLIS -> "Less than a minute"
//        timeInMillis < HOUR_IN_MILLIS -> "${timeInMillis / MINUTE_IN_MILLIS}m"
//        else -> "${timeInMillis / HOUR_IN_MILLIS}h ${(timeInMillis % HOUR_IN_MILLIS) / MINUTE_IN_MILLIS}m"
//    }

    //    fun hasRequiredPermissions(context: Context): Boolean {
//        return hasUsageStatsPermission(context) &&
//                hasAccessibilityPermission(context)
//    }

//    fun hasUsageStatsPermission(context: Context): Boolean {
//        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
//
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            appOps.unsafeCheckOpNoThrow(
//                AppOpsManager.OPSTR_GET_USAGE_STATS,
//                android.os.Process.myUid(),
//                context.packageName
//            ) == AppOpsManager.MODE_ALLOWED
//        } else {
//            appOps.checkOpNoThrow(
//                AppOpsManager.OPSTR_GET_USAGE_STATS,
//                android.os.Process.myUid(),
//                context.packageName
//            ) == AppOpsManager.MODE_ALLOWED
//        }
//    }
//}
