package studio.lunabee.onesafe.feature.verifypassword

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import androidx.navigation.compose.composable
import studio.lunabee.onesafe.feature.verifypassword.check.CheckPasswordDestination
import studio.lunabee.onesafe.feature.verifypassword.check.CheckPasswordRoute
import studio.lunabee.onesafe.feature.verifypassword.rightpassword.RightPasswordDestination
import studio.lunabee.onesafe.feature.verifypassword.rightpassword.RightPasswordScreen
import studio.lunabee.onesafe.feature.verifypassword.wrongpassword.WrongPasswordDestination
import studio.lunabee.onesafe.feature.verifypassword.wrongpassword.WrongPasswordRoute
import studio.lunabee.onesafe.commonui.utils.safeNavigate
import studio.lunabee.onesafe.navigation.graph.ChangePasswordNavGraphDestination
import studio.lunabee.onesafe.commonui.OSDestination

fun NavGraphBuilder.verifyPasswordNavGraph(
    navController: NavController,
    navigateBack: () -> Unit,
) {
    navigation(
        startDestination = CheckPasswordDestination.route,
        route = VerifyPasswordNavGraphDestination.route,
    ) {
        composable(
            route = CheckPasswordDestination.route,
        ) {
            CheckPasswordRoute(
                navigateBack = navigateBack,
                navigateToWrongPassword = { navController.safeNavigate(WrongPasswordDestination.route) },
                navigateToRightPassword = {
                    navController.safeNavigate(RightPasswordDestination.route) {
                        popUpTo(CheckPasswordDestination.route) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = WrongPasswordDestination.route,
        ) {
            WrongPasswordRoute(
                navigateBack = navigateBack,
                navigateToChangePassword = {
                    navController.safeNavigate(ChangePasswordNavGraphDestination.route) {
                        popUpTo(CheckPasswordDestination.route) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = RightPasswordDestination.route,
        ) {
            RightPasswordScreen(
                navigateBack = navigateBack,
            )
        }
    }
}

object VerifyPasswordNavGraphDestination : OSDestination {
    override val route: String = "verify_password_graph"
}
