package studio.lunabee.onesafe.feature.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import studio.lunabee.onesafe.commonui.OSDestination

object SettingsDestination : OSDestination {
    const val StartChangePasswordFlowArgs: String = "StartChangePasswordFlowArgs"
    override val route: String = "settings?$StartChangePasswordFlowArgs={$StartChangePasswordFlowArgs}"

    fun getRoute(
        startChangePasswordFlow: Boolean = false,
    ): String = route.replace("{$StartChangePasswordFlowArgs}", startChangePasswordFlow.toString())
}

fun NavGraphBuilder.settingsGraph(
    settingsNavigation: SettingsNavigation,
) {
    composable(
        route = SettingsDestination.route,
        arguments = listOf(
            navArgument(SettingsDestination.StartChangePasswordFlowArgs) {
                type = NavType.BoolType
            },
        ),
    ) {
        SettingsRoute(
            settingsNavigation = settingsNavigation,
        )
    }
}
