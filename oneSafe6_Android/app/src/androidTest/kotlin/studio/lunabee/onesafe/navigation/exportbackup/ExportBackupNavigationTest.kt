package studio.lunabee.onesafe.navigation.exportbackup

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.storage.dao.SafeItemDao
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.test
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.ui.UiConstants
import javax.inject.Inject

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class ExportBackupNavigationTest : OSMainActivityTest() {
    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var safeItemDao: SafeItemDao

    override val initialTestState: InitialTestState = InitialTestState.Home {
        createItemUseCase.test(name = "Test")
    }

    /**
     * Check export empty screen when user has not data to export.
     */
    @Test
    fun export_auth_without_data_test() {
        runTest {
            safeItemDao.removeById(testUUIDs[0])
        }
        invoke {
            navigateFromHomeToSettings()

            // Navigate to export screen.
            hasText(text = getString(OSString.settings_backupCard_exportLabel))
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .performClick()

            // Check that empty screen is displayed.
            hasTestTag(testTag = UiConstants.TestTag.Screen.ExportEmptyScreen)
                .waitAndPrintRootToCacheDir(printRule = printRule, suffix = "_export_empty_route")
                .assertIsDisplayed()

            hasText(text = getString(OSString.common_back))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()

            // Back to settings screen.
            hasTestTag(testTag = UiConstants.TestTag.Screen.Settings)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    /*
     * Navigate to [studio.lunabee.onesafe.feature.exportbackup.auth.ExportAuthRoute] and make some UI check.
     * Use a valid password directly.
     */
    @Test
    fun export_auth_state_with_valid_password_test() {
        invoke {
            navigateFromHomeToSettings()
            navigateFromSettingsToExportAuth()
            navigateFromExportAuthToExportData()
        }
    }

    /**
     * Use a wrong password, check error and then move to next step with the valid password.
     */
    @Test
    fun export_auth_state_with_invalid_password_test() {
        invoke {
            navigateFromHomeToSettings()
            navigateFromSettingsToExportAuth()

            // Fill with an invalid password.
            hasText(text = getString(OSString.backup_protectBackup_passwordCard_passwordInputLabel))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performTextReplacement(text = "wrongPassword")

            Espresso.closeSoftKeyboard()
            keyboardHelper.waitForKeyboardVisibility(visible = false)

            // Check button state and perform click.
            hasText(text = getString(OSString.common_next))
                .waitUntilExactlyOneExists()
                .performScrollTo() // avoid flakiness with keyboard
                .assertIsDisplayed()
                .assertIsEnabled()
                .performClick()

            // Check that error is displayed
            hasText(text = getString(OSString.backup_protectBackup_passwordCard_passwordErrorLabel))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()

            // Replace with valid password
            hasText(text = getString(OSString.backup_protectBackup_passwordCard_passwordInputLabel))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performTextReplacement(text = testPassword)

            // Avoid flakiness.
            Espresso.closeSoftKeyboard()
            keyboardHelper.waitForKeyboardVisibility(visible = false)

            // Error should not be displayed anymore.
            hasText(text = getString(OSString.backup_protectBackup_passwordCard_passwordErrorLabel))
                .waitUntilDoesNotExist()
                .assertDoesNotExist()

            // Check button state and move to next step.
            hasText(text = getString(OSString.common_next))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .assertIsEnabled()
                .performClick()

            // At this point, we should be on ExportDataRoute.
            hasTestTag(testTag = UiConstants.TestTag.Screen.ExportDataScreen)
                .waitAndPrintRootToCacheDir(printRule = printRule, suffix = "_export_data_route")
                .assertIsDisplayed()
        }
    }

    /**
     * Authenticate and check UI in [studio.lunabee.onesafe.feature.exportbackup.getarchive.ExportGetArchiveRoute]
     */
    @Test
    fun export_file_test() {
        invoke {
            // Navigate directly to last export step.
            navigateFromHomeToSettings()
            navigateFromSettingsToExportAuth()
            navigateFromExportAuthToExportData()

            // At this point, we should be on ExportGetArchiveRoute.
            hasTestTag(testTag = UiConstants.TestTag.Screen.ExportGetArchiveScreen)
                .waitAndPrintRootToCacheDir(printRule = printRule, suffix = "_export_get_archive_route")
                .assertIsDisplayed()

            // Done button should not be displayed at this point.
            hasText(text = getString(OSString.backup_exportBackup_doneButton))
                .waitUntilDoesNotExist()

            // Check action button state and perform click on any of them (intent logic is not tested).
            hasText(text = getString(OSString.backup_exportBackup_saveButton))
                .waitUntilExactlyOneExists()
                .assertIsEnabled()
                .assertIsDisplayed()

            hasText(text = getString(OSString.backup_exportBackup_shareButton))
                .waitUntilExactlyOneExists()
                .assertIsEnabled()
                .assertIsDisplayed()
                .performClick()

            // No viable option for now to test back from intent.
            // Try to dig https://slack-chats.kotlinlang.org/t/509519/how-do-you-mock-rememberlauncherforactivityresult-in-compose
            // Currently, the end of the navigation process is mocked and tested in ExportGetArchiveScreenTest
        }
    }

    @Test
    fun export_file_back_test() {
        invoke {
            // Navigate directly to last export step.
            navigateFromHomeToSettings()
            navigateFromSettingsToExportAuth()
            navigateFromExportAuthToExportData()

            // At this point, we should be on ExportGetArchiveRoute.
            hasTestTag(testTag = UiConstants.TestTag.Screen.ExportGetArchiveScreen)
                .waitAndPrintRootToCacheDir(printRule = printRule, suffix = "_export_get_archive_route")
                .assertIsDisplayed()

            // Click on back.
            hasContentDescription(value = getString(OSString.common_accessibility_back))
                .waitUntilExactlyOneExists()
                .performClick()

            // Check that we are back to settings screen.
            hasTestTag(testTag = UiConstants.TestTag.Screen.Settings)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    private fun ComposeUiTest.navigateFromHomeToSettings() {
        hasContentDescription(value = getString(OSString.accessibility_home_settings_button_clickLabel))
            .waitUntilExactlyOneExists()
            .performClick()
        // At this point, we should be on SettingsRoute.
        hasTestTag(testTag = UiConstants.TestTag.Screen.Settings)
            .waitAndPrintRootToCacheDir(printRule = printRule, suffix = "_settings_route")
            .assertIsDisplayed()
    }

    private fun ComposeUiTest.navigateFromSettingsToExportAuth() {
        hasText(text = getString(OSString.settings_backupCard_exportLabel))
            .waitUntilExactlyOneExists()
            .performScrollTo()
            .performClick()
        // At this point, we should be on ExportAuthRoute.
        hasTestTag(testTag = UiConstants.TestTag.Screen.ExportAuthScreen)
            .waitAndPrintRootToCacheDir(printRule = printRule, suffix = "_export_auth_route")
            .assertIsDisplayed()
    }

    private fun ComposeUiTest.navigateFromExportAuthToExportData() {
        // Button should be disabled until password is filled.
        hasText(text = getString(OSString.common_next))
            .waitUntilExactlyOneExists()
            .assertIsDisplayed()
            .assertIsNotEnabled()

        // Fill with valid password.
        hasText(text = getString(OSString.backup_protectBackup_passwordCard_passwordInputLabel))
            .waitUntilExactlyOneExists()
            .assertIsDisplayed()
            .performTextReplacement(testPassword)

        // Avoid flakiness.
        Espresso.closeSoftKeyboard()
        keyboardHelper.waitForKeyboardVisibility(visible = false)

        // Check button state and move to next step.
        hasText(text = getString(OSString.common_next))
            .waitUntilExactlyOneExists()
            .performScrollTo() // avoid flakiness.
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        // At this point, we should be on ExportDataRoute.
        hasTestTag(testTag = UiConstants.TestTag.Screen.ExportDataScreen)
            .waitUntilExactlyOneExists()
            .assertIsDisplayed()
    }
}
