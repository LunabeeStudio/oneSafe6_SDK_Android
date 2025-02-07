package studio.lunabee.onesafe.feature.settings.about

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.aboutGraph(
    navigateBack: () -> Unit,
    navigateToCredits: () -> Unit,
    onClickOnLibraries: () -> Unit,
) {
    composable(
        route = AboutDestination.route,
    ) {
        AboutRoute(
            navigateBack = navigateBack,
            navigateToCredits = navigateToCredits,
            onClickOnLibraries = onClickOnLibraries,
        )
    }

    composable(
        route = CreditsDestination.route,
    ) {
        CreditsRoute(
            navigateBack = navigateBack,
        )
    }
}
