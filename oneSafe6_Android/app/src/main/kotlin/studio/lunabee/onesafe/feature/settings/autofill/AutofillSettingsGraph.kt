package studio.lunabee.onesafe.feature.settings.autofill

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.autofillSettingsGraph(
    navigateBack: () -> Unit,
) {
    composable(
        route = AutofillSettingsDestination.route,
    ) {
        AutofillSettingsRoute(
            navigateBack = navigateBack,
        )
    }
}
