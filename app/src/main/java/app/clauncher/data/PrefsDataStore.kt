package app.clauncher.data

import android.content.Context
import android.view.Gravity
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property for Context to access the DataStore instance
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app.clauncher")

class PrefsDataStore(private val context: Context) {
    // Define preference keys
    companion object {
        // General settings
        val FIRST_OPEN = booleanPreferencesKey("FIRST_OPEN")
        val FIRST_OPEN_TIME = longPreferencesKey("FIRST_OPEN_TIME")
        val FIRST_SETTINGS_OPEN = booleanPreferencesKey("FIRST_SETTINGS_OPEN")
        val FIRST_HIDE = booleanPreferencesKey("FIRST_HIDE")
        val USER_STATE = stringPreferencesKey("USER_STATE")
        val LOCK_MODE = booleanPreferencesKey("LOCK_MODE")
        val HOME_APPS_NUM = intPreferencesKey("HOME_APPS_NUM")
        val SHOW_APP_NAMES = booleanPreferencesKey("SHOW_APP_NAMES")
        val AUTO_SHOW_KEYBOARD = booleanPreferencesKey("AUTO_SHOW_KEYBOARD")
        val KEYBOARD_MESSAGE = booleanPreferencesKey("KEYBOARD_MESSAGE")
        val PLAIN_WALLPAPER = booleanPreferencesKey("PLAIN_WALLPAPER")
        val HOME_ALIGNMENT = intPreferencesKey("HOME_ALIGNMENT")
        val HOME_BOTTOM_ALIGNMENT = booleanPreferencesKey("HOME_BOTTOM_ALIGNMENT")
        val APP_LABEL_ALIGNMENT = intPreferencesKey("APP_LABEL_ALIGNMENT")
        val STATUS_BAR = booleanPreferencesKey("STATUS_BAR")
        val DATE_TIME_VISIBILITY = intPreferencesKey("DATE_TIME_VISIBILITY")
        val SWIPE_LEFT_ENABLED = booleanPreferencesKey("SWIPE_LEFT_ENABLED")
        val SWIPE_RIGHT_ENABLED = booleanPreferencesKey("SWIPE_RIGHT_ENABLED")
        val HIDDEN_APPS = stringSetPreferencesKey("HIDDEN_APPS")
        val HIDDEN_APPS_UPDATED = booleanPreferencesKey("HIDDEN_APPS_UPDATED")
        val SHOW_HINT_COUNTER = intPreferencesKey("SHOW_HINT_COUNTER")
        val APP_THEME = intPreferencesKey("APP_THEME")
        val ABOUT_CLICKED = booleanPreferencesKey("ABOUT_CLICKED")
        val RATE_CLICKED = booleanPreferencesKey("RATE_CLICKED")
        val SHARE_SHOWN_TIME = longPreferencesKey("SHARE_SHOWN_TIME")
        val SWIPE_DOWN_ACTION = intPreferencesKey("SWIPE_DOWN_ACTION")
        val TEXT_SIZE_SCALE = floatPreferencesKey("TEXT_SIZE_SCALE")
        val USE_SYSTEM_FONT = booleanPreferencesKey("USE_SYSTEM_FONT")

        // App keys (1-8)
        val APP_NAME_1 = stringPreferencesKey("APP_NAME_1")
        val APP_NAME_2 = stringPreferencesKey("APP_NAME_2")
        val APP_NAME_3 = stringPreferencesKey("APP_NAME_3")
        val APP_NAME_4 = stringPreferencesKey("APP_NAME_4")
        val APP_NAME_5 = stringPreferencesKey("APP_NAME_5")
        val APP_NAME_6 = stringPreferencesKey("APP_NAME_6")
        val APP_NAME_7 = stringPreferencesKey("APP_NAME_7")
        val APP_NAME_8 = stringPreferencesKey("APP_NAME_8")

        val APP_PACKAGE_1 = stringPreferencesKey("APP_PACKAGE_1")
        val APP_PACKAGE_2 = stringPreferencesKey("APP_PACKAGE_2")
        val APP_PACKAGE_3 = stringPreferencesKey("APP_PACKAGE_3")
        val APP_PACKAGE_4 = stringPreferencesKey("APP_PACKAGE_4")
        val APP_PACKAGE_5 = stringPreferencesKey("APP_PACKAGE_5")
        val APP_PACKAGE_6 = stringPreferencesKey("APP_PACKAGE_6")
        val APP_PACKAGE_7 = stringPreferencesKey("APP_PACKAGE_7")
        val APP_PACKAGE_8 = stringPreferencesKey("APP_PACKAGE_8")

        val APP_ACTIVITY_CLASS_NAME_1 = stringPreferencesKey("APP_ACTIVITY_CLASS_NAME_1")
        val APP_ACTIVITY_CLASS_NAME_2 = stringPreferencesKey("APP_ACTIVITY_CLASS_NAME_2")
        val APP_ACTIVITY_CLASS_NAME_3 = stringPreferencesKey("APP_ACTIVITY_CLASS_NAME_3")
        val APP_ACTIVITY_CLASS_NAME_4 = stringPreferencesKey("APP_ACTIVITY_CLASS_NAME_4")
        val APP_ACTIVITY_CLASS_NAME_5 = stringPreferencesKey("APP_ACTIVITY_CLASS_NAME_5")
        val APP_ACTIVITY_CLASS_NAME_6 = stringPreferencesKey("APP_ACTIVITY_CLASS_NAME_6")
        val APP_ACTIVITY_CLASS_NAME_7 = stringPreferencesKey("APP_ACTIVITY_CLASS_NAME_7")
        val APP_ACTIVITY_CLASS_NAME_8 = stringPreferencesKey("APP_ACTIVITY_CLASS_NAME_8")

        val APP_USER_1 = stringPreferencesKey("APP_USER_1")
        val APP_USER_2 = stringPreferencesKey("APP_USER_2")
        val APP_USER_3 = stringPreferencesKey("APP_USER_3")
        val APP_USER_4 = stringPreferencesKey("APP_USER_4")
        val APP_USER_5 = stringPreferencesKey("APP_USER_5")
        val APP_USER_6 = stringPreferencesKey("APP_USER_6")
        val APP_USER_7 = stringPreferencesKey("APP_USER_7")
        val APP_USER_8 = stringPreferencesKey("APP_USER_8")

        // Swipe left/right app settings
        val APP_NAME_SWIPE_LEFT = stringPreferencesKey("APP_NAME_SWIPE_LEFT")
        val APP_NAME_SWIPE_RIGHT = stringPreferencesKey("APP_NAME_SWIPE_RIGHT")
        val APP_PACKAGE_SWIPE_LEFT = stringPreferencesKey("APP_PACKAGE_SWIPE_LEFT")
        val APP_PACKAGE_SWIPE_RIGHT = stringPreferencesKey("APP_PACKAGE_SWIPE_RIGHT")
        val APP_ACTIVITY_CLASS_NAME_SWIPE_LEFT =
            stringPreferencesKey("APP_ACTIVITY_CLASS_NAME_SWIPE_LEFT")
        val APP_ACTIVITY_CLASS_NAME_SWIPE_RIGHT =
            stringPreferencesKey("APP_ACTIVITY_CLASS_NAME_SWIPE_RIGHT")
        val APP_USER_SWIPE_LEFT = stringPreferencesKey("APP_USER_SWIPE_LEFT")
        val APP_USER_SWIPE_RIGHT = stringPreferencesKey("APP_USER_SWIPE_RIGHT")

        // Clock and calendar app settings
        val CLOCK_APP_PACKAGE = stringPreferencesKey("CLOCK_APP_PACKAGE")
        val CLOCK_APP_USER = stringPreferencesKey("CLOCK_APP_USER")
        val CLOCK_APP_CLASS_NAME = stringPreferencesKey("CLOCK_APP_CLASS_NAME")
        val CALENDAR_APP_PACKAGE = stringPreferencesKey("CALENDAR_APP_PACKAGE")
        val CALENDAR_APP_USER = stringPreferencesKey("CALENDAR_APP_USER")
        val CALENDAR_APP_CLASS_NAME = stringPreferencesKey("CALENDAR_APP_CLASS_NAME")
    }

    // Getter Flows for all preferences

    // General settings
    val firstOpen: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[FIRST_OPEN] != false
    }

    val firstOpenTime: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[FIRST_OPEN_TIME] ?: 0L
    }

    val firstSettingsOpen: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[FIRST_SETTINGS_OPEN] != false
    }

    val firstHide: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[FIRST_HIDE] != false
    }

    val userState: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_STATE] ?: "" // Define a sensible default
    }

    val lockMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[LOCK_MODE] == true
    }

    val homeAppsNum: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[HOME_APPS_NUM] ?: 0
    }

    val showAppNames: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_APP_NAMES] != false
    }

    val autoShowKeyboard: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_SHOW_KEYBOARD] != false
    }

    val keyboardMessage: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEYBOARD_MESSAGE] == true
    }

    val plainWallpaper: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PLAIN_WALLPAPER] == true
    }

    val homeAlignment: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[HOME_ALIGNMENT] ?: Gravity.START
    }

    val homeBottomAlignment: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HOME_BOTTOM_ALIGNMENT] == true
    }

    val appLabelAlignment: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[APP_LABEL_ALIGNMENT] ?: Gravity.START
    }

    val statusBar: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[STATUS_BAR] == true
    }

    val dateTimeVisibility: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DATE_TIME_VISIBILITY] ?: 0 // Define a sensible default
    }

    val swipeLeftEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SWIPE_LEFT_ENABLED] != false
    }

    val swipeRightEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SWIPE_RIGHT_ENABLED] != false
    }

    val hiddenApps: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[HIDDEN_APPS] ?: emptySet()
    }

    val hiddenAppsUpdated: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HIDDEN_APPS_UPDATED] == true
    }

    val showHintCounter: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[SHOW_HINT_COUNTER] ?: 1
    }

    val appTheme: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[APP_THEME] ?: AppCompatDelegate.MODE_NIGHT_YES
    }

    val aboutClicked: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ABOUT_CLICKED] == true
    }

    val rateClicked: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[RATE_CLICKED] == true
    }

    val shareShownTime: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[SHARE_SHOWN_TIME] ?: 0L
    }

    val swipeDownAction: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[SWIPE_DOWN_ACTION] ?: 0 // Define default based on your Constants
    }

    val textSizeScale: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[TEXT_SIZE_SCALE] ?: 1.0f
    }

    val useSystemFont: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_SYSTEM_FONT] != false
    }

    // Home apps preferences
    val appName1: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_NAME_1] ?: ""
    }

    val appName2: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_NAME_2] ?: ""
    }

    val appName3: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_NAME_3] ?: ""
    }

    val appName4: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_NAME_4] ?: ""
    }

    val appName5: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_NAME_5] ?: ""
    }

    val appName6: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_NAME_6] ?: ""
    }

    val appName7: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_NAME_7] ?: ""
    }

    val appName8: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_NAME_8] ?: ""
    }

    val appPackage1: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_PACKAGE_1] ?: ""
    }

    val appPackage2: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_PACKAGE_2] ?: ""
    }

    val appPackage3: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_PACKAGE_3] ?: ""
    }

    val appPackage4: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_PACKAGE_4] ?: ""
    }

    val appPackage5: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_PACKAGE_5] ?: ""
    }

    val appPackage6: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_PACKAGE_6] ?: ""
    }

    val appPackage7: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_PACKAGE_7] ?: ""
    }

    val appPackage8: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_PACKAGE_8] ?: ""
    }

    val appActivityClassName1: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[APP_ACTIVITY_CLASS_NAME_1]
    }

    val appActivityClassName2: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[APP_ACTIVITY_CLASS_NAME_2]
    }

    val appActivityClassName3: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[APP_ACTIVITY_CLASS_NAME_3]
    }

    val appActivityClassName4: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[APP_ACTIVITY_CLASS_NAME_4]
    }

    val appActivityClassName5: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[APP_ACTIVITY_CLASS_NAME_5]
    }

    val appActivityClassName6: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[APP_ACTIVITY_CLASS_NAME_6]
    }

    val appActivityClassName7: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[APP_ACTIVITY_CLASS_NAME_7]
    }

    val appActivityClassName8: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[APP_ACTIVITY_CLASS_NAME_8]
    }

    val appUser1: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_USER_1] ?: ""
    }

    val appUser2: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_USER_2] ?: ""
    }

    val appUser3: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_USER_3] ?: ""
    }

    val appUser4: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_USER_4] ?: ""
    }

    val appUser5: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_USER_5] ?: ""
    }

    val appUser6: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_USER_6] ?: ""
    }

    val appUser7: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_USER_7] ?: ""
    }

    val appUser8: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_USER_8] ?: ""
    }

    // Swipe apps
    val appNameSwipeLeft: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_NAME_SWIPE_LEFT] ?: "Camera"
    }

    val appNameSwipeRight: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_NAME_SWIPE_RIGHT] ?: "Phone"
    }

    val appPackageSwipeLeft: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_PACKAGE_SWIPE_LEFT] ?: ""
    }

    val appPackageSwipeRight: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_PACKAGE_SWIPE_RIGHT] ?: ""
    }

    val appActivityClassNameSwipeLeft: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[APP_ACTIVITY_CLASS_NAME_SWIPE_LEFT]
    }

    val appActivityClassNameSwipeRight: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[APP_ACTIVITY_CLASS_NAME_SWIPE_RIGHT]
    }

    val appUserSwipeLeft: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_USER_SWIPE_LEFT] ?: ""
    }

    val appUserSwipeRight: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_USER_SWIPE_RIGHT] ?: ""
    }

    // Clock and calendar
    val clockAppPackage: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CLOCK_APP_PACKAGE] ?: ""
    }

    val clockAppUser: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CLOCK_APP_USER] ?: ""
    }

    val clockAppClassName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[CLOCK_APP_CLASS_NAME]
    }

    val calendarAppPackage: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CALENDAR_APP_PACKAGE] ?: ""
    }

    val calendarAppUser: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CALENDAR_APP_USER] ?: ""
    }

    val calendarAppClassName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[CALENDAR_APP_CLASS_NAME]
    }

    // Setter functions for all preferences

    // General settings
    suspend fun setFirstOpen(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FIRST_OPEN] = value
        }
    }

    suspend fun setFirstOpenTime(value: Long) {
        context.dataStore.edit { preferences ->
            preferences[FIRST_OPEN_TIME] = value
        }
    }

    suspend fun setFirstSettingsOpen(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FIRST_SETTINGS_OPEN] = value
        }
    }

    suspend fun setFirstHide(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FIRST_HIDE] = value
        }
    }

    suspend fun setUserState(value: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_STATE] = value
        }
    }

    suspend fun setLockMode(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[LOCK_MODE] = value
        }
    }

    suspend fun setHomeAppsNum(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[HOME_APPS_NUM] = value
        }
    }

    suspend fun setShowAppNames(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_APP_NAMES] = value
        }
    }

    suspend fun setAutoShowKeyboard(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_SHOW_KEYBOARD] = value
        }
    }

    suspend fun setKeyboardMessage(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEYBOARD_MESSAGE] = value
        }
    }

    suspend fun setPlainWallpaper(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PLAIN_WALLPAPER] = value
        }
    }

    suspend fun setHomeAlignment(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[HOME_ALIGNMENT] = value
        }
    }

    suspend fun setHomeBottomAlignment(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HOME_BOTTOM_ALIGNMENT] = value
        }
    }

    suspend fun setAppLabelAlignment(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[APP_LABEL_ALIGNMENT] = value
        }
    }

    suspend fun setStatusBar(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[STATUS_BAR] = value
        }
    }

    suspend fun setDateTimeVisibility(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[DATE_TIME_VISIBILITY] = value
        }
    }

    suspend fun setSwipeLeftEnabled(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SWIPE_LEFT_ENABLED] = value
        }
    }

    suspend fun setSwipeRightEnabled(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SWIPE_RIGHT_ENABLED] = value
        }
    }

    suspend fun setHiddenApps(value: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[HIDDEN_APPS] = value
        }
    }

    suspend fun setHiddenAppsUpdated(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HIDDEN_APPS_UPDATED] = value
        }
    }

    suspend fun setShowHintCounter(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_HINT_COUNTER] = value
        }
    }

    suspend fun setAppTheme(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[APP_THEME] = value
        }
    }

    suspend fun setAboutClicked(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ABOUT_CLICKED] = value
        }
    }

    suspend fun setRateClicked(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[RATE_CLICKED] = value
        }
    }

    suspend fun setShareShownTime(value: Long) {
        context.dataStore.edit { preferences ->
            preferences[SHARE_SHOWN_TIME] = value
        }
    }

    suspend fun setSwipeDownAction(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[SWIPE_DOWN_ACTION] = value
        }
    }

    suspend fun setTextSizeScale(value: Float) {
        context.dataStore.edit { preferences ->
            preferences[TEXT_SIZE_SCALE] = value
        }
    }

    suspend fun setUseSystemFont(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_SYSTEM_FONT] = value
        }
    }

    // Home apps setters
    suspend fun setAppName1(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_NAME_1] = value
        }
    }

    suspend fun setAppName2(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_NAME_2] = value
        }
    }

    suspend fun setAppName3(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_NAME_3] = value
        }
    }

    suspend fun setAppName4(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_NAME_4] = value
        }
    }

    suspend fun setAppName5(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_NAME_5] = value
        }
    }

    suspend fun setAppName6(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_NAME_6] = value
        }
    }

    suspend fun setAppName7(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_NAME_7] = value
        }
    }

    suspend fun setAppName8(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_NAME_8] = value
        }
    }

    suspend fun setAppPackage1(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_PACKAGE_1] = value
        }
    }

    suspend fun setAppPackage2(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_PACKAGE_2] = value
        }
    }

    suspend fun setAppPackage3(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_PACKAGE_3] = value
        }
    }

    suspend fun setAppPackage4(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_PACKAGE_4] = value
        }
    }

    suspend fun setAppPackage5(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_PACKAGE_5] = value
        }
    }

    suspend fun setAppPackage6(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_PACKAGE_6] = value
        }
    }

    suspend fun setAppPackage7(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_PACKAGE_7] = value
        }
    }

    suspend fun setAppPackage8(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_PACKAGE_8] = value
        }
    }

    suspend fun setAppUser1(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_USER_1] = value
        }
    }

    suspend fun setAppUser2(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_USER_2] = value
        }
    }

    suspend fun setAppUser3(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_USER_3] = value
        }
    }

    suspend fun setAppUser4(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_USER_4] = value
        }
    }

    suspend fun setAppUser5(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_USER_5] = value
        }
    }

    suspend fun setAppUser6(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_USER_6] = value
        }
    }

    suspend fun setAppUser7(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_USER_7] = value
        }
    }

    suspend fun setAppUser8(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_USER_8] = value
        }
    }


    suspend fun setAppActivityClassName1(value: String?) {
        context.dataStore.edit { preferences ->
            if (value != null) {
                preferences[APP_ACTIVITY_CLASS_NAME_1] = value
            } else {
                preferences.remove(APP_ACTIVITY_CLASS_NAME_1)
            }
        }
    }

    suspend fun setAppActivityClassName2(value: String?) {
        context.dataStore.edit { preferences ->
            if (value != null) {
                preferences[APP_ACTIVITY_CLASS_NAME_2] = value
            } else {
                preferences.remove(APP_ACTIVITY_CLASS_NAME_2)
            }
        }
    }

    suspend fun setAppActivityClassName3(value: String?) {
        context.dataStore.edit { preferences ->
            if (value != null) {
                preferences[APP_ACTIVITY_CLASS_NAME_3] = value
            } else {
                preferences.remove(APP_ACTIVITY_CLASS_NAME_3)
            }
        }
    }

    suspend fun setAppActivityClassName4(value: String?) {
        context.dataStore.edit { preferences ->
            if (value != null) {
                preferences[APP_ACTIVITY_CLASS_NAME_4] = value
            } else {
                preferences.remove(APP_ACTIVITY_CLASS_NAME_4)
            }
        }
    }

    suspend fun setAppActivityClassName5(value: String?) {
        context.dataStore.edit { preferences ->
            if (value != null) {
                preferences[APP_ACTIVITY_CLASS_NAME_5] = value
            } else {
                preferences.remove(APP_ACTIVITY_CLASS_NAME_5)
            }
        }
    }

    suspend fun setAppActivityClassName6(value: String?) {
        context.dataStore.edit { preferences ->
            if (value != null) {
                preferences[APP_ACTIVITY_CLASS_NAME_6] = value
            } else {
                preferences.remove(APP_ACTIVITY_CLASS_NAME_6)
            }
        }
    }

    suspend fun setAppActivityClassName7(value: String?) {
        context.dataStore.edit { preferences ->
            if (value != null) {
                preferences[APP_ACTIVITY_CLASS_NAME_7] = value
            } else {
                preferences.remove(APP_ACTIVITY_CLASS_NAME_7)
            }
        }
    }

    suspend fun setAppActivityClassName8(value: String?) {
        context.dataStore.edit { preferences ->
            if (value != null) {
                preferences[APP_ACTIVITY_CLASS_NAME_8] = value
            } else {
                preferences.remove(APP_ACTIVITY_CLASS_NAME_8)
            }
        }
    }

    suspend fun setAppNameSwipeLeft(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_NAME_SWIPE_LEFT] = value
        }
    }

    suspend fun setAppPackageSwipeLeft(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_PACKAGE_SWIPE_LEFT] = value
        }
    }

    suspend fun setAppUserSwipeLeft(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_USER_SWIPE_LEFT] = value
        }
    }

    suspend fun setAppNameSwipeRight(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_NAME_SWIPE_RIGHT] = value
        }
    }

    suspend fun setAppPackageSwipeRight(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_PACKAGE_SWIPE_RIGHT] = value
        }
    }

    suspend fun setAppUserSwipeRight(value: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_USER_SWIPE_RIGHT] = value
        }
    }

    suspend fun setAppActivityClassNameSwipeRight(value: String?) {
        context.dataStore.edit { preferences ->
            if (value != null) {
                preferences[APP_ACTIVITY_CLASS_NAME_SWIPE_RIGHT] = value
            } else {
                preferences.remove(APP_ACTIVITY_CLASS_NAME_SWIPE_RIGHT)
            }
        }
    }
    suspend fun setAppActivityClassNameSwipeLeft(value: String?) {
        context.dataStore.edit { preferences ->
            if (value != null) {
                preferences[APP_ACTIVITY_CLASS_NAME_SWIPE_LEFT] = value
            } else {
                preferences.remove(APP_ACTIVITY_CLASS_NAME_SWIPE_LEFT)
            }
        }
    }


    suspend fun setClockAppPackage(value: String) {
        context.dataStore.edit { preferences ->
            preferences[CLOCK_APP_PACKAGE] = value
        }
    }

    suspend fun setClockAppUser(value: String) {
        context.dataStore.edit { preferences ->
            preferences[CLOCK_APP_USER] = value
        }
    }

    suspend fun setClockAppClassName(value: String?) {
        context.dataStore.edit { preferences ->
            if (value != null) {
                preferences[CLOCK_APP_CLASS_NAME] = value
            } else {
                preferences.remove(CLOCK_APP_CLASS_NAME)
            }
        }
    }

    suspend fun setCalendarAppPackage(value: String) {
        context.dataStore.edit { preferences ->
            preferences[CALENDAR_APP_PACKAGE] = value
        }
    }

    suspend fun setCalendarAppUser(value: String) {
        context.dataStore.edit { preferences ->
            preferences[CALENDAR_APP_USER] = value
        }
    }

    suspend fun setCalendarAppClassName(value: String?) {
        context.dataStore.edit { preferences ->
            if (value != null) {
                preferences[CALENDAR_APP_CLASS_NAME] = value
            } else {
                preferences.remove(CALENDAR_APP_CLASS_NAME)
            }
        }
    }


}