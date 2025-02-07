package studio.lunabee.onesafe.feature.settings.personalization

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import studio.lunabee.onesafe.MainActivity
import studio.lunabee.onesafe.common.extensions.alias
import studio.lunabee.onesafe.commonui.utils.OSProcessPhoenix
import studio.lunabee.onesafe.model.AppIcon
import javax.inject.Inject

class ChangeIconUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    operator fun invoke(iconAlias: AppIcon) {
        val packageManager = context.packageManager
        val packageName = MainActivity::class.java.`package`!!.name
        AppIcon.entries.forEach { alias ->
            val componentName = ComponentName(context, "$packageName.${alias.alias}")
            val isEnabled = iconAlias == alias
            packageManager.setComponentEnabledSetting(
                componentName,
                if (isEnabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP, // this flag does not work on API >= 31 (i.e android 12)
            )
        }

        // PackageManager.DONT_KILL_APP does work in following versions. But we also want to kill the activity to apply icon change.
        // Ideally, we want to restart the app, but it can lead to concurrence issue between the flag killing the app, and the restart.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            OSProcessPhoenix.triggerKill(context = context)
        }
    }
}
