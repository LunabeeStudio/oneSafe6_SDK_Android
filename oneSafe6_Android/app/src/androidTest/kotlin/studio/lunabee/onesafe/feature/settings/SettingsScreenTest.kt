package studio.lunabee.onesafe.feature.settings

import android.content.Context
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onRoot
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.printToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.feature.settings.prevention.UiPreventionSettingsWarning
import studio.lunabee.onesafe.model.AppIcon
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.theme.OSTheme

@OptIn(ExperimentalTestApi::class)
class SettingsScreenTest : LbcComposeTest() {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun no_warning_displayed_test() {
        setSettingsScreen(null) {
            onRoot().printToCacheDir(printRule)
            hasTestTag(UiConstants.TestTag.Item.FeedbackWarningPrevention)
                .waitUntilDoesNotExist()
        }
    }

    @Test
    fun password_warning_displayed_test() {
        checkPreventionWarning(UiPreventionSettingsWarning.PasswordVerification)
    }

    @Test
    fun backup_warning_displayed_test() {
        checkPreventionWarning(UiPreventionSettingsWarning.Backup)
    }

    @Test
    fun password_backup_warning_displayed_test() {
        checkPreventionWarning(UiPreventionSettingsWarning.PasswordVerificationAndBackup)
    }

    private fun checkPreventionWarning(preventionWarning: UiPreventionSettingsWarning) {
        setSettingsScreen(preventionWarning) {
            onRoot().printToCacheDir(printRule)
            hasTestTag(UiConstants.TestTag.Item.FeedbackWarningPrevention)
                .waitUntilExactlyOneExists()
            hasText(preventionWarning.title.string(context = context))
                .waitUntilExactlyOneExists()
            hasText(preventionWarning.description.string(context = context))
                .waitUntilExactlyOneExists()
        }
    }

    private fun setSettingsScreen(preventionWarning: UiPreventionSettingsWarning?, block: ComposeUiTest.() -> Unit) {
        invoke {
            setContent {
                OSTheme {
                    SettingsScreen(
                        showSafeCta = false,
                        preventionWarning = preventionWarning,
                        isOverEncryptionEnabled = false,
                        navigateBack = {},
                        importData = {},
                        exportData = {},
                        navigateToSecuritySettings = {},
                        navigateToAbout = {},
                        navigateToAutoFillScreen = {},
                        navigateToBubblesSettings = {},
                        navigateToPersonalizationSettings = {},
                        navigateToOverEncryption = {},
                        startChangePasswordFlow = {},
                        onClickOnRateUs = {},
                        navigateToAutoBackupSettings = {},
                        onSafeDeletion = {},
                        onCloseIndependentVaultsMessage = {},
                        createNewSafe = {},
                        currentAliasSelected = AppIcon.Default,
                        showIconBottomSheet = {},
                        navigateToPanicWidgetSettings = {},
                        isWidgetEnabled = false,
                        isPanicModeEnabled = false,
                        onClosePreventWarningMessage = {},
                        showPreventionBottomSheet = {},
                    )
                }
            }
            block()
        }
    }
}
