package studio.lunabee.onesafe.commonui

import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import studio.lunabee.onesafe.commonui.extension.findFragmentActivity
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.oSDefaultEnableEdgeToEdge
import studio.lunabee.onesafe.window.LocalWindow

@Composable
fun OSForcedLightScreen(
    content: @Composable () -> Unit,
) {
    val window = LocalWindow.current
    val activity = LocalContext.current.findFragmentActivity()
    DisposableEffect(Unit) {
        val params: WindowManager.LayoutParams = window.attributes
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        window.attributes = params
        activity.oSDefaultEnableEdgeToEdge(forceLight = true)

        onDispose {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            window.attributes = params
            activity.oSDefaultEnableEdgeToEdge()
        }
    }

    OSTheme(
        isSystemInDarkTheme = false,
        isMaterialYouSettingsEnabled = LocalDesignSystem.current.isMaterialYouEnabled,
        content = content,
    )
}
