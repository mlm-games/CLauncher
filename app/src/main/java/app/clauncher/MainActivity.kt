package app.clauncher

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import app.clauncher.data.Constants
import app.clauncher.data.Prefs
import app.clauncher.helper.isDarkThemeOn
import app.clauncher.helper.isEinkDisplay
import app.clauncher.helper.isTablet
import app.clauncher.helper.resetLauncherViaFakeActivity
import app.clauncher.helper.setPlainWallpaper
import app.clauncher.helper.showLauncherSelector
import app.clauncher.ui.compose.CLauncherNavigation
import app.clauncher.ui.theme.CLauncherTheme

class MainActivity : ComponentActivity() {
    private lateinit var prefs: Prefs
    private lateinit var viewModel: MainViewModel

    override fun attachBaseContext(context: Context) {
        val newConfig = Configuration(context.resources.configuration)
        newConfig.fontScale = Prefs(context).textSizeScale
        applyOverrideConfiguration(newConfig)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = Prefs(this)
        if (isEinkDisplay()) prefs.appTheme = AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(prefs.appTheme)
        super.onCreate(savedInstanceState)

        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        

        if (prefs.firstOpen) {
            viewModel.firstOpen(true)
            prefs.firstOpen = false
            prefs.firstOpenTime = System.currentTimeMillis()
        }

        setupOrientation()
        window.addFlags(FLAG_LAYOUT_NO_LIMITS)
//     }

//     override fun onStop() {
//         backToHomeScreen()
//         super.onStop()
//     }


//     override fun onUserLeaveHint() {
//         // Only go home if not switching apps?
//         if (!isChangingConfigurations) {
//             backToHomeScreen()
//         }
//         super.onUserLeaveHint()
//     }

//     override fun onNewIntent(intent: Intent?) {
//         // Check if this is an app switch intent
//         if (intent?.action != Intent.ACTION_MAIN ||
//             intent.hasCategory(Intent.CATEGORY_HOME)) {
//             backToHomeScreen()
//         }
//         super.onNewIntent(intent)
//     }

//     override fun onConfigurationChanged(newConfig: Configuration) {
//         super.onConfigurationChanged(newConfig)
//         AppCompatDelegate.setDefaultNightMode(prefs.appTheme)
//         if (prefs.plainWallpaper && AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
//             setPlainWallpaper()
//             recreate()

        setContent {
            CLauncherTheme {
                var currentScreen by remember { mutableStateOf("home") }
                
                // Handle system back press
                BackHandler(enabled = currentScreen != "home") {
                    currentScreen = "home"
                }

                // Observe ViewModel events
                DisposableEffect(viewModel) {
                    val launcherResetObserver = Observer<Boolean> { resetFailed ->
                        openLauncherChooser(resetFailed)
                    }
                    
                    viewModel.launcherResetFailed.observe(this@MainActivity, launcherResetObserver)
                    
                    onDispose {
                        viewModel.launcherResetFailed.removeObserver(launcherResetObserver)
                    }
                }

                // Main navigation
                CLauncherNavigation(
                    viewModel = viewModel,
                    currentScreen = currentScreen,
                    onScreenChange = { screen ->
                        currentScreen = screen
                    }
                )
            }
        }

        initObservers()
        viewModel.getAppList()
    }

    private fun initObservers() {
        viewModel.resetLauncherLiveData.observe(this) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                resetLauncherViaFakeActivity()
            else
                showLauncherSelector(Constants.REQUEST_CODE_LAUNCHER_SELECTOR)
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setupOrientation() {
        if (isTablet(this) || Build.VERSION.SDK_INT == Build.VERSION_CODES.O)
            return
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun openLauncherChooser(resetFailed: Boolean) {
        if (resetFailed) {
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            startActivity(intent)
        }
    }

    private fun setPlainWallpaper() {
        if (this.isDarkThemeOn())
            setPlainWallpaper(this, android.R.color.black)
        else setPlainWallpaper(this, android.R.color.white)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        AppCompatDelegate.setDefaultNightMode(prefs.appTheme)
        if (prefs.plainWallpaper && AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            setPlainWallpaper()
            recreate()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constants.REQUEST_CODE_ENABLE_ADMIN -> {
                if (resultCode == Activity.RESULT_OK)
                    prefs.lockModeOn = true
            }
            Constants.REQUEST_CODE_LAUNCHER_SELECTOR -> {
                if (resultCode == Activity.RESULT_OK)
                    resetLauncherViaFakeActivity()
            }
        }
    }
}
