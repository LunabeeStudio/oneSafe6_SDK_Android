package studio.lunabee.onesafe.navigation.graph

import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import studio.lunabee.onesafe.common.extensions.hasBiometric
import studio.lunabee.onesafe.commonui.OSDestination
import studio.lunabee.onesafe.commonui.utils.safeNavigate
import studio.lunabee.onesafe.feature.biometric.MultiSafeBiometricCreationDestination
import studio.lunabee.onesafe.feature.biometric.MultiSafeBiometricCreationRoute
import studio.lunabee.onesafe.feature.congratulation.MultiSafeCongratulationRoute
import studio.lunabee.onesafe.feature.congratulation.destination.MultiSafeCongratulationDestination
import studio.lunabee.onesafe.feature.multisafe.MultiSafePresentationDestination
import studio.lunabee.onesafe.feature.multisafe.MultiSafePresentationRoute
import studio.lunabee.onesafe.feature.password.confirmation.multisafe.MultiSafePasswordConfirmationDestination
import studio.lunabee.onesafe.feature.password.confirmation.multisafe.MultiSafePasswordConfirmationRoute
import studio.lunabee.onesafe.feature.password.creation.MultiSafePasswordCreationDestination
import studio.lunabee.onesafe.feature.password.creation.MultiSafePasswordCreationRoute

fun NavGraphBuilder.multiSafeOnBoardingGraph(
    navController: NavController,
    navigateBack: () -> Unit,
) {
    navigation(startDestination = MultiSafePresentationDestination.route, route = MultiSafeOnBoardingNavGraphDestination.route) {
        composable(
            route = MultiSafePresentationDestination.route,
        ) {
            MultiSafePresentationRoute(
                navigateBack = navigateBack,
                createNewSafe = {
                    navController.safeNavigate(MultiSafePasswordCreationDestination.route)
                },
            )
        }

        composable(
            route = MultiSafePasswordCreationDestination.route,
        ) {
            MultiSafePasswordCreationRoute(
                navigateBack = navigateBack,
                onConfirm = {
                    navController.safeNavigate(MultiSafePasswordConfirmationDestination.route)
                },
            )
        }

        composable(
            route = MultiSafePasswordConfirmationDestination.route,
        ) {
            val context = LocalContext.current
            MultiSafePasswordConfirmationRoute(
                navigateBack = navigateBack,
                onConfirm = {
                    if (context.hasBiometric()) {
                        navController.safeNavigate(MultiSafeBiometricCreationDestination.route)
                    } else {
                        navController.safeNavigate(MultiSafeCongratulationDestination.route) {
                            popUpTo(MultiSafeOnBoardingNavGraphDestination.route) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(
            route = MultiSafeBiometricCreationDestination.route,
        ) {
            MultiSafeBiometricCreationRoute(
                navigateBack = navigateBack,
                onFinish = {
                    navController.safeNavigate(MultiSafeCongratulationDestination.route) {
                        popUpTo(MultiSafeOnBoardingNavGraphDestination.route) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = MultiSafeCongratulationDestination.route,
        ) {
            MultiSafeCongratulationRoute(
                popSafeCreationFlow = {
                    navController.popBackStack(route = MultiSafeOnBoardingNavGraphDestination.route, inclusive = true)
                },
            )
        }
    }
}

object MultiSafeOnBoardingNavGraphDestination : OSDestination {
    override val route: String = "multisafe_onboarding_graph_route"
}
