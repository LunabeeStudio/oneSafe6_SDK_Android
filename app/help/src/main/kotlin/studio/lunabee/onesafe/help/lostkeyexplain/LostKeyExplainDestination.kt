package studio.lunabee.onesafe.help.lostkeyexplain

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import studio.lunabee.onesafe.commonui.OSDestination

object LostKeyExplainDestination : OSDestination {
    override val route: String = "lost_key_explain"
}

context(LostKeyExplainNavigation)
fun NavGraphBuilder.lostKeyExplainScreen() {
    composable(
        route = LostKeyExplainDestination.route,
    ) {
        LostKeyExplainRoute()
    }
}
