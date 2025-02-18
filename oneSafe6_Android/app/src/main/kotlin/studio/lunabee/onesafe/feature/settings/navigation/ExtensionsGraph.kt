package studio.lunabee.onesafe.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import studio.lunabee.onesafe.feature.settings.bubbles.BubblesSettingsRoute
import studio.lunabee.onesafe.feature.settings.bubbles.BubblesSettingsDestination

fun NavGraphBuilder.bubblesSettingsGraph(
    navigateBack: () -> Unit,
    navigateToKeyboardOnBoarding: () -> Unit,
) {
    composable(
        route = BubblesSettingsDestination.route,
    ) {
        BubblesSettingsRoute(
            navigateBack = navigateBack,
            navigateToKeyboardOnBoarding = navigateToKeyboardOnBoarding,
        )
    }
}
