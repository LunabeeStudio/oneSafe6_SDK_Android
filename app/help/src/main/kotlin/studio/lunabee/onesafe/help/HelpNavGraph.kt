package studio.lunabee.onesafe.help

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import studio.lunabee.onesafe.commonui.animation.slideHorizontalEnterTransition
import studio.lunabee.onesafe.commonui.animation.slideHorizontalExitTransition
import studio.lunabee.onesafe.commonui.animation.slideHorizontalPopEnterTransition
import studio.lunabee.onesafe.commonui.animation.slideHorizontalPopExitTransition
import studio.lunabee.onesafe.help.cipherkeyprompt.CipherKeyPromptDestination
import studio.lunabee.onesafe.help.cipherkeyprompt.CipherKeyPromptNavigation
import studio.lunabee.onesafe.help.cipherkeyprompt.cipherKeyPromptScreen

internal const val HelpNavGraphRoute: String = "help_nav_host"

@Composable
fun HelpNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    NavHost(
        navController = navController,
        startDestination = CipherKeyPromptDestination.route,
        enterTransition = slideHorizontalEnterTransition,
        exitTransition = slideHorizontalExitTransition,
        popEnterTransition = slideHorizontalPopEnterTransition,
        popExitTransition = slideHorizontalPopExitTransition,
        route = HelpNavGraphRoute,
        modifier = modifier,
    ) {
        val cipherKeyPromptNavigation = CipherKeyPromptNavigation(
            navController = navController,
            navigateBack = navController::popBackStack,
            context = context,
        )
        with(cipherKeyPromptNavigation) {
            cipherKeyPromptScreen()
        }
    }
}
