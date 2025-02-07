package studio.lunabee.onesafe.feature.itemform.destination

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.composable
import studio.lunabee.onesafe.feature.itemform.screen.ItemEditionRoute
import studio.lunabee.onesafe.commonui.OSDestination
import java.util.UUID

object ItemEditionDestination : OSDestination {
    const val ItemIdArg: String = "itemId"

    override val route: String = "itemEdition/{$ItemIdArg}"

    fun getRoute(
        itemId: UUID,
    ): String {
        return route.replace("{$ItemIdArg}", itemId.toString())
    }
}

fun NavGraphBuilder.itemEditionGraph(
    navigateBack: () -> Unit,
) {
    composable(
        route = ItemEditionDestination.route,
        arguments = listOf(
            navArgument(ItemEditionDestination.ItemIdArg) {
                type = NavType.StringType
            },
        ),
    ) {
        ItemEditionRoute(
            navigateBack = navigateBack,
        )
    }
}
