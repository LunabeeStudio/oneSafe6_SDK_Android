package studio.lunabee.onesafe.feature.itemdetails

import androidx.compose.material3.SnackbarVisuals
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import studio.lunabee.onesafe.commonui.OSDestination
import studio.lunabee.onesafe.feature.itemactions.ComposeItemActionNavScope
import java.util.UUID

object ItemDetailsDestination : OSDestination {
    const val itemIdArgument: String = "itemId"

    override val route: String = "itemDetails/{$itemIdArgument}"

    fun getRoute(
        itemId: UUID,
    ): String = this.route.replace("{$itemIdArgument}", itemId.toString())
}

context(ComposeItemActionNavScope)
@SuppressWarnings("LongParameterList")
fun NavGraphBuilder.itemDetailsScreen(
    navigateBack: () -> Unit,
    navigateToItemDetails: (itemId: UUID, popCurrent: Boolean) -> Unit,
    navigateToEditItem: (itemId: UUID) -> Unit,
    navigateToFullScreenField: (itemId: UUID, fieldId: UUID) -> Unit,
    navigateToFileViewer: (UUID) -> Unit,
    showSnackbar: (visuals: SnackbarVisuals) -> Unit,
) {
    composable(
        route = ItemDetailsDestination.route,
        arguments = listOf(
            navArgument(ItemDetailsDestination.itemIdArgument) {
                type = NavType.StringType
            },
        ),
        deepLinks = listOf(navDeepLink { uriPattern = ItemDetailsDestination.route }),
    ) {
        ItemDetailsRoute(
            navigateBack = navigateBack,
            navigateToItemDetails = navigateToItemDetails,
            navigateToEditItem = navigateToEditItem,
            navigateToFullScreen = navigateToFullScreenField,
            showSnackbar = showSnackbar,
            navigateToFileViewer = navigateToFileViewer,
        )
    }
}
