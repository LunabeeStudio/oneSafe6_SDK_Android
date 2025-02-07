package studio.lunabee.onesafe.feature.breadcrumb

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import studio.lunabee.onesafe.commonui.OSDestination
import studio.lunabee.onesafe.crashlytics.CrashlyticsCustomKeys
import studio.lunabee.onesafe.crashlytics.CrashlyticsHelper
import studio.lunabee.onesafe.crashlytics.CrashlyticsUnknown
import studio.lunabee.onesafe.navigation.graph.GraphIdentifier

object BreadcrumbNavGraphDestination : OSDestination {
    const val ArgItemId: String = "itemId"
    override val route: String = GraphIdentifier.BreadcrumbNavGraph.name
}

fun NavGraphBuilder.breadcrumbScreen(
    breadcrumbNavigation: BreadcrumbNavigation,
    setBreadcrumbNavController: ((NavHostController) -> Unit)?,
) {
    composable(
        route = BreadcrumbNavGraphDestination.route,
        arguments = listOf(
            navArgument(BreadcrumbNavGraphDestination.ArgItemId) {
                type = NavType.StringType
                nullable = true
            },
        ),
    ) {
        val breadcrumbNavController = rememberNavController()

        val state by breadcrumbNavController.currentBackStackEntryAsState()
        state?.let {
            val route = kotlin.runCatching { it.destination.route }.getOrNull()
            LaunchedEffect(route) {
                CrashlyticsHelper.setCustomKey(
                    CrashlyticsCustomKeys.BreadcrumbNavScreen,
                    route ?: CrashlyticsUnknown,
                )
            }
        }

        LaunchedEffect(setBreadcrumbNavController) {
            setBreadcrumbNavController?.invoke(breadcrumbNavController)
        }
        BreadcrumbRoute(
            breadcrumbNavigation = breadcrumbNavigation,
            breadcrumbNavController = breadcrumbNavController,
        )
    }
}
