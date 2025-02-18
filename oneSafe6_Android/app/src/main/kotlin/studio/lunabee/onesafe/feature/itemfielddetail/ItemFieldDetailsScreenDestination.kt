package studio.lunabee.onesafe.feature.itemfielddetail

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import studio.lunabee.onesafe.commonui.OSDestination
import studio.lunabee.onesafe.feature.itemfielddetail.screen.ItemFieldDetailsRoute
import java.util.UUID

/**
 * Visualize any specific field of the item in a fullscreen mode.
 */
object ItemFieldDetailsScreenDestination : OSDestination {
    const val ItemIdArg: String = "ItemIdArg"
    const val FieldIdArg: String = "FieldNoteIdArg"

    override val route: String = "itemDetailsFieldFullScreen/{$ItemIdArg}/{$FieldIdArg}"

    fun getRoute(
        itemId: UUID,
        fieldId: UUID,
    ): String = route
        .replace("{$ItemIdArg}", itemId.toString())
        .replace("{$FieldIdArg}", fieldId.toString())
}

fun NavGraphBuilder.itemFieldDetailsScreen(
    navigateBack: () -> Unit,
) {
    composable(
        route = ItemFieldDetailsScreenDestination.route,
        arguments = listOf(
            navArgument(ItemFieldDetailsScreenDestination.ItemIdArg) {
                type = NavType.StringType
            },
            navArgument(ItemFieldDetailsScreenDestination.FieldIdArg) {
                type = NavType.StringType
            },
        ),
    ) {
        ItemFieldDetailsRoute(
            navigateBack = navigateBack,
        )
    }
}
