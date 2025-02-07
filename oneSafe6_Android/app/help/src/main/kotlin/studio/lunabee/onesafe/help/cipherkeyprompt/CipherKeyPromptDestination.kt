package studio.lunabee.onesafe.help.cipherkeyprompt

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import studio.lunabee.onesafe.commonui.OSDestination

object CipherKeyPromptDestination : OSDestination {
    override val route: String = "cipher_key_prompt"
}

context(CipherKeyPromptNavigation)
fun NavGraphBuilder.cipherKeyPromptScreen() {
    composable(
        route = CipherKeyPromptDestination.route,
    ) {
        CipherKeyPromptRoute()
    }
}
