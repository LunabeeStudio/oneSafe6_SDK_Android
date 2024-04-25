package studio.lunabee.onesafe.help.cipherkeyprompt

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.navigation.NavController
import studio.lunabee.onesafe.commonui.utils.OSProcessPhoenix
import studio.lunabee.onesafe.help.lostkey.LostKeyDestination
import studio.lunabee.onesafe.help.lostkeyexplain.LostKeyExplainDestination

@Stable
class CipherKeyPromptNavigation(
    private val navController: NavController,
    val navigateBack: () -> Unit,
    private val context: Context,
) {
    fun navigateToWhyKeyMissing() {
        navController.navigate(LostKeyExplainDestination.route)
    }

    fun navigateToLostKey() {
        navController.navigate(LostKeyDestination.route)
    }

    fun exitToMain() {
        OSProcessPhoenix.triggerRebirth(context)
    }
}
