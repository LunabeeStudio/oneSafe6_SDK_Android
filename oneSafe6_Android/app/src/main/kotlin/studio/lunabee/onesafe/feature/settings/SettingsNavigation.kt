package studio.lunabee.onesafe.feature.settings

import androidx.compose.runtime.Stable
import androidx.navigation.NavController
import studio.lunabee.onesafe.commonui.utils.safeNavigate
import studio.lunabee.onesafe.feature.exportbackup.auth.ExportAuthDestination
import studio.lunabee.onesafe.feature.importbackup.selectfile.ImportFileDestination
import studio.lunabee.onesafe.feature.multisafe.MultiSafePresentationDestination
import studio.lunabee.onesafe.feature.settings.about.AboutDestination
import studio.lunabee.onesafe.feature.settings.autofill.AutofillSettingsDestination
import studio.lunabee.onesafe.feature.settings.bubbles.BubblesSettingsDestination
import studio.lunabee.onesafe.feature.settings.overencryption.OverEncryptionSettingDisabledNavGraphDestination
import studio.lunabee.onesafe.feature.settings.overencryption.OverEncryptionSettingEnabledDestination
import studio.lunabee.onesafe.feature.settings.panicwidget.PanicWidgetSettingsDestination
import studio.lunabee.onesafe.feature.settings.personalization.PersonalizationSettingsDestination
import studio.lunabee.onesafe.feature.settings.security.SecuritySettingDestination
import studio.lunabee.onesafe.importexport.settings.AutoBackupSettingsNavGraphDestination
import studio.lunabee.onesafe.login.screen.LoginDestination
import studio.lunabee.onesafe.navigation.graph.ChangePasswordNavGraphDestination

@Stable
class SettingsNavigation(
    navController: NavController,
    val navigateBack: () -> Unit,
) {
    val navigateToExportAuthDestination: () -> Unit = { navController.safeNavigate(ExportAuthDestination.route) }
    val navigateToImportFile: () -> Unit = { navController.safeNavigate(ImportFileDestination.getRoute(null, false)) }
    val navigateToSecuritySettings: () -> Unit = { navController.safeNavigate(SecuritySettingDestination.route) }
    val navigateToAbout: () -> Unit = { navController.safeNavigate(AboutDestination.route) }
    val navigateToBubblesSettings: () -> Unit = { navController.safeNavigate(BubblesSettingsDestination.route) }
    val navigateToPersonalizationSettings: () -> Unit = { navController.safeNavigate(PersonalizationSettingsDestination.route) }
    val navigateToChangePasswordSettings: () -> Unit = { navController.safeNavigate(ChangePasswordNavGraphDestination.route) }
    val navigateToAutofillSettings: () -> Unit = { navController.safeNavigate(AutofillSettingsDestination.route) }
    val navigateToAutoBackupSettings: () -> Unit = { navController.safeNavigate(AutoBackupSettingsNavGraphDestination.route) }
    val navigateToOnBoardingMultiSafe: () -> Unit = { navController.safeNavigate(MultiSafePresentationDestination.route) }
    val navigateToLogin: () -> Unit = { navController.popBackStack(LoginDestination.route, false) }
    val navigateToOverEncryption: (isEncryptionEnabled: Boolean) -> Unit = { isEncryptionEnabled ->
        if (isEncryptionEnabled) {
            navController.safeNavigate(route = OverEncryptionSettingEnabledDestination.route)
        } else {
            navController.safeNavigate(route = OverEncryptionSettingDisabledNavGraphDestination.route)
        }
    }
    val navigateToPanicWidget: () -> Unit = { navController.safeNavigate(PanicWidgetSettingsDestination.route) }
}
