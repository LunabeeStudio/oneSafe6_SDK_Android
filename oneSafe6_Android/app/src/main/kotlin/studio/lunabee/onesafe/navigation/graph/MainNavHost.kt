package studio.lunabee.onesafe.navigation.graph

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import studio.lunabee.onesafe.navigation.animations.DestinationAnimation

@ExperimentalAnimationApi
@Composable
fun MainNavHost(
    navController: NavHostController,
    startDestination: String,
    builder: NavGraphBuilder.() -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            DestinationAnimation.getEnterTransitionFromRoute(
                targetState.destination.route,
                false,
            )(this)
        },
        exitTransition = {
            DestinationAnimation.getExitTransitionFromRoute(
                targetState.destination.route,
                false,
            )(this)
        },
        popEnterTransition = {
            DestinationAnimation.getPopEnterTransitionFromRoute(
                initialState.destination.route,
                false,
            )(this)
        },
        popExitTransition = {
            DestinationAnimation.getPopExitTransitionFromRoute(
                initialState.destination.route,
                false,
            )(this)
        },
        route = GraphIdentifier.MainNavGraph.name,
        builder = builder,
    )
}
