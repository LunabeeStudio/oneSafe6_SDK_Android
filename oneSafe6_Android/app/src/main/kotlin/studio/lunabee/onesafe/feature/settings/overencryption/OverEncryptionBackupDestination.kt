package studio.lunabee.onesafe.feature.settings.overencryption

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import studio.lunabee.onesafe.commonui.OSDestination

object OverEncryptionBackupDestination : OSDestination {
    override val route: String = "over_encryption_backup"
}

class OverEncryptionBackupNavigation(
    val navigateBack: () -> Unit,
    val navigateToOverEncryptionKey: (Boolean) -> Unit,
)

context(OverEncryptionBackupNavigation)
fun NavGraphBuilder.overEncryptionBackupScreen(graphViewModel: @Composable () -> OverEncryptionSettingDisabledViewModel) {
    composable(
        route = OverEncryptionBackupDestination.route,
    ) {
        graphViewModel()
        OverEncryptionBackupRoute()
    }
}
