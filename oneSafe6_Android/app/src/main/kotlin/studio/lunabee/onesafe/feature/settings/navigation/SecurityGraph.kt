package studio.lunabee.onesafe.feature.settings.navigation

import androidx.compose.material3.SnackbarVisuals
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import studio.lunabee.onesafe.commonui.OSDestination
import studio.lunabee.onesafe.feature.settings.overencryption.OverEncryptionSettingEnabledNavigation
import studio.lunabee.onesafe.feature.settings.overencryption.OverEncryptionSettingDisabledNavGraphNavigation
import studio.lunabee.onesafe.feature.settings.overencryption.overEncryptionSettingEnabledScreen
import studio.lunabee.onesafe.feature.settings.overencryption.overEncryptionSettingDisabledNavGraph
import studio.lunabee.onesafe.feature.settings.security.SecuritySettingDestination
import studio.lunabee.onesafe.feature.settings.security.SecuritySettingNavigation
import studio.lunabee.onesafe.feature.settings.security.securitySettingScreen

context(SecuritySettingsGraphNavigation)
fun NavGraphBuilder.securitySettingsGraph(
    showSnackBar: (visuals: SnackbarVisuals) -> Unit,
    getBackStackEntry: (route: String) -> NavBackStackEntry,
) {
    navigation(
        startDestination = SecuritySettingDestination.route,
        route = SecuritySettingNavGraphDestination.route,
    ) {
        with(securitySettingNavigation) {
            securitySettingScreen(showSnackBar)
        }

        with(overEncryptionSettingNavGraphNavigation) {
            overEncryptionSettingDisabledNavGraph(getBackStackEntry)
        }

        with(overEncryptionEnabledNavigation) {
            overEncryptionSettingEnabledScreen()
        }
    }
}

object SecuritySettingNavGraphDestination : OSDestination {
    override val route: String = "security_settings_graph"
}

class SecuritySettingsGraphNavigation(
    val securitySettingNavigation: SecuritySettingNavigation,
    val overEncryptionSettingNavGraphNavigation: OverEncryptionSettingDisabledNavGraphNavigation,
    val overEncryptionEnabledNavigation: OverEncryptionSettingEnabledNavigation,
)
