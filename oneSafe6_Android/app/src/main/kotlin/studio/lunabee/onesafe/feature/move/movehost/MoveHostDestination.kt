package studio.lunabee.onesafe.feature.move.movehost

import androidx.compose.material3.SnackbarVisuals
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import studio.lunabee.onesafe.commonui.OSDestination
import studio.lunabee.onesafe.navigation.graph.GraphIdentifier
import java.util.UUID

object MoveHostDestination : OSDestination {
    const val ItemIdArgument: String = "itemId"

    private val path: String = GraphIdentifier.MoveNavGraph.name

    override val route: String = "$path/$ItemIdArgument={$ItemIdArgument}"

    fun getRoute(itemId: UUID): String =
        this.route
            .replace("{$ItemIdArgument}", itemId.toString())
}

fun NavGraphBuilder.moveGraph(
    navigateBack: () -> Unit,
    showSnackBar: (visuals: SnackbarVisuals) -> Unit,
    navigateToHome: () -> Unit,
    navigateToItem: (UUID) -> Unit,
) {
    composable(
        route = MoveHostDestination.route,
        arguments = listOf(
            navArgument(MoveHostDestination.ItemIdArgument) {
                type = NavType.StringType
            },
        ),
    ) {
        MoveHostRoute(
            navigateToHome = navigateToHome,
            showSnackBar = showSnackBar,
            navigateToItem = navigateToItem,
            navigateBack = navigateBack,
        )
    }
}
