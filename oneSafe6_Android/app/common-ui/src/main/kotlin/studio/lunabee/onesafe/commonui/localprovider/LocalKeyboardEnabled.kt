package studio.lunabee.onesafe.commonui.localprovider

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * True when compose is actually running in oneSafe K
 */
val LocalIsOneSafeK: ProvidableCompositionLocal<Boolean> = staticCompositionLocalOf { false }

/**
 * Provides information and control over the keyboard in oneSafe K
 */
val LocalOneSafeKImeController: ProvidableCompositionLocal<OneSafeKImeController> = staticCompositionLocalOf {
    OneSafeKImeController(isVisible = false, showKeyboard = {}, hideKeyboard = {})
}

class OneSafeKImeController(
    val isVisible: Boolean,
    val showKeyboard: () -> Unit,
    val hideKeyboard: () -> Unit,
)
