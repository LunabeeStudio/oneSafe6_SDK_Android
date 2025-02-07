package studio.lunabee.onesafe.feature.move.movehost

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import studio.lunabee.onesafe.feature.breadcrumb.BreadcrumbNavHost
import studio.lunabee.onesafe.feature.move.selectdestination.SelectMoveDestination
import studio.lunabee.onesafe.feature.move.selectdestination.SelectMoveDestinationRoute
import studio.lunabee.onesafe.commonui.utils.safeNavigate
import studio.lunabee.onesafe.navigation.graph.GraphIdentifier
import java.util.UUID

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MoveNavGraph(
    itemId: UUID?,
    itemName: String,
    onMove: () -> Unit,
    showSnackBar: (SnackbarVisuals) -> Unit,
    moveNavController: NavHostController,
    lazyGridState: LazyGridState,
    nestedScrollConnection: NestedScrollConnection,
    modifier: Modifier = Modifier,
) {
    BreadcrumbNavHost(
        navController = moveNavController,
        startDestination = SelectMoveDestination.route,
        isInSearchMode = false,
        graphIdentifier = GraphIdentifier.MoveNavGraph,
        onCompositionNav = { null },
        modifier = modifier,
    ) {
        composable(
            route = SelectMoveDestination.route,
            arguments = listOf(
                navArgument(SelectMoveDestination.DestinationItemIdArgument) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            SelectMoveDestinationRoute(
                itemToMoveId = itemId,
                itemToMoveName = itemName,
                showSnackBar = showSnackBar,
                navigateToDestination = { destinationId ->
                    moveNavController.safeNavigate(
                        SelectMoveDestination.getRoute(destinationItemId = destinationId),
                    )
                },
                lazyGridState = lazyGridState,
                nestedScrollConnection = nestedScrollConnection,
                onMove = onMove,
            )
        }
    }
}
