package studio.lunabee.onesafe.common.utils

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.MainActivity
import studio.lunabee.onesafe.common.utils.settings.AndroidSettings
import studio.lunabee.onesafe.ime.OneSafeKeyboardHelper

/**
 * @see [requestEnableOSKIme]
 */
interface OSKImeSettings {
    /**
     * Launch intent to access system ime settings and poll current setting to check if oSK has been enabled
     */
    fun requestEnableOSKIme()
}

/**
 * @param onOSKImeResult callback called on coming back from system settings
 *
 * @see [OSKImeSettings]
 */
@Composable
fun rememberOSKImeSettings(
    onOSKImeResult: (Boolean) -> Unit = {},
): OSKImeSettings {
    val context = LocalContext.current
    val settingsObserverScope = rememberCoroutineScope()
    val enableOneSafeKeyboardLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            settingsObserverScope.coroutineContext.cancelChildren() // stop polling by cancelling children jobs
            onOSKImeResult(isOneSafeKeyboardEnabled(context))
        },
    )
    return remember(context) { OSKImeSettingsImpl(settingsObserverScope, context, enableOneSafeKeyboardLauncher) }
}

private class OSKImeSettingsImpl(
    private val coroutineScope: CoroutineScope,
    private val context: Context,
    private val enableOneSafeKeyboardLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
) : OSKImeSettings {
    override fun requestEnableOSKIme() {
        val settingsIntent = Intent()
        val activityManager = context.getSystemService(ActivityManager::class.java)
        // Get current top most activity to re-navigate to it on ime selection (inspired from Florisboard). Expected to always be the
        // MainActivity, so fallback to it.
        val topComponent: ComponentName = activityManager.appTasks.firstOrNull()?.taskInfo?.topActivity
            ?: ComponentName(context, MainActivity::class.java)
        settingsIntent.action = Settings.ACTION_INPUT_METHOD_SETTINGS
        settingsIntent.addCategory(Intent.CATEGORY_DEFAULT)
        enableOneSafeKeyboardLauncher.launch(settingsIntent)
        coroutineScope.launch {
            // Poll setting until oSK is enabled
            while (!isOneSafeKeyboardEnabled(context))
                delay(KeyboardEnabledPollingDelayMs)
            val osIntent = Intent()
                .setComponent(topComponent)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            // Navigate to oSK by clearing top activities (system activity)
            context.startActivity(osIntent)
        }
    }
}

private fun isOneSafeKeyboardEnabled(context: Context): Boolean =
    AndroidSettings.Secure.getString(context, Settings.Secure.ENABLED_INPUT_METHODS)?.let {
        OneSafeKeyboardHelper.parseIsOneSafeKeyboardEnabled(context, it)
    } ?: false

private const val KeyboardEnabledPollingDelayMs: Long = 200
