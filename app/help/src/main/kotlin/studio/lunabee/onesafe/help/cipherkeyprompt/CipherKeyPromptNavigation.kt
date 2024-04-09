package studio.lunabee.onesafe.help.cipherkeyprompt

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Stable
import androidx.navigation.NavController
import studio.lunabee.onesafe.commonui.utils.OSProcessPhoenix

// TODO <cipher> navigation tests
@Stable
class CipherKeyPromptNavigation(
    @Suppress("unused") private val navController: NavController,
    val navigateBack: () -> Unit,
    private val context: Context,
) {
    // TODO <cipher> nav
    fun navigateToWhyKeyMissing() {
        Toast.makeText(context, "Not implemented", Toast.LENGTH_SHORT).show()
    }

    // TODO <cipher> nav
    fun navigateToLostKey() {
        Toast.makeText(context, "Not implemented", Toast.LENGTH_SHORT).show()
    }

    fun exitToMain() {
        OSProcessPhoenix.triggerRebirth(context)
    }
}
