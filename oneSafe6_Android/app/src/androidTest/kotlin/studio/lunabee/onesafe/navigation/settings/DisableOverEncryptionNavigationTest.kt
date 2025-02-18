package studio.lunabee.onesafe.navigation.settings

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
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
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.di.InMemoryDatabaseEncryptionManager
import studio.lunabee.onesafe.FinishSetupDatabaseActivity
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.model.crypto.DatabaseKey
import studio.lunabee.onesafe.domain.repository.DatabaseEncryptionManager
import studio.lunabee.onesafe.domain.repository.DatabaseKeyRepository
import studio.lunabee.onesafe.domain.usecase.authentication.IsBiometricEnabledState
import studio.lunabee.onesafe.domain.usecase.authentication.HasBiometricSafeUseCase
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.usecase.GetAutoBackupModeUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.ui.UiConstants
import javax.inject.Inject

/**
 * Covers the whole OverEncryption disable flow. See [studio.lunabee.onesafe.feature.settings.overencryption]
 */
@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class DisableOverEncryptionNavigationTest : OSMainActivityTest() {

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
    lateinit var databaseKeyRepository: DatabaseKeyRepository

    @Inject
    lateinit var sqlCipherDBManager: DatabaseEncryptionManager

    @Before
    fun setUp() {
        // Avoid process kill
        mockkObject(FinishSetupDatabaseActivity.Companion)
        every { FinishSetupDatabaseActivity.launch(any()) } returns Unit
        runTest {
            databaseKeyRepository.setKey(DatabaseKey(OSTestConfig.random.nextBytes(DatabaseKey.DatabaseKeyByteSize)), true)
        }
    }

    @Test
    fun disable_full_flow_ok_test(): Unit = invoke {
        fullFlow()
        hasText(getString(OSString.overEncryptionEnabled_loadingCard_title))
            .waitUntilExactlyOneExists()
    }

    @Test
    fun disable_skipBackup_flow_ok_test(): Unit = invoke {
        val autoBackupMode = AutoBackupMode.LocalOnly
        every { getAutoBackupModeUseCase.flow() } returns flowOf(autoBackupMode)
        coEvery { getAutoBackupModeUseCase(firstSafeId) } returns autoBackupMode

        fullFlow(skipBackup = true)
        hasText(getString(OSString.overEncryptionEnabled_loadingCard_title))
            .waitUntilExactlyOneExists()
    }

    @Test
    fun disable_full_flow_error_test(): Unit = invoke {
        val inMemoryDatabaseEncryptionManager = sqlCipherDBManager as InMemoryDatabaseEncryptionManager
        inMemoryDatabaseEncryptionManager.throwOnMigrate = true
        fullFlow()
        hasText(getString(OSString.overEncryptionEnabled_errorCard_message, ""), substring = true)
            .waitUntilExactlyOneExists()
    }

    context(ComposeUiTest)
    private fun fullFlow(skipBackup: Boolean = false) {
        SettingsNavigationTest.navToSettings()
        hasText(getString(OSString.settings_tab_app))
            .waitUntilExactlyOneExists()
            .performScrollTo()
            .performClick()
        hasText(getString(OSString.settings_security_section_overEncryption_status_enabled))
            .waitUntilExactlyOneExists()
            .performScrollTo()
            .performClick()
        hasTestTag(UiConstants.TestTag.Screen.OverEncryptionEnabledScreen)
            .waitUntilExactlyOneExists()
        if (skipBackup) {
            hasText(getString(OSString.overEncryptionBackup_toggleCard_message_disable))
                .waitUntilDoesNotExist()
                .assertDoesNotExist()
        } else {
            hasText(getString(OSString.overEncryptionBackup_toggleCard_message_disable))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
        hasText(getString(OSString.common_disable))
            .waitUntilExactlyOneExists()
            .performScrollTo()
            .performClick()
        hasText(getString(OSString.common_yesDisable))
            .waitUntilExactlyOneExists()
            .performClick()
    }
}
