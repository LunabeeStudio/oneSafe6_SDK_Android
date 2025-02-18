package studio.lunabee.onesafe.feature.breadcrumb

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.feature.bin.binScreen
import studio.lunabee.onesafe.feature.favorite.favoriteScreen
import studio.lunabee.onesafe.feature.home.HomeDestination
import studio.lunabee.onesafe.feature.home.HomeNavigation
import studio.lunabee.onesafe.feature.home.homeScreen
import studio.lunabee.onesafe.feature.itemactions.ComposeItemActionNavScope
import studio.lunabee.onesafe.feature.itemdetails.ItemDetailsDestination
import studio.lunabee.onesafe.feature.itemdetails.itemDetailsScreen
import studio.lunabee.onesafe.commonui.utils.safeNavigate
import studio.lunabee.onesafe.navigation.graph.GraphIdentifier
import java.util.UUID

private val logger = LBLogger.get("BreadcrumbNavGraph")

context(ComposeItemActionNavScope)
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BreadcrumbNavGraph(
    showSnackBar: (visuals: SnackbarVisuals) -> Unit,
    outerNavigation: BreadcrumbNavigation,
    showBreadcrumb: (Boolean) -> Unit,
    navController: NavHostController,
    isInSearchMode: Boolean,
) {
    val navigateBack = {
        val currentLifecycleState = navController.currentBackStackEntry?.lifecycle?.currentState
        if (currentLifecycleState?.isAtLeast(Lifecycle.State.RESUMED) == true) {
            if (!navController.popBackStack()) {
                outerNavigation.navigateBack()
            }
        } else {
            logger.v("PopBackBack is not possible. Current backstack entry lifecycle is $currentLifecycleState")
        }
    }

    BreadcrumbNavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        isInSearchMode = isInSearchMode,
        graphIdentifier = GraphIdentifier.BreadcrumbNavGraph,
        onCompositionNav = outerNavigation.onCompositionNav,
    ) {
        val navigateToItemDetails: (UUID) -> Unit = { navController.safeNavigate(ItemDetailsDestination.getRoute(it)) }

        homeScreen(
            homeNavigation = HomeNavigation(
                navController = navController,
                showBreadcrumb = showBreadcrumb,
                breadcrumbNavigation = outerNavigation,
            ),
            showSnackBar = showSnackBar,
        )

        itemDetailsScreen(
            navigateBack = navigateBack,
            navigateToItemDetails = { itemId, popCurrent ->
                if (popCurrent) {
                    navigateBack()
                }
                navController.safeNavigate(ItemDetailsDestination.getRoute(itemId))
            },
            navigateToEditItem = outerNavigation.navigateToEditItem,
            navigateToFullScreenField = outerNavigation.navigateToFullScreenField,
            navigateToFileViewer = outerNavigation.navigateToFileViewer,
            showSnackbar = showSnackBar,
        )

        binScreen(
            navigateBack = navigateBack,
            navigateToItemDetails = navigateToItemDetails,
            showSnackBar = showSnackBar,
        )

        favoriteScreen(
            navigateBack = navigateBack,
            navigateToItemDetails = navigateToItemDetails,
            showBreadcrumb = showBreadcrumb,
        )
    }
}
