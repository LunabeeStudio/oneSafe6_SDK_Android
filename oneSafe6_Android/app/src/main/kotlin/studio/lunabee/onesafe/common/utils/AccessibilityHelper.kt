package studio.lunabee.onesafe.common.utils

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.platform.LocalContext
import studio.lunabee.onesafe.common.utils.settings.AndroidSettings
import studio.lunabee.onesafe.messagecompanion.OneSafeAccessibilityHelper

object AccessibilityHelper {
    @Composable
    fun observeOneSafeKAccessibilityEnabled(
        context: Context = LocalContext.current.applicationContext,
        foregroundOnly: Boolean = false,
    ): State<Boolean> = AndroidSettings.Secure.observeAsState(
        key = Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        foregroundOnly = foregroundOnly,
        transform = { OneSafeAccessibilityHelper.parseOneSafeKAccessibilityEnabled(context, it.toString()) },
    )
}
