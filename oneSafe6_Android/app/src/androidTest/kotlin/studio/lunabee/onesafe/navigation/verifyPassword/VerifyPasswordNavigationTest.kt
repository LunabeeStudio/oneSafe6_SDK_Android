package studio.lunabee.onesafe.navigation.verifyPassword

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.test.performTextInput
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
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.repository.SecuritySettingsRepository
import studio.lunabee.onesafe.domain.usecase.authentication.IsCurrentSafeBiometricEnabledUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.ui.UiConstants
import javax.inject.Inject
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class VerifyPasswordNavigationTest : OSMainActivityTest() {
    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home {
        createItemUseCase("", null, false, null, null) // Create an item to not have the empty home.
    }

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var securitySettingsRepository: SecuritySettingsRepository

    @BindValue
    val isCurrentSafeBiometricEnabledUseCase: IsCurrentSafeBiometricEnabledUseCase = mockk {
        every { flow() } returns flowOf(true)
        coEvery { this@mockk.invoke() } returns true
    }

    @Test
    fun verify_wrong_password_from_home_test() {
        invoke {
            onNodeWithTag(UiConstants.TestTag.Item.HomeItemGrid).performScrollToKey(OSString.common_other + "OtherCard".hashCode())
            hasText(getString(OSString.home_verifyPassword_title)).waitUntilExactlyOneExists().performClick()
            hasTestTag(UiConstants.TestTag.BottomSheet.VerifyPasswordBottomSheet).waitUntilExactlyOneExists()
                .performTouchInput { swipeUp() }
            onNodeWithText(getString(OSString.verifyPassword_bottomSheet_verifyButton)).performClick()
            hasTestTag(UiConstants.TestTag.Screen.PasswordConfirmation).waitUntilExactlyOneExists()
            onNodeWithTag(UiConstants.TestTag.Item.PasswordConfirmationTextField).performClick().performTextInput("aze") // Wrong password
            onNodeWithText(getString(OSString.common_confirm)).performScrollTo().performClick()
            hasTestTag(UiConstants.TestTag.Screen.WrongPasswordScreen).waitUntilExactlyOneExists()
            onNodeWithText(getString(OSString.wrongPassword_card_retryButton)).performClick()
            hasTestTag(UiConstants.TestTag.Screen.PasswordConfirmation).waitUntilExactlyOneExists()
            onNodeWithTag(UiConstants.TestTag.Item.PasswordConfirmationTextField).performClick().performTextInput("aze") // Wrong password
            onNodeWithText(getString(OSString.common_confirm)).performScrollTo().performClick()
            hasTestTag(UiConstants.TestTag.Screen.WrongPasswordScreen).waitUntilExactlyOneExists()
        }
    }

    @Test
    fun verify_right_password_from_home_test() {
        invoke {
            onNodeWithTag(UiConstants.TestTag.Item.HomeItemGrid).performScrollToKey(OSString.common_other + "OtherCard".hashCode())
            onNodeWithText(getString(OSString.home_verifyPassword_title)).performClick()
            hasTestTag(UiConstants.TestTag.BottomSheet.VerifyPasswordBottomSheet).waitUntilExactlyOneExists()
                .performTouchInput { swipeUp() }
            onNodeWithText(getString(OSString.verifyPassword_bottomSheet_verifyButton)).performClick()
            hasTestTag(UiConstants.TestTag.Screen.PasswordConfirmation).waitUntilExactlyOneExists()
            onNodeWithTag(UiConstants.TestTag.Item.PasswordConfirmationTextField).performClick().performTextInput("a") // right password
            onNodeWithText(getString(OSString.common_confirm)).performScrollTo().performClick()
            hasTestTag(UiConstants.TestTag.Screen.RightPasswordScreen).waitUntilExactlyOneExists()

            // test that we save the verification time
            assertEquals(
                expected = testClock.millis().toDouble(),
                actual = runBlocking { securitySettingsRepository.lastPasswordVerificationInstant(firstSafeId).toEpochMilli().toDouble() },
                absoluteTolerance = 1000.0,
            )
            onNodeWithText(getString(OSString.rightPassword_card_finishButton)).performClick()
            hasTestTag(UiConstants.TestTag.Screen.Home).waitUntilExactlyOneExists()
        }
    }
}
