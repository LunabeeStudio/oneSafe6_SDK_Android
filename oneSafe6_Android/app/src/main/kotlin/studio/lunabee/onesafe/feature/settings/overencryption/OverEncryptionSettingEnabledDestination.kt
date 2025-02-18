package studio.lunabee.onesafe.feature.settings.overencryption

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import studio.lunabee.onesafe.commonui.OSDestination

object OverEncryptionSettingEnabledDestination : OSDestination {
    override val route: String = "over_encryption_setting_enabled"
}

class OverEncryptionSettingEnabledNavigation(
    val navigateBack: () -> Unit,
)

context(OverEncryptionSettingEnabledNavigation)
fun NavGraphBuilder.overEncryptionSettingEnabledScreen() {
    composable(
        route = OverEncryptionSettingEnabledDestination.route,
    ) {
        OverEncryptionEnabledRoute()
    }
}
