package app.clauncher.ui.mapper

import app.clauncher.data.AppModel
import app.clauncher.ui.state.AppUiModel
import app.clauncher.ui.state.HomeAppUiModel

// Map from domain model to UI model
fun AppModel.toUiModel(): AppUiModel {
    return AppUiModel(
        id = this.getKey(),
        label = this.appLabel,
        packageName = this.appPackage,
        icon = this.appIcon,
        isHidden = this.isHidden
    )
}

// Map from UI model back to domain model (if needed)
fun AppUiModel.toDomainModel(userHandle: android.os.UserHandle, activityClassName: String?): AppModel {
    return AppModel(
        appLabel = this.label,
        key = null, // Would need to recreate the collation key
        appPackage = this.packageName,
        activityClassName = activityClassName,
        user = userHandle,
        appIcon = this.icon
    )
}

// Map to home app UI model
fun AppModel.toHomeAppUiModel(isInstalled: Boolean): HomeAppUiModel {
    return HomeAppUiModel(
        id = this.getKey(),
        label = this.appLabel,
        packageName = this.appPackage,
        isInstalled = isInstalled
    )
}