package studio.lunabee.onesafe.navigation.graph

import android.annotation.SuppressLint
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import studio.lunabee.onesafe.common.extensions.hasBiometric
import studio.lunabee.onesafe.commonui.OSDestination
import studio.lunabee.onesafe.commonui.utils.safeNavigate
import studio.lunabee.onesafe.feature.biometric.OnBoardingBiometricCreationRoute
import studio.lunabee.onesafe.feature.biometric.OnboardingBiometricCreationDestination
import studio.lunabee.onesafe.feature.congratulation.OnboardingCongratulationRoute
import studio.lunabee.onesafe.feature.congratulation.destination.CongratulationOnBoardingDestination
import studio.lunabee.onesafe.feature.onboarding.presentation.AppPresentationDestination
import studio.lunabee.onesafe.feature.onboarding.presentation.AppPresentationRoute
import studio.lunabee.onesafe.feature.password.confirmation.onboarding.OnboardingPasswordConfirmationDestination
import studio.lunabee.onesafe.feature.password.confirmation.onboarding.OnboardingPasswordConfirmationRoute
import studio.lunabee.onesafe.feature.password.creation.OnboardingPasswordCreationDestination
import studio.lunabee.onesafe.feature.password.creation.OnboardingPasswordCreationRoute
import studio.lunabee.onesafe.login.screen.LoginDestination

@SuppressLint("UnrememberedGetBackStackEntry")
fun NavGraphBuilder.onBoardingNavGraph(
    navController: NavController,
    navigateBack: () -> Unit,
) {
    composable(
        route = CongratulationOnBoardingDestination.route,
    ) {
        OnboardingCongratulationRoute(
            navigateToLogin = { navController.safeNavigate(LoginDestination.route) },
        )
    }

    navigation(startDestination = AppPresentationDestination.route, route = OnBoardingNavGraphDestination.route) {
        composable(
            route = AppPresentationDestination.route,
        ) {
            AppPresentationRoute(
                navigateToNextStep = { navController.safeNavigate(OnboardingPasswordCreationDestination.route) },
            )
        }

        composable(
            route = OnboardingPasswordCreationDestination.route,
        ) {
            OnboardingPasswordCreationRoute(
                navigateBack = navigateBack,
                onConfirm = {
                    navController.safeNavigate(OnboardingPasswordConfirmationDestination.route)
                },
            )
        }

        composable(
            route = OnboardingPasswordConfirmationDestination.route,
        ) {
            val context = LocalContext.current
            OnboardingPasswordConfirmationRoute(
                navigateBack = navigateBack,
                onConfirm = {
                    if (context.hasBiometric()) {
                        navController.safeNavigate(OnboardingBiometricCreationDestination.route)
                    } else {
                        navController.safeNavigate(CongratulationOnBoardingDestination.route) {
                            popUpTo(OnBoardingNavGraphDestination.route) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(
            route = OnboardingBiometricCreationDestination.route,
        ) {
            OnBoardingBiometricCreationRoute(
                navigateBack = navigateBack,
                onFinish = {
                    navController.safeNavigate(CongratulationOnBoardingDestination.route) {
                        popUpTo(OnBoardingNavGraphDestination.route) { inclusive = true }
                    }
                },
            )
        }
    }
}

object OnBoardingNavGraphDestination : OSDestination {
    override val route: String = "on_boarding_graph_route"
}
