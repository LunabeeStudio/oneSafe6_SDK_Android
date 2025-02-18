package studio.lunabee.onesafe.feature.keyboard

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import studio.lunabee.onesafe.common.utils.settings.UiNotificationHelper
import studio.lunabee.onesafe.commonui.OSDestination
import studio.lunabee.onesafe.feature.keyboard.destination.KeyboardFinishOnBoardingDestination
import studio.lunabee.onesafe.feature.keyboard.destination.KeyboardNotificationDestination
import studio.lunabee.onesafe.feature.keyboard.destination.KeyboardPresentationDestination
import studio.lunabee.onesafe.feature.keyboard.destination.KeyboardSelectionDestination
import studio.lunabee.onesafe.feature.keyboard.screen.KeyboardFinishOnBoardingRoute
import studio.lunabee.onesafe.feature.keyboard.screen.KeyboardNotificationRoute
import studio.lunabee.onesafe.feature.keyboard.screen.KeyboardPresentationRoute
import studio.lunabee.onesafe.feature.keyboard.screen.KeyboardSelectionRoute
import studio.lunabee.onesafe.commonui.utils.safeNavigate

fun NavGraphBuilder.keyboardOnBoardingNavGraph(
    navigateBack: () -> Unit,
    navController: NavController,
) {
    navigation(startDestination = KeyboardPresentationDestination.route, route = KeyboardOnBoardingDestination.route) {
        composable(
            route = KeyboardPresentationDestination.route,
        ) {
            KeyboardPresentationRoute(
                navigateBack = navigateBack,
                onClickOnConfigure = { navController.safeNavigate(KeyboardSelectionDestination.route) },
            )
        }

        composable(
            route = KeyboardSelectionDestination.route,
        ) {
            val areNotificationsEnabled by UiNotificationHelper.areNotificationsEnabled()
            KeyboardSelectionRoute(
                navigateBack = navigateBack,
                onKeyboardSelected = {
                    if (areNotificationsEnabled) {
                        navController.navigate(KeyboardFinishOnBoardingDestination.route) {
                            popUpTo(KeyboardOnBoardingDestination.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(KeyboardNotificationDestination.route) {
                            popUpTo(KeyboardSelectionDestination.route) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(
            route = KeyboardNotificationDestination.route,
        ) {
            KeyboardNotificationRoute(
                navigateBack = navigateBack,
                onDone = {
                    navController.navigate(KeyboardFinishOnBoardingDestination.route) {
                        popUpTo(KeyboardOnBoardingDestination.route) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = KeyboardFinishOnBoardingDestination.route,
        ) {
            KeyboardFinishOnBoardingRoute(
                navigateBack = navigateBack,
            )
        }
    }
}

object KeyboardOnBoardingDestination : OSDestination {
    override val route: String = "keyboard_on_boarding"
}
