package studio.lunabee.onesafe.help.lostkey

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import studio.lunabee.onesafe.commonui.OSDestination

object LostKeyDestination : OSDestination {
    override val route: String = "lost_key"
}

context(LostKeyNavigation)
fun NavGraphBuilder.lostKeyScreen() {
    composable(
        route = LostKeyDestination.route,
    ) {
        LostKeyRoute()
    }
}
