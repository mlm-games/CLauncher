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
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.clauncher.data.Constants
import app.clauncher.data.Navigation
import app.clauncher.data.PrefsDataStore
import app.clauncher.helper.isDarkThemeOn
import app.clauncher.helper.isEinkDisplay
import app.clauncher.helper.isTablet
import app.clauncher.helper.resetLauncherViaFakeActivity
import app.clauncher.helper.setPlainWallpaper
import app.clauncher.helper.showLauncherSelector
import app.clauncher.ui.compose.CLauncherNavigation
import app.clauncher.ui.theme.CLauncherTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var prefsDataStore: PrefsDataStore
    private lateinit var viewModel: MainViewModel

    override fun attachBaseContext(context: Context) {
        val newConfig = Configuration(context.resources.configuration)
        //newConfig.fontScale = Prefs(context).textSizeScale
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        prefsDataStore = PrefsDataStore(this)
        lifecycleScope.launch {
            val appTheme = prefsDataStore.appTheme.first()
            if (isEinkDisplay()) {
                prefsDataStore.setAppTheme(AppCompatDelegate.MODE_NIGHT_NO)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(appTheme)
            }
        }

        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        lifecycleScope.launch {
            val firstOpen = prefsDataStore.firstOpen.first()
            if (firstOpen) {
                viewModel.firstOpen(true)
                prefsDataStore.setFirstOpen(false)
                prefsDataStore.setFirstOpenTime(System.currentTimeMillis())
            }
        }

        setupOrientation()
        window.addFlags(FLAG_LAYOUT_NO_LIMITS)

        setContent {
            CLauncherTheme {
                var currentScreen by remember { mutableStateOf("home") }

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
        viewModel.loadApps()
    }

    private fun initObservers() {
//        viewModel.resetLauncherLiveData.observe(this) {
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
//                //TODO: show toast saying to set it manually.
//                resetLauncherViaFakeActivity()
//            else
//                showLauncherSelector(Constants.REQUEST_CODE_LAUNCHER_SELECTOR)
//        }

        lifecycleScope.launch {
            // Only collect when the lifecycle is at least STARTED
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.launcherResetFailed.collect { resetFailed ->
                    openLauncherChooser(resetFailed)
                }
            }
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
        lifecycleScope.launch {
            val appTheme = prefsDataStore.appTheme.first()
            AppCompatDelegate.setDefaultNightMode(appTheme)

            val plainWallpaper = prefsDataStore.plainWallpaper.first()
            if (plainWallpaper && AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
                setPlainWallpaper()
                recreate()
            }
        }
    }
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        when (result.resultCode) {
            RESULT_OK -> {
                when (result.data?.getIntExtra("requestCode", 0)) { // Or however you're passing the request code back
                    Constants.REQUEST_CODE_ENABLE_ADMIN -> {
                        lifecycleScope.launch {
                            prefsDataStore.setLockMode(true)
                        }
                    }
                    Constants.REQUEST_CODE_LAUNCHER_SELECTOR -> resetLauncherViaFakeActivity()
                }
            }
        }
    }

}