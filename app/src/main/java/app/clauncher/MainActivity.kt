package app.clauncher

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import app.clauncher.data.Constants
import app.clauncher.data.Prefs
import app.clauncher.databinding.ActivityMainBinding
import app.clauncher.helper.isDarkThemeOn
import app.clauncher.helper.isEinkDisplay
import app.clauncher.helper.isTablet
import app.clauncher.helper.resetLauncherViaFakeActivity
import app.clauncher.helper.setPlainWallpaper
import app.clauncher.helper.showLauncherSelector

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs
    private lateinit var navController: NavController
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (navController.currentDestination?.id != R.id.mainFragment) {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }

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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = this.findNavController(R.id.nav_host_fragment)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        if (prefs.firstOpen) {
            viewModel.firstOpen(true)
            prefs.firstOpen = false
            prefs.firstOpenTime = System.currentTimeMillis()
        }


        //initClickListeners()
        initObservers(viewModel)
        viewModel.getAppList()
        setupOrientation()

        window.addFlags(FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onStop() {
        backToHomeScreen()
        super.onStop()
    }


    override fun onUserLeaveHint() {
        // Only go home if not switching apps?
        if (!isChangingConfigurations) {
            backToHomeScreen()
        }
        super.onUserLeaveHint()
    }

    override fun onNewIntent(intent: Intent?) {
        intent ?: return super.onNewIntent(intent)
        // Check if this is an app switch intent
        if (intent.action != Intent.ACTION_MAIN &&
            intent.hasCategory(Intent.CATEGORY_HOME)) {
            backToHomeScreen()
        }
        super.onNewIntent(intent)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (prefs.appTheme == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            AppCompatDelegate.setDefaultNightMode(prefs.appTheme)
            updatePlainWallpaper()
        }
    }

    private fun updatePlainWallpaper() {
        if (prefs.plainWallpaper) {
            setPlainWallpaper()
            recreate()
        }
    }
//    private fun initClickListeners() {
//        binding.ivClose.setOnClickListener {
//            binding.messageLayout.visibility = View.GONE
//        }
//    }

    private fun initObservers(viewModel: MainViewModel) {
        viewModel.launcherResetFailed.observe(this) {
            openLauncherChooser(it)
        }
        viewModel.resetLauncherLiveData.observe(this) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                resetLauncherViaFakeActivity()
            else
                showLauncherSelector(Constants.REQUEST_CODE_LAUNCHER_SELECTOR)
        }
//        viewModel.checkForMessages.observe(this) {
//            checkForMessages()
//        }
//        viewModel.showDialog.observe(this) {
//            when (it) {
//                Constants.Dialog.ABOUT -> {
//                    showMessageDialog(getString(R.string.app_name), getString(R.string.welcome_to_clauncher_settings), getString(R.string.okay)) {
//                        //binding.messageLayout.visibility = View.GONE
//                    }
//                }
//                Constants.Dialog.REVIEW -> {
//                    prefs.userState = Constants.UserState.RATE
//                    showMessageDialog(getString(R.string.did_you_know), getString(R.string.review_message), getString(R.string.leave_a_review)) {
//                        //binding.messageLayout.visibility = View.GONE
//                        prefs.rateClicked = true
//                        showToast("😇❤️")
//                        rateApp()
//                    }
//                }
//
//                Constants.Dialog.RATE -> {
//                    prefs.userState = Constants.UserState.SHARE
//                    showMessageDialog(getString(R.string.app_name), getString(R.string.rate_us_message), getString(R.string.rate_now)) {
//                        //binding.messageLayout.visibility = View.GONE
//                        prefs.rateClicked = true
//                        showToast("🤩❤️")
//                        rateApp()
//                    }
//                }
//
//                Constants.Dialog.SHARE -> {
//                    prefs.shareShownTime = System.currentTimeMillis()
//                    showMessageDialog(getString(R.string.app_name), getString(R.string.share_message), getString(R.string.share_now)) {
//                        //binding.messageLayout.visibility = View.GONE
//                        showToast("😊❤️")
//                        shareApp()
//                    }
//                }
//
//                Constants.Dialog.HIDDEN -> {
//                    showMessageDialog(getString(R.string.hidden_apps), getString(R.string.hidden_apps_message), getString(R.string.okay)) {
//                        //binding.messageLayout.visibility = View.GONE
//                    }
//                }
//
//                Constants.Dialog.KEYBOARD -> {
//                    showMessageDialog(getString(R.string.app_name), getString(R.string.keyboard_message), getString(R.string.okay)) {
//                        //binding.messageLayout.visibility = View.GONE
//                    }
//                }
//
//                Constants.Dialog.DIGITAL_WELLBEING -> {
//                    showMessageDialog("Hi", getString(R.string.digital_wellbeing_message), getString(R.string.learn_more)) {
//                        //binding.messageLayout.visibility = View.GONE
//                        openUrl(Constants.URL_DIGITAL_WELLBEING_LEARN_MORE)
//                    }
//                }
//            }
//        }
    }

//    private fun showMessageDialog(title: String, message: String, action: String, clickListener: () -> Unit) {
//        binding.tvTitle.text = title
//        binding.tvMessage.text = message
//        binding.tvAction.text = action
//        binding.tvAction.setOnClickListener { clickListener() }
//        binding.messageLayout.visibility = View.VISIBLE
//    }

//    private fun checkForMessages() {
//        if (prefs.firstOpenTime == 0L)
//            prefs.firstOpenTime = System.currentTimeMillis()
//
//        when (prefs.userState) {
//            Constants.UserState.START -> {
//                if (prefs.firstOpenTime.hasBeenHours(1))
//                    prefs.userState = Constants.UserState.REVIEW
//            }
//
//            Constants.UserState.REVIEW -> {
//                if (prefs.rateClicked)
//                    prefs.userState = Constants.UserState.SHARE
//                else if (isClauncherDefault(this))
//                    viewModel.showDialog.postValue(Constants.Dialog.REVIEW)
//            }
//
//            Constants.UserState.RATE -> {
//                if (prefs.rateClicked)
//                    prefs.userState = Constants.UserState.SHARE
//                else if (isClauncherDefault(this)
//                    && prefs.firstOpenTime.hasBeenDays(3)
//                    && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 15
//                ) viewModel.showDialog.postValue(Constants.Dialog.RATE)
//            }
//
//            Constants.UserState.SHARE -> {
//                if (isClauncherDefault(this) && prefs.firstOpenTime.hasBeenDays(14)
//                    && prefs.shareShownTime.hasBeenDays(45)
//                    && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 15
//                ) viewModel.showDialog.postValue(Constants.Dialog.SHARE)
//            }
//        }
//    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setupOrientation() {
        if (isTablet(this) || Build.VERSION.SDK_INT == Build.VERSION_CODES.O)
            return
        // In Android 8.0, windowIsTranslucent cannot be used with screenOrientation=portrait
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun backToHomeScreen() {
        //binding.messageLayout.visibility = View.GONE
        if (navController.currentDestination?.id != R.id.mainFragment)
            navController.popBackStack(R.id.mainFragment, false)
    }

    private fun setPlainWallpaper() {
        if (this.isDarkThemeOn())
            setPlainWallpaper(this, android.R.color.black)
        else setPlainWallpaper(this, android.R.color.white)
    }

    private fun openLauncherChooser(resetFailed: Boolean) {
        if (resetFailed) {
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            startActivity(intent)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constants.REQUEST_CODE_ENABLE_ADMIN -> {
                if (resultCode == RESULT_OK)
                    prefs.lockModeOn = true
            }

            Constants.REQUEST_CODE_LAUNCHER_SELECTOR -> {
                if (resultCode == RESULT_OK)
                    resetLauncherViaFakeActivity()
            }
        }
    }
}
