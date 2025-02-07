package studio.lunabee.onesafe.navigation.extension

import androidx.navigation.NavController
import com.lunabee.lbextensions.enumValueOfOrNull
import studio.lunabee.onesafe.commonui.utils.safeNavigate
import studio.lunabee.onesafe.feature.home.HomeDestination
import studio.lunabee.onesafe.feature.itemdetails.ItemDetailsDestination
import studio.lunabee.onesafe.feature.move.selectdestination.SelectMoveDestination
import studio.lunabee.onesafe.navigation.graph.GraphIdentifier
import java.util.UUID

/**
 * Popup to [itemId] if the destination is in the back queue, or popup to the last destination other than item details and then nav to
 * the item details with id [itemId].
 * [itemId] might be null depending on your [GraphIdentifier].
 */
fun NavController.popUpToItemId(itemId: UUID?) {
    val graphIdentifier = enumValueOfOrNull<GraphIdentifier>(graph.route)
    checkNotNull(graphIdentifier) { "Unexpected graph route ${graph.route}" }
    val route: String = when (graphIdentifier) {
        GraphIdentifier.BreadcrumbNavGraph -> ItemDetailsDestination.getRoute(itemId!!) // null is currently not expected for this case.
        GraphIdentifier.MoveNavGraph -> SelectMoveDestination.getRoute(itemId)
        GraphIdentifier.MainNavGraph,
        GraphIdentifier.AutoFillNavGraph,
        -> error("Unexpected graph route ${graph.route}")
    }
    try {
        getBackStackEntry(route) // throw an IllegalArgumentException if route is not found (i.e requireNotNull). It's ok for our use case.
        popBackStack(route, inclusive = false)
    } catch (e: IllegalArgumentException) { // other exception is not expected
        // This code is called when Breadcrumb is not matching the currentBackStackEntries (ex: add to favorite a child item)
        val destination = when (graphIdentifier) {
            GraphIdentifier.BreadcrumbNavGraph -> ItemDetailsDestination.getRoute(itemId!!) // null is currently not expected for this case.
            // currently not expected in the Move feature/graph.
            GraphIdentifier.MoveNavGraph,
            GraphIdentifier.AutoFillNavGraph, // already throw above
            GraphIdentifier.MainNavGraph, // already throw above
            -> throw IllegalStateException("Unexpected graph route ${graph.route}", e)
        }
        safeNavigate(destination) {
            // Currently popup to Home.
            popUpTo(HomeDestination.route)
        }
    }
}
