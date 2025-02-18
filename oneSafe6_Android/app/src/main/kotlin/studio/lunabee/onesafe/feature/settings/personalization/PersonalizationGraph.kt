package studio.lunabee.onesafe.feature.settings.personalization

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.personalizationGraph(
    navigateBack: () -> Unit,
) {
    composable(
        route = PersonalizationSettingsDestination.route,
    ) {
        PersonalizationSettingsRoute(
            navigateBack = navigateBack,
        )
    }
}
