package studio.lunabee.onesafe.feature.settings.security

import androidx.compose.material3.SnackbarVisuals
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import studio.lunabee.onesafe.commonui.OSDestination

object SecuritySettingDestination : OSDestination {
    override val route: String = "security_setting"
}

class SecuritySettingNavigation(
    val navigateBack: () -> Unit,
    val navigateToAutoDestructionSetting: () -> Unit,
)

context(SecuritySettingNavigation)
fun NavGraphBuilder.securitySettingScreen(
    showSnackBar: (visuals: SnackbarVisuals) -> Unit,
) {
    composable(
        route = SecuritySettingDestination.route,
    ) {
        SecuritySettingRoute(
            showSnackBar = showSnackBar,
        )
    }
}
