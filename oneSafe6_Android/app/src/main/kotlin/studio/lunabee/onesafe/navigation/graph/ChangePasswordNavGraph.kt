package studio.lunabee.onesafe.navigation.graph

import androidx.compose.material3.SnackbarVisuals
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import studio.lunabee.onesafe.commonui.OSDestination
import studio.lunabee.onesafe.commonui.utils.safeNavigate
import studio.lunabee.onesafe.feature.biometric.ChangePasswordBiometricCreationDestination
import studio.lunabee.onesafe.feature.biometric.ChangePasswordBiometricCreationRoute
import studio.lunabee.onesafe.feature.password.confirmation.changepassword.ChangePasswordConfirmationDestination
import studio.lunabee.onesafe.feature.password.confirmation.changepassword.ChangePasswordConfirmationRoute
import studio.lunabee.onesafe.feature.password.creation.ChangePasswordCreationDestination
import studio.lunabee.onesafe.feature.password.creation.ChangePasswordPasswordCreationRoute

fun NavGraphBuilder.changePasswordNavGraph(
    navController: NavController,
    navigateBack: () -> Unit,
    showSnackBar: (visuals: SnackbarVisuals) -> Unit,
) {
    navigation(startDestination = ChangePasswordCreationDestination.route, route = ChangePasswordNavGraphDestination.route) {
        composable(
            route = ChangePasswordCreationDestination.route,
        ) {
            ChangePasswordPasswordCreationRoute(
                navigateBack = navigateBack,
                onConfirm = {
                    navController.safeNavigate(ChangePasswordConfirmationDestination.route)
                },
            )
        }

        composable(
            route = ChangePasswordConfirmationDestination.route,
        ) {
            ChangePasswordConfirmationRoute(
                navigateBack = navigateBack,
                navigateNext = { hasBiometric ->
                    if (hasBiometric) {
                        navController.safeNavigate(ChangePasswordBiometricCreationDestination.route)
                    } else {
                        navController.popBackStack(ChangePasswordNavGraphDestination.route, inclusive = true)
                    }
                },
                showSnackBar = showSnackBar,
            )
        }

        composable(
            route = ChangePasswordBiometricCreationDestination.route,
        ) {
            ChangePasswordBiometricCreationRoute {
                navController.popBackStack(ChangePasswordNavGraphDestination.route, inclusive = true)
            }
        }
    }
}

object ChangePasswordNavGraphDestination : OSDestination {
    override val route: String = "change_password_graph_route"
}
