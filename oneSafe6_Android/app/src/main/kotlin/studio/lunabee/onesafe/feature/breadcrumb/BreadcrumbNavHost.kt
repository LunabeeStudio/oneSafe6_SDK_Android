package studio.lunabee.onesafe.feature.breadcrumb

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.lunabee.lbloading.LoadingBlockBackView
import studio.lunabee.onesafe.navigation.animations.DestinationAnimation
import studio.lunabee.onesafe.navigation.extension.popUpToItemId
import studio.lunabee.onesafe.navigation.graph.BreadcrumbOnCompositionNav
import studio.lunabee.onesafe.navigation.graph.GraphIdentifier

@ExperimentalAnimationApi
@Composable
fun BreadcrumbNavHost(
    navController: NavHostController,
    startDestination: String,
    isInSearchMode: Boolean,
    graphIdentifier: GraphIdentifier,
    onCompositionNav: () -> BreadcrumbOnCompositionNav?,
    modifier: Modifier = Modifier,
    builder: NavGraphBuilder.() -> Unit,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            DestinationAnimation.getEnterTransitionFromRoute(
                targetState.destination.route,
                isInSearchMode,
            )(this)
        },
        exitTransition = {
            DestinationAnimation.getExitTransitionFromRoute(
                targetState.destination.route,
                isInSearchMode,
            )(this)
        },
        popEnterTransition = {
            DestinationAnimation.getPopEnterTransitionFromRoute(
                initialState.destination.route,
                isInSearchMode,
            )(this)
        },
        popExitTransition = {
            DestinationAnimation.getPopExitTransitionFromRoute(
                initialState.destination.route,
                isInSearchMode,
            )(this)
        },
        route = graphIdentifier.name,
        builder = builder,
    )

    onCompositionNav()?.let { compositionNav ->
        when (compositionNav) {
            is BreadcrumbOnCompositionNav.Navigate -> navController.navigate(compositionNav.route)
            is BreadcrumbOnCompositionNav.PopToExclusive -> navController.popBackStack(compositionNav.route, inclusive = false)
            is BreadcrumbOnCompositionNav.PopToItem -> navController.popUpToItemId(compositionNav.itemId)
        }
    }

    LoadingBlockBackView()
}
