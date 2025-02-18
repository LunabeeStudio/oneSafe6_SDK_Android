package studio.lunabee.onesafe.feature.share

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navigation
import androidx.navigation.compose.composable
import studio.lunabee.onesafe.feature.share.encrypt.EncryptShareDestination
import studio.lunabee.onesafe.feature.share.encrypt.EncryptShareRoute
import studio.lunabee.onesafe.feature.share.file.ShareFileDestination
import studio.lunabee.onesafe.feature.share.file.ShareFileRoute
import studio.lunabee.onesafe.commonui.utils.safeNavigate
import studio.lunabee.onesafe.commonui.OSDestination
import java.util.UUID

fun NavGraphBuilder.shareNavGraph(
    navController: NavController,
    navigateBack: () -> Unit,
) {
    navigation(startDestination = EncryptShareDestination.route, route = ShareNavGraphDestination.route) {
        argument(EncryptShareDestination.ItemToShareIdArgument) { type = NavType.StringType }
        argument(EncryptShareDestination.IncludeChildrenArgument) { type = NavType.BoolType }

        composable(
            route = EncryptShareDestination.route,
            arguments = listOf(
                navArgument(EncryptShareDestination.ItemToShareIdArgument) { type = NavType.StringType },
                navArgument(EncryptShareDestination.IncludeChildrenArgument) { type = NavType.BoolType },
            ),
        ) {
            EncryptShareRoute(
                navigateBack = navigateBack,
                onSuccess = { sharingData ->
                    navController.safeNavigate(ShareFileDestination.getRoute(sharingData)) {
                        popUpTo(ShareNavGraphDestination.route) {
                            inclusive = true
                        }
                    }
                },
            )
        }

        composable(
            route = ShareFileDestination.route,
            arguments = listOf(
                navArgument(ShareFileDestination.PasswordArg) { type = NavType.StringType },
                navArgument(ShareFileDestination.ItemsNbrArg) { type = NavType.IntType },
                navArgument(ShareFileDestination.FilePathArgs) { type = NavType.StringType },
            ),
        ) {
            ShareFileRoute(
                navigateBack = navigateBack,
            )
        }
    }
}

object ShareNavGraphDestination : OSDestination {
    private const val ItemToShareIdArgument: String = "itemToShare"
    private const val IncludeChildrenArgument: String = "includeChildren"

    override val route: String = "share_graph/$ItemToShareIdArgument={$ItemToShareIdArgument}/" +
        "$IncludeChildrenArgument={$IncludeChildrenArgument}"

    fun getRoute(itemId: UUID, includeChildren: Boolean): String =
        route
            .replace("{$ItemToShareIdArgument}", itemId.toString())
            .replace("{$IncludeChildrenArgument}", includeChildren.toString())
}
