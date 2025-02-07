package studio.lunabee.onesafe.feature.settings.overencryption

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import studio.lunabee.onesafe.commonui.OSDestination

object OverEncryptionExplanationDestination : OSDestination {
    override val route: String = "over_encryption_explanation"
}

class OverEncryptionExplanationNavigation(
    val navigateBack: () -> Unit,
    val navigateToOverEncryptionBackup: () -> Unit,
    val navigateToOverEncryptionKey: (Boolean) -> Unit,
)

context(OverEncryptionExplanationNavigation)
fun NavGraphBuilder.overEncryptionExplanationScreen(graphViewModel: @Composable () -> OverEncryptionSettingDisabledViewModel) {
    composable(
        route = OverEncryptionExplanationDestination.route,
    ) {
        graphViewModel()
        OverEncryptionExplanationRoute()
    }
}
