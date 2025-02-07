package studio.lunabee.onesafe.login.screen

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import studio.lunabee.onesafe.commonui.OSDestination

object LoginDestination : OSDestination {
    override val route: String = "login"
}

fun NavGraphBuilder.loginGraph(
    navigateToHome: (restoreState: Boolean) -> Unit,
    navigateToMultiSafeOnBoarding: () -> Unit,
    unsafeNavigateToHome: () -> Unit,
) {
    composable(
        route = LoginDestination.route,
    ) {
        LoginRoute(
            onSuccess = navigateToHome,
            onBypass = unsafeNavigateToHome,
            onCreateNewSafe = navigateToMultiSafeOnBoarding,
        )
    }
}
