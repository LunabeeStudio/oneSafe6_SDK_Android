package studio.lunabee.onesafe.feature.forceupgrade

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import studio.lunabee.onesafe.commonui.OSDestination

object ForceUpgradeDestination : OSDestination {
    override val route: String = "forceUpgrade"
}

fun NavGraphBuilder.forceUpgradeGraph(
    navigateToStart: () -> Unit,
) {
    composable(
        route = ForceUpgradeDestination.route,
    ) {
        ForceUpgradeRoute(
            onSkipClick = navigateToStart,
        )
    }
}
