package studio.lunabee.onesafe.feature.home

import androidx.compose.material3.SnackbarVisuals
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import studio.lunabee.onesafe.commonui.OSDestination
import studio.lunabee.onesafe.feature.itemactions.ComposeItemActionNavScope

object HomeDestination : OSDestination {
    override val route: String = "home"
}

context(ComposeItemActionNavScope)
fun NavGraphBuilder.homeScreen(
    homeNavigation: HomeNavigation,
    showSnackBar: (SnackbarVisuals) -> Unit,
) {
    composable(
        route = HomeDestination.route,
    ) {
        HomeRoute(
            homeNavigation = homeNavigation,
            showSnackBar = showSnackBar,
        )
    }
}
