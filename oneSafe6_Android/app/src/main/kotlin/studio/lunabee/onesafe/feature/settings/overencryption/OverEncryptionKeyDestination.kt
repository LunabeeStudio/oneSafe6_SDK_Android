package studio.lunabee.onesafe.feature.settings.overencryption

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import studio.lunabee.onesafe.commonui.OSDestination

object OverEncryptionKeyDestination : OSDestination {
    const val doBackupArg: String = "backup"
    private const val path: String = "over_encryption_key"
    override val route: String = "$path?$doBackupArg={$doBackupArg}"

    fun getRoute(doBackup: Boolean): String = Uri.Builder()
        .path(path)
        .appendQueryParameter(doBackupArg, doBackup.toString())
        .toString()
}

class OverEncryptionKeyNavigation(
    val navigateBack: () -> Unit,
)

context(OverEncryptionKeyNavigation)
fun NavGraphBuilder.overEncryptionKeyScreen(graphViewModel: @Composable () -> OverEncryptionSettingDisabledViewModel) {
    composable(
        route = OverEncryptionKeyDestination.route,
        arguments = listOf(
            navArgument(OverEncryptionKeyDestination.doBackupArg) {
                type = NavType.BoolType
            },
        ),
    ) {
        OverEncryptionKeyRoute(graphViewModel().key)
    }
}
