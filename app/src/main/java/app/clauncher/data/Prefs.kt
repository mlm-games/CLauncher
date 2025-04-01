package app.clauncher.data

import android.content.Context
import android.content.SharedPreferences
import android.view.Gravity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import androidx.core.content.edit

class Prefs(context: Context) {
    private val PREFS_FILENAME = "app.clauncher"

    private val FIRST_OPEN = "FIRST_OPEN"
    private val FIRST_OPEN_TIME = "FIRST_OPEN_TIME"
    private val FIRST_SETTINGS_OPEN = "FIRST_SETTINGS_OPEN"
    private val FIRST_HIDE = "FIRST_HIDE"
    private val USER_STATE = "USER_STATE"
    private val LOCK_MODE = "LOCK_MODE"
    private val HOME_APPS_NUM = "HOME_APPS_NUM"
    private val SHOW_APP_NAMES = "SHOW_APP_NAMES"
    private val AUTO_SHOW_KEYBOARD = "AUTO_SHOW_KEYBOARD"
    private val KEYBOARD_MESSAGE = "KEYBOARD_MESSAGE"
    private val PLAIN_WALLPAPER = "PLAIN_WALLPAPER"
    private val HOME_ALIGNMENT = "HOME_ALIGNMENT"
    private val HOME_BOTTOM_ALIGNMENT = "HOME_BOTTOM_ALIGNMENT"
    private val APP_LABEL_ALIGNMENT = "APP_LABEL_ALIGNMENT"
    private val STATUS_BAR = "STATUS_BAR"
    private val DATE_TIME_VISIBILITY = "DATE_TIME_VISIBILITY"
    private val SWIPE_LEFT_ENABLED = "SWIPE_LEFT_ENABLED"
    private val SWIPE_RIGHT_ENABLED = "SWIPE_RIGHT_ENABLED"
    private val HIDDEN_APPS = "HIDDEN_APPS"
    private val HIDDEN_APPS_UPDATED = "HIDDEN_APPS_UPDATED"
    private val SHOW_HINT_COUNTER = "SHOW_HINT_COUNTER"
    private val APP_THEME = "APP_THEME"
    private val ABOUT_CLICKED = "ABOUT_CLICKED"
    private val RATE_CLICKED = "RATE_CLICKED"
    private val SHARE_SHOWN_TIME = "SHARE_SHOWN_TIME"
    private val SWIPE_DOWN_ACTION = "SWIPE_DOWN_ACTION"
    private val TEXT_SIZE_SCALE = "TEXT_SIZE_SCALE"

    private val APP_NAME_1 = "APP_NAME_1"
    private val APP_NAME_2 = "APP_NAME_2"
    private val APP_NAME_3 = "APP_NAME_3"
    private val APP_NAME_4 = "APP_NAME_4"
    private val APP_NAME_5 = "APP_NAME_5"
    private val APP_NAME_6 = "APP_NAME_6"
    private val APP_NAME_7 = "APP_NAME_7"
    private val APP_NAME_8 = "APP_NAME_8"
    private val APP_PACKAGE_1 = "APP_PACKAGE_1"
    private val APP_PACKAGE_2 = "APP_PACKAGE_2"
    private val APP_PACKAGE_3 = "APP_PACKAGE_3"
    private val APP_PACKAGE_4 = "APP_PACKAGE_4"
    private val APP_PACKAGE_5 = "APP_PACKAGE_5"
    private val APP_PACKAGE_6 = "APP_PACKAGE_6"
    private val APP_PACKAGE_7 = "APP_PACKAGE_7"
    private val APP_PACKAGE_8 = "APP_PACKAGE_8"
    private val APP_ACTIVITY_CLASS_NAME_1 = "APP_ACTIVITY_CLASS_NAME_1"
    private val APP_ACTIVITY_CLASS_NAME_2 = "APP_ACTIVITY_CLASS_NAME_2"
    private val APP_ACTIVITY_CLASS_NAME_3 = "APP_ACTIVITY_CLASS_NAME_3"
    private val APP_ACTIVITY_CLASS_NAME_4 = "APP_ACTIVITY_CLASS_NAME_4"
    private val APP_ACTIVITY_CLASS_NAME_5 = "APP_ACTIVITY_CLASS_NAME_5"
    private val APP_ACTIVITY_CLASS_NAME_6 = "APP_ACTIVITY_CLASS_NAME_6"
    private val APP_ACTIVITY_CLASS_NAME_7 = "APP_ACTIVITY_CLASS_NAME_7"
    private val APP_ACTIVITY_CLASS_NAME_8 = "APP_ACTIVITY_CLASS_NAME_8"
    private val APP_USER_1 = "APP_USER_1"
    private val APP_USER_2 = "APP_USER_2"
    private val APP_USER_3 = "APP_USER_3"
    private val APP_USER_4 = "APP_USER_4"
    private val APP_USER_5 = "APP_USER_5"
    private val APP_USER_6 = "APP_USER_6"
    private val APP_USER_7 = "APP_USER_7"
    private val APP_USER_8 = "APP_USER_8"

    private val APP_NAME_SWIPE_LEFT = "APP_NAME_SWIPE_LEFT"
    private val APP_NAME_SWIPE_RIGHT = "APP_NAME_SWIPE_RIGHT"
    private val APP_PACKAGE_SWIPE_LEFT = "APP_PACKAGE_SWIPE_LEFT"
    private val APP_PACKAGE_SWIPE_RIGHT = "APP_PACKAGE_SWIPE_RIGHT"
    private val APP_ACTIVITY_CLASS_NAME_SWIPE_LEFT = "APP_ACTIVITY_CLASS_NAME_SWIPE_LEFT"
    private val APP_ACTIVITY_CLASS_NAME_SWIPE_RIGHT = "APP_ACTIVITY_CLASS_NAME_SWIPE_RIGHT"
    private val APP_USER_SWIPE_LEFT = "APP_USER_SWIPE_LEFT"
    private val APP_USER_SWIPE_RIGHT = "APP_USER_SWIPE_RIGHT"
    private val CLOCK_APP_PACKAGE = "CLOCK_APP_PACKAGE"
    private val CLOCK_APP_USER = "CLOCK_APP_USER"
    private val CLOCK_APP_CLASS_NAME = "CLOCK_APP_CLASS_NAME"
    private val CALENDAR_APP_PACKAGE = "CALENDAR_APP_PACKAGE"
    private val CALENDAR_APP_USER = "CALENDAR_APP_USER"
    private val CALENDAR_APP_CLASS_NAME = "CALENDAR_APP_CLASS_NAME"

    private val prefs = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

    private inline fun <T> preference(
        defaultValue: T,
        key: String,
        crossinline getter: SharedPreferences.(String, T) -> T,
        crossinline setter: SharedPreferences.Editor.(String, T) -> SharedPreferences.Editor
    ) = object : ReadWriteProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T =
            prefs.getter(key, defaultValue)

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) =
            prefs.edit { setter(key, value) }
    }

    private fun stringSetPreference(
        key: String,
        defaultValue: Set<String> = emptySet()
    ): ReadWriteProperty<Any?, MutableSet<String>> = object : ReadWriteProperty<Any?, MutableSet<String>> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): MutableSet<String> {
            return prefs.getStringSet(key, defaultValue)?.toMutableSet() ?: mutableSetOf()
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: MutableSet<String>) {
            prefs.edit { putStringSet(key, value) }
        }
    }

    var hiddenApps by stringSetPreference(HIDDEN_APPS)

    var appTheme by preference(
        AppCompatDelegate.MODE_NIGHT_YES,
        "APP_THEME",
        SharedPreferences::getInt,
        SharedPreferences.Editor::putInt
    )

    var firstOpen by preference(
        true,
        "FIRST_OPEN",
        SharedPreferences::getBoolean,
        SharedPreferences.Editor::putBoolean
    )

    var useSystemFont by preference(
        true,
        "USE_SYSTEM_FONT",
        SharedPreferences::getBoolean,
        SharedPreferences.Editor::putBoolean
    )

    var firstOpenTime by preference(
        0L,
        FIRST_OPEN_TIME,
        SharedPreferences::getLong,
        SharedPreferences.Editor::putLong
    )

    var firstSettingsOpen by preference(
        true,
        FIRST_SETTINGS_OPEN,
        SharedPreferences::getBoolean,
        SharedPreferences.Editor::putBoolean
    )

    var firstHide by preference(
        true,
        FIRST_HIDE,
        SharedPreferences::getBoolean,
        SharedPreferences.Editor::putBoolean
    )

    var userState by preference(
        Constants.UserState.START,
        USER_STATE,
        SharedPreferences::getString,
        SharedPreferences.Editor::putString
    )

    var lockModeOn by preference(
        false,
        LOCK_MODE,
        SharedPreferences::getBoolean,
        SharedPreferences.Editor::putBoolean
    )

    var toggleAppVisibility by preference(
        false,
        SHOW_APP_NAMES,
        SharedPreferences::getBoolean,
        SharedPreferences.Editor::putBoolean
    )

    var autoShowKeyboard by preference(
        true,
        AUTO_SHOW_KEYBOARD,
        SharedPreferences::getBoolean,
        SharedPreferences.Editor::putBoolean
    )

    var keyboardMessageShown by preference(
        false,
        KEYBOARD_MESSAGE,
        SharedPreferences::getBoolean,
        SharedPreferences.Editor::putBoolean
    )

    var plainWallpaper by preference(
        false,
        PLAIN_WALLPAPER,
        SharedPreferences::getBoolean,
        SharedPreferences.Editor::putBoolean
    )

    var homeAppsNum by preference(
        0,
        HOME_APPS_NUM,
        SharedPreferences::getInt,
        SharedPreferences.Editor::putInt
    )

    var homeAlignment by preference(
        Gravity.START,
        HOME_ALIGNMENT,
        SharedPreferences::getInt,
        SharedPreferences.Editor::putInt
    )

    var homeBottomAlignment by preference(
        false,
        HOME_BOTTOM_ALIGNMENT,
        SharedPreferences::getBoolean,
        SharedPreferences.Editor::putBoolean
    )

    var appLabelAlignment by preference(
        Gravity.START,
        APP_LABEL_ALIGNMENT,
        SharedPreferences::getInt,
        SharedPreferences.Editor::putInt
    )

    var showStatusBar by preference(
        false,
        STATUS_BAR,
        SharedPreferences::getBoolean,
        SharedPreferences.Editor::putBoolean
    )

    var dateTimeVisibility by preference(
        Constants.DateTime.ON,
        DATE_TIME_VISIBILITY,
        SharedPreferences::getInt,
        SharedPreferences.Editor::putInt
    )

    var swipeLeftEnabled by preference(
        true,
        SWIPE_LEFT_ENABLED,
        SharedPreferences::getBoolean,
        SharedPreferences.Editor::putBoolean
    )

    var swipeRightEnabled by preference(
        true,
        SWIPE_RIGHT_ENABLED,
        SharedPreferences::getBoolean,
        SharedPreferences.Editor::putBoolean
    )

    var textSizeScale by preference(
        1.0f,
        TEXT_SIZE_SCALE,
        SharedPreferences::getFloat,
        SharedPreferences.Editor::putFloat
    )

    var hiddenAppsUpdated by preference(
        false,
        HIDDEN_APPS_UPDATED,
        SharedPreferences::getBoolean,
        SharedPreferences.Editor::putBoolean
    )

    var toShowHintCounter by preference(
        1,
        SHOW_HINT_COUNTER,
        SharedPreferences::getInt,
        SharedPreferences.Editor::putInt
    )

    var aboutClicked by preference(
        false,
        ABOUT_CLICKED,
        SharedPreferences::getBoolean,
        SharedPreferences.Editor::putBoolean
    )

    var rateClicked by preference(
        false,
        RATE_CLICKED,
        SharedPreferences::getBoolean,
        SharedPreferences.Editor::putBoolean
    )

    var shareShownTime by preference(
        0L,
        SHARE_SHOWN_TIME,
        SharedPreferences::getLong,
        SharedPreferences.Editor::putLong
    )

    var swipeDownAction by preference(
        Constants.SwipeDownAction.NOTIFICATIONS,
        SWIPE_DOWN_ACTION,
        SharedPreferences::getInt,
        SharedPreferences.Editor::putInt
    )
    

    var appName1 by preference("", APP_NAME_1, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appName2 by preference("", APP_NAME_2, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appName3 by preference("", APP_NAME_3, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appName4 by preference("", APP_NAME_4, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appName5 by preference("", APP_NAME_5, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appName6 by preference("", APP_NAME_6, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appName7 by preference("", APP_NAME_7, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appName8 by preference("", APP_NAME_8, SharedPreferences::getString, SharedPreferences.Editor::putString)

    var appPackage1 by preference("", APP_PACKAGE_1, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appPackage2 by preference("", APP_PACKAGE_2, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appPackage3 by preference("", APP_PACKAGE_3, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appPackage4 by preference("", APP_PACKAGE_4, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appPackage5 by preference("", APP_PACKAGE_5, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appPackage6 by preference("", APP_PACKAGE_6, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appPackage7 by preference("", APP_PACKAGE_7, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appPackage8 by preference("", APP_PACKAGE_8, SharedPreferences::getString, SharedPreferences.Editor::putString)

    var appActivityClassName1 by preference<String?>(null, APP_ACTIVITY_CLASS_NAME_1, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appActivityClassName2 by preference<String?>(null, APP_ACTIVITY_CLASS_NAME_2, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appActivityClassName3 by preference<String?>(null, APP_ACTIVITY_CLASS_NAME_3, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appActivityClassName4 by preference<String?>(null, APP_ACTIVITY_CLASS_NAME_4, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appActivityClassName5 by preference<String?>(null, APP_ACTIVITY_CLASS_NAME_5, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appActivityClassName6 by preference<String?>(null, APP_ACTIVITY_CLASS_NAME_6, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appActivityClassName7 by preference<String?>(null, APP_ACTIVITY_CLASS_NAME_7, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appActivityClassName8 by preference<String?>(null, APP_ACTIVITY_CLASS_NAME_8, SharedPreferences::getString, SharedPreferences.Editor::putString)

    var appUser1 by preference("", APP_USER_1, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appUser2 by preference("", APP_USER_2, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appUser3 by preference("", APP_USER_3, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appUser4 by preference("", APP_USER_4, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appUser5 by preference("", APP_USER_5, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appUser6 by preference("", APP_USER_6, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appUser7 by preference("", APP_USER_7, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appUser8 by preference("", APP_USER_8, SharedPreferences::getString, SharedPreferences.Editor::putString)

    var appNameSwipeLeft by preference("Camera", APP_NAME_SWIPE_LEFT, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appNameSwipeRight by preference("Phone", APP_NAME_SWIPE_RIGHT, SharedPreferences::getString, SharedPreferences.Editor::putString)

    var appPackageSwipeLeft by preference("", APP_PACKAGE_SWIPE_LEFT, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appPackageSwipeRight by preference("", APP_PACKAGE_SWIPE_RIGHT, SharedPreferences::getString, SharedPreferences.Editor::putString)

    var appActivityClassNameSwipeLeft by preference<String?>(null, APP_ACTIVITY_CLASS_NAME_SWIPE_LEFT, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appActivityClassNameRight by preference<String?>(null, APP_ACTIVITY_CLASS_NAME_SWIPE_RIGHT, SharedPreferences::getString, SharedPreferences.Editor::putString)

    var appUserSwipeLeft by preference("", APP_USER_SWIPE_LEFT, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var appUserSwipeRight by preference("", APP_USER_SWIPE_RIGHT, SharedPreferences::getString, SharedPreferences.Editor::putString)

    var clockAppPackage by preference("", CLOCK_APP_PACKAGE, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var clockAppUser by preference("", CLOCK_APP_USER, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var clockAppClassName by preference<String?>(null, CLOCK_APP_CLASS_NAME, SharedPreferences::getString, SharedPreferences.Editor::putString)

    var calendarAppPackage by preference("", CALENDAR_APP_PACKAGE, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var calendarAppUser by preference("", CALENDAR_APP_USER, SharedPreferences::getString, SharedPreferences.Editor::putString)
    var calendarAppClassName by preference<String?>(null, CALENDAR_APP_CLASS_NAME, SharedPreferences::getString, SharedPreferences.Editor::putString)

    fun getAppPackage(location: Int): String {
        return when (location) {
            1 -> prefs.getString(APP_PACKAGE_1, "").toString()
            2 -> prefs.getString(APP_PACKAGE_2, "").toString()
            3 -> prefs.getString(APP_PACKAGE_3, "").toString()
            4 -> prefs.getString(APP_PACKAGE_4, "").toString()
            5 -> prefs.getString(APP_PACKAGE_5, "").toString()
            6 -> prefs.getString(APP_PACKAGE_6, "").toString()
            7 -> prefs.getString(APP_PACKAGE_7, "").toString()
            8 -> prefs.getString(APP_PACKAGE_8, "").toString()
            else -> ""
        }
    }

    fun getAppActivityClassName(location: Int): String {
        return when (location) {
            1 -> prefs.getString(APP_ACTIVITY_CLASS_NAME_1, "").toString()
            2 -> prefs.getString(APP_ACTIVITY_CLASS_NAME_2, "").toString()
            3 -> prefs.getString(APP_ACTIVITY_CLASS_NAME_3, "").toString()
            4 -> prefs.getString(APP_ACTIVITY_CLASS_NAME_4, "").toString()
            5 -> prefs.getString(APP_ACTIVITY_CLASS_NAME_5, "").toString()
            6 -> prefs.getString(APP_ACTIVITY_CLASS_NAME_6, "").toString()
            7 -> prefs.getString(APP_ACTIVITY_CLASS_NAME_7, "").toString()
            8 -> prefs.getString(APP_ACTIVITY_CLASS_NAME_8, "").toString()
            else -> ""
        }
    }

    fun getAppUser(location: Int): String {
        return when (location) {
            1 -> prefs.getString(APP_USER_1, "").toString()
            2 -> prefs.getString(APP_USER_2, "").toString()
            3 -> prefs.getString(APP_USER_3, "").toString()
            4 -> prefs.getString(APP_USER_4, "").toString()
            5 -> prefs.getString(APP_USER_5, "").toString()
            6 -> prefs.getString(APP_USER_6, "").toString()
            7 -> prefs.getString(APP_USER_7, "").toString()
            8 -> prefs.getString(APP_USER_8, "").toString()
            else -> ""
        }
    }

    fun getAppRenameLabel(appPackage: String): String = prefs.getString(appPackage, "").toString()

    fun setAppRenameLabel(appPackage: String, renameLabel: String) = prefs.edit {
        putString(
            appPackage,
            renameLabel
        )
    }

    @Composable
    fun rememberPrefs(): Prefs {
        val context = LocalContext.current
        return remember { Prefs(context) }
    }




}