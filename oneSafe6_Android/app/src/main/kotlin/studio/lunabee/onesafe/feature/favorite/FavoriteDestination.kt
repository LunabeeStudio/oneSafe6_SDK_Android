package studio.lunabee.onesafe.feature.favorite

import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import studio.lunabee.onesafe.commonui.OSDestination
import studio.lunabee.onesafe.feature.itemactions.ComposeItemActionNavScope
import java.util.UUID

object FavoriteDestination : OSDestination {
    override val route: String = "favorite"
}

context(ComposeItemActionNavScope)
fun NavGraphBuilder.favoriteScreen(
    navigateBack: () -> Unit,
    navigateToItemDetails: (UUID) -> Unit,
    showBreadcrumb: (Boolean) -> Unit,
) {
    composable(
        route = FavoriteDestination.route,
    ) {
        DisposableEffect(Unit) {
            showBreadcrumb(false)
            onDispose {
                showBreadcrumb(true)
            }
        }

        FavoriteRoute(
            navigateBack = navigateBack,
            navigateToItemDetails = navigateToItemDetails,
        )
    }
}
