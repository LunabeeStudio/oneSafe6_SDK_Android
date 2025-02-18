package studio.lunabee.onesafe.feature.settings.overencryption

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import studio.lunabee.onesafe.commonui.OSDestination

context(OverEncryptionSettingDisabledNavGraphNavigation)
fun NavGraphBuilder.overEncryptionSettingDisabledNavGraph(
    getBackStackEntry: (route: String) -> NavBackStackEntry,
) {
    navigation(
        startDestination = OverEncryptionExplanationDestination.route,
        route = OverEncryptionSettingDisabledNavGraphDestination.route,
    ) {
        val overEncryptionExplanationNavigation = OverEncryptionExplanationNavigation(
            navigateBack = navigateBack,
            navigateToOverEncryptionBackup = navigateToOverEncryptionBackup,
            navigateToOverEncryptionKey = navigateToOverEncryptionKey,
        )
        val overEncryptionBackupNavigation = OverEncryptionBackupNavigation(
            navigateBack = navigateBack,
            navigateToOverEncryptionKey = navigateToOverEncryptionKey,
        )
        val overEncryptionKeyNavigation = OverEncryptionKeyNavigation(
            navigateBack = navigateBack,
        )

        with(overEncryptionExplanationNavigation) {
            overEncryptionExplanationScreen { graphViewModel(getBackStackEntry) }
        }
        with(overEncryptionBackupNavigation) {
            overEncryptionBackupScreen { graphViewModel(getBackStackEntry) }
        }
        with(overEncryptionKeyNavigation) {
            overEncryptionKeyScreen { graphViewModel(getBackStackEntry) }
        }
    }
}

@Composable
private fun graphViewModel(
    getBackStackEntry: (route: String) -> NavBackStackEntry,
): OverEncryptionSettingDisabledViewModel {
    val viewModelStoreOwner = remember {
        getBackStackEntry(OverEncryptionSettingDisabledNavGraphDestination.route)
    }
    return hiltViewModel<OverEncryptionSettingDisabledViewModel>(viewModelStoreOwner)
}

object OverEncryptionSettingDisabledNavGraphDestination : OSDestination {
    override val route: String = "over_encryption_settings_disabled_graph"
}

class OverEncryptionSettingDisabledNavGraphNavigation(
    val navigateBack: () -> Unit,
    val navigateToOverEncryptionBackup: () -> Unit,
    val navigateToOverEncryptionKey: (Boolean) -> Unit,
)
