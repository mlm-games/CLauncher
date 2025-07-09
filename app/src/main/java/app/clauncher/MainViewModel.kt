package app.clauncher

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.clauncher.data.AppModel
import app.clauncher.data.Constants
import app.clauncher.data.Prefs
import app.clauncher.helper.SingleLiveEvent
import app.clauncher.helper.getAppsList
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


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
        data class Error(val message: String) : ViewState()
    }

    private val _viewState = MutableLiveData<ViewState>()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        _viewState.postValue(ViewState.Error(exception.message ?: "An error occurred"))
    }


    fun selectedApp(appModel: AppModel, flag: Int) {
        when (flag) {
            Constants.FLAG_LAUNCH_APP, Constants.FLAG_HIDDEN_APPS -> launchApp(appModel)
            Constants.FLAG_SET_HOME_APP_1 -> setHomeApp(appModel, 1)
            Constants.FLAG_SET_HOME_APP_2 -> setHomeApp(appModel, 2)
            Constants.FLAG_SET_HOME_APP_3 -> setHomeApp(appModel, 3)
            Constants.FLAG_SET_HOME_APP_4 -> setHomeApp(appModel, 4)
            Constants.FLAG_SET_HOME_APP_5 -> setHomeApp(appModel, 5)
            Constants.FLAG_SET_HOME_APP_6 -> setHomeApp(appModel, 6)
            Constants.FLAG_SET_HOME_APP_7 -> setHomeApp(appModel, 7)
            Constants.FLAG_SET_HOME_APP_8 -> setHomeApp(appModel, 8)
            Constants.FLAG_SET_SWIPE_LEFT_APP -> setSwipeApp(appModel, true)
            Constants.FLAG_SET_SWIPE_RIGHT_APP -> setSwipeApp(appModel, false)
            Constants.FLAG_SET_CLOCK_APP -> setClockApp(appModel)
            Constants.FLAG_SET_CALENDAR_APP -> setCalendarApp(appModel)
        }
    }

    private fun setHomeApp(appModel: AppModel, appNumber: Int) {
        when (appNumber) {
            1 -> {
                prefs.appName1 = appModel.appLabel
                prefs.appPackage1 = appModel.appPackage
                prefs.appUser1 = appModel.user.toString()
                prefs.appActivityClassName1 = appModel.activityClassName
            }
            2 -> {
                prefs.appName2 = appModel.appLabel
                prefs.appPackage2 = appModel.appPackage
                prefs.appUser2 = appModel.user.toString()
                prefs.appActivityClassName2 = appModel.activityClassName
            }
            3 -> {
                prefs.appName3 = appModel.appLabel
                prefs.appPackage3 = appModel.appPackage
                prefs.appUser3 = appModel.user.toString()
                prefs.appActivityClassName3 = appModel.activityClassName
            }
            4 -> {
                prefs.appName4 = appModel.appLabel
                prefs.appPackage4 = appModel.appPackage
                prefs.appUser4 = appModel.user.toString()
                prefs.appActivityClassName4 = appModel.activityClassName
            }
            5 -> {
                prefs.appName5 = appModel.appLabel
                prefs.appPackage5 = appModel.appPackage
                prefs.appUser5 = appModel.user.toString()
                prefs.appActivityClassName5 = appModel.activityClassName
            }
            6 -> {
                prefs.appName6 = appModel.appLabel
                prefs.appPackage6 = appModel.appPackage
                prefs.appUser6 = appModel.user.toString()
                prefs.appActivityClassName6 = appModel.activityClassName
            }
            7 -> {
                prefs.appName7 = appModel.appLabel
                prefs.appPackage7 = appModel.appPackage
                prefs.appUser7 = appModel.user.toString()
                prefs.appActivityClassName7 = appModel.activityClassName
            }
            8 -> {
                prefs.appName8 = appModel.appLabel
                prefs.appPackage8 = appModel.appPackage
                prefs.appUser8 = appModel.user.toString()
                prefs.appActivityClassName8 = appModel.activityClassName
            }
        }
        refreshHome(false)
    }

    private fun setSwipeApp(appModel: AppModel, isLeft: Boolean) {
        if (isLeft) {
            prefs.appNameSwipeLeft = appModel.appLabel
            prefs.appPackageSwipeLeft = appModel.appPackage
            prefs.appUserSwipeLeft = appModel.user.toString()
            prefs.appActivityClassNameSwipeLeft = appModel.activityClassName
        } else {
            prefs.appNameSwipeRight = appModel.appLabel
            prefs.appPackageSwipeRight = appModel.appPackage
            prefs.appUserSwipeRight = appModel.user.toString()
            prefs.appActivityClassNameRight = appModel.activityClassName
        }
        updateSwipeApps()
    }

    private fun setClockApp(appModel: AppModel) {
        prefs.clockAppPackage = appModel.appPackage
        prefs.clockAppUser = appModel.user.toString()
        prefs.clockAppClassName = appModel.activityClassName
    }

    private fun setCalendarApp(appModel: AppModel) {
        prefs.calendarAppPackage = appModel.appPackage
        prefs.calendarAppUser = appModel.user.toString()
        prefs.calendarAppClassName = appModel.activityClassName
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
//            } catch (e: SecurityException) {
//                handleSecurityException(e, appModel)
            } catch (_: Exception) {
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
    fun updateHomeAlignment(gravity: Int) {
        prefs.homeAlignment = gravity
        homeAppAlignment.value = prefs.homeAlignment
    }

    // Add lifecycle management
    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

}