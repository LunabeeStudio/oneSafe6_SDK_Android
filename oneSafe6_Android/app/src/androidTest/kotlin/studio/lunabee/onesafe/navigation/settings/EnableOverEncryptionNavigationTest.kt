package studio.lunabee.onesafe.navigation.settings

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.di.InMemoryDatabaseEncryptionManager
import studio.lunabee.onesafe.FinishSetupDatabaseActivity
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.repository.DatabaseEncryptionManager
import studio.lunabee.onesafe.domain.usecase.authentication.IsBiometricEnabledState
import studio.lunabee.onesafe.domain.usecase.authentication.HasBiometricSafeUseCase
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.usecase.GetAutoBackupModeUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.ui.UiConstants
import javax.inject.Inject

/**
 * Covers the whole OverEncryption enable flow. See [studio.lunabee.onesafe.feature.settings.overencryption]
 */
@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class EnableOverEncryptionNavigationTest : OSMainActivityTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.Home()

    @BindValue
    val hasBiometricSafeUseCase: HasBiometricSafeUseCase = mockk {
        coEvery { this@mockk.invoke() } returns flowOf(IsBiometricEnabledState.Disabled)
    }

    @BindValue
    val getAutoBackupModeUseCase: GetAutoBackupModeUseCase = mockk {
        val autoBackupMode = AutoBackupMode.Disabled
        every { flow() } returns flowOf(autoBackupMode)
        coEvery { this@mockk(any()) } returns autoBackupMode
    }

    @Inject
    lateinit var sqlCipherDBManager: DatabaseEncryptionManager

    @Before
    fun setUp() {
        // Avoid process kill
        mockkObject(FinishSetupDatabaseActivity.Companion)
        every { FinishSetupDatabaseActivity.launch(any()) } returns Unit
    }

    @Test
    fun enable_full_flow_ok_test(): Unit = invoke {
        fullFlow()
        hasText(getString(OSString.overEncryptionKey_loadingCard_title))
            .waitUntilExactlyOneExists()
    }

    @Test
    fun enable_skipBackup_flow_ok_test(): Unit = invoke {
        val autoBackupMode = AutoBackupMode.LocalOnly
        every { getAutoBackupModeUseCase.flow() } returns flowOf(autoBackupMode)
        coEvery { getAutoBackupModeUseCase(firstSafeId) } returns autoBackupMode

        fullFlow(skipBackup = true)
        hasText(getString(OSString.overEncryptionKey_loadingCard_title))
            .waitUntilExactlyOneExists()
    }

    @Test
    fun enable_full_flow_error_test(): Unit = invoke {
        val inMemoryDatabaseEncryptionManager = sqlCipherDBManager as InMemoryDatabaseEncryptionManager
        inMemoryDatabaseEncryptionManager.throwOnMigrate = true
        fullFlow()
        hasText(getString(OSString.overEncryptionKey_errorCard_message, ""), substring = true)
            .waitUntilExactlyOneExists()
    }

    context(ComposeUiTest)
    private fun fullFlow(skipBackup: Boolean = false) {
        SettingsNavigationTest.navToSettings()
        hasText(getString(OSString.settings_tab_app))
            .waitUntilExactlyOneExists()
            .performScrollTo()
            .performClick()
        hasText(getString(OSString.settings_security_section_overEncryption_status_disabled))
            .waitUntilExactlyOneExists()
            .performScrollTo()
            .performClick()
        hasTestTag(UiConstants.TestTag.Screen.OverEncryptionExplanationScreen)
            .waitUntilExactlyOneExists()
        hasText(getString(OSString.common_start))
            .waitUntilExactlyOneExists()
            .performScrollTo()
            .performClick()
        if (skipBackup) {
            hasTestTag(UiConstants.TestTag.Screen.OverEncryptionBackupScreen)
                .waitUntilDoesNotExist()
                .assertDoesNotExist()
        } else {
            hasTestTag(UiConstants.TestTag.Screen.OverEncryptionBackupScreen)
                .waitUntilExactlyOneExists()
            hasText(getString(OSString.overEncryptionBackup_toggleCard_message_enable))
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .assertIsDisplayed()
                .assertHasClickAction()
            hasText(getString(OSString.common_next))
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .performClick()
        }
        hasTestTag(UiConstants.TestTag.Screen.OverEncryptionKeyScreen)
            .waitUntilExactlyOneExists()
        hasText(getString(OSString.overEncryptionKey_enableButton))
            .waitUntilExactlyOneExists()
            .performScrollTo()
            .performClick()
        hasText(getString(OSString.overEncryptionKey_confirmationDialog_message))
            .waitUntilExactlyOneExists()
        hasText(getString(OSString.common_noCancel))
            .waitUntilExactlyOneExists()
            .performClick()
        hasText(getString(OSString.overEncryptionKey_confirmationDialog_message))
            .waitUntilDoesNotExist()
        hasText(getString(OSString.overEncryptionKey_enableButton))
            .waitUntilExactlyOneExists()
            .performScrollTo()
            .performClick()
        hasText(getString(OSString.overEncryptionKey_confirmationDialog_message))
            .waitUntilExactlyOneExists()
        hasText(getString(OSString.common_yesLetsGo))
            .waitUntilExactlyOneExists()
            .performClick()
    }
}
