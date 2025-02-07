package studio.lunabee.onesafe.common.utils.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import studio.lunabee.onesafe.commonui.notification.OSNotificationManager

object UiNotificationHelper {

    @Composable
    fun areNotificationsEnabled(): State<Boolean> {
        val lifecycleOwner = LocalLifecycleOwner.current
        val context = LocalContext.current
        val state = remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                state.value = OSNotificationManager(context).areNotificationsEnabled(null)
            }
        }
        return state
    }

    fun getSettingIntent(context: Context): Intent = Intent().apply {
        action = "android.settings.APP_NOTIFICATION_SETTINGS"
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            putExtra("app_package", context.packageName)
            putExtra("app_uid", context.applicationInfo.uid)
        } else {
            putExtra("android.provider.extra.APP_PACKAGE", context.packageName)
        }
    }
}
