package studio.lunabee.onesafe.navigation.settings

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.domain.usecase.authentication.IsCurrentSafeBiometricEnabledUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetSecuritySettingUseCase
import studio.lunabee.onesafe.navigation.settings.SettingsNavigationTest.Companion.navToSettings
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.ui.UiConstants
import javax.inject.Inject
import kotlin.test.assertEquals

@HiltAndroidTest
class SettingsVerifyPasswordNavigationTest : OSMainActivityTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var getSecuritySettingUseCase: GetSecuritySettingUseCase

    @BindValue
    val isCurrentSafeBiometricEnabledUseCase: IsCurrentSafeBiometricEnabledUseCase = mockk {
        every { flow() } returns flowOf(true)
        coEvery { this@mockk.invoke() } returns true
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun test_change_password_verification_interval() {
        invoke {
            navToSettings()

            onNodeWithText(getString(OSString.settings_section_security_option_label))
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.Settings)
                .waitAndPrintRootToCacheDir(printRule)

            // Never
            onNodeWithText(getString(OSString.settings_security_section_verifyPassword_row), useUnmergedTree = true)
                .performScrollTo()
                .performClick()
            hasTestTag(UiConstants.TestTag.BottomSheet.VerifyPasswordBottomSheetInterval).waitUntilExactlyOneExists()
                .performTouchInput { swipeUp() }
            onNodeWithText(getString(OSString.verifyPassword_interval_never)).performClick()
            assertEquals(
                expected = VerifyPasswordInterval.NEVER,
                actual = runBlocking { getSecuritySettingUseCase.verifyPasswordInterval() }.data,
            )

            // Every Month
            onNodeWithText(getString(OSString.settings_security_section_verifyPassword_row), useUnmergedTree = true).performClick()
            hasTestTag(UiConstants.TestTag.BottomSheet.VerifyPasswordBottomSheetInterval).waitUntilExactlyOneExists()
                .performTouchInput { swipeUp() }
            onNodeWithText(getString(OSString.verifyPassword_interval_everyMonth)).performClick()
            assertEquals(
                expected = VerifyPasswordInterval.EVERY_MONTH,
                actual = runBlocking { getSecuritySettingUseCase.verifyPasswordInterval() }.data,
            )

            // Every Two Months
            onNodeWithText(getString(OSString.settings_security_section_verifyPassword_row), useUnmergedTree = true).performClick()
            hasTestTag(UiConstants.TestTag.BottomSheet.VerifyPasswordBottomSheetInterval).waitUntilExactlyOneExists()
                .performTouchInput { swipeUp() }
            onNodeWithText(getString(OSString.verifyPassword_interval_everyTwoMonths)).performClick()
            assertEquals(
                expected = VerifyPasswordInterval.EVERY_TWO_MONTHS,
                actual = runBlocking { getSecuritySettingUseCase.verifyPasswordInterval() }.data,
            )

            // Every Six Months
            onNodeWithText(getString(OSString.settings_security_section_verifyPassword_row), useUnmergedTree = true).performClick()
            hasTestTag(UiConstants.TestTag.BottomSheet.VerifyPasswordBottomSheetInterval).waitUntilExactlyOneExists()
                .performTouchInput { swipeUp() }
            onNodeWithText(getString(OSString.verifyPassword_interval_everySixMonths)).performClick()
            assertEquals(
                expected = VerifyPasswordInterval.EVERY_SIX_MONTHS,
                actual = runBlocking { getSecuritySettingUseCase.verifyPasswordInterval() }.data,
            )
        }
    }
}
