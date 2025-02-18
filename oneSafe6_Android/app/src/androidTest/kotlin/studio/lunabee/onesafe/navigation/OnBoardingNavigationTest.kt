package studio.lunabee.onesafe.navigation

import android.content.Context
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.printToCacheDir
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.common.extensions.hasBiometric
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.ui.UiConstants

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class OnBoardingNavigationTest : OSMainActivityTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.SignedOut

    @Test
    fun full_new_user_on_boarding_navigation_biometric_test() {
        invoke {
            mockkStatic(Context::hasBiometric)
            every { this@OnBoardingNavigationTest.activity.hasBiometric() } returns true
            onBoardingFinishEnterCredential()
            hasTestTag(UiConstants.TestTag.Screen.BiometricSetup)
                .waitAndPrintRootToCacheDir(printRule, "_${UiConstants.TestTag.Screen.BiometricSetup}Screen")
                .assertIsDisplayed()
            hasText(getString(OSString.onBoarding_fastIdScreen_noButton))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.CongratulationOnBoarding)
                .waitAndPrintRootToCacheDir(printRule, "_${UiConstants.TestTag.Screen.CongratulationOnBoarding}Screen")
                .assertIsDisplayed()
            hasText(getString(OSString.onBoarding_congratulationScreen_goButton))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.Login)
                .waitAndPrintRootToCacheDir(printRule, "_${UiConstants.TestTag.Screen.Home}Screen")
                .assertIsDisplayed()
            hasText(getString(OSString.signInScreen_firstTime_welcome))
            hasText(getString(OSString.login_firstLogin_message))
        }
    }

    @Test
    fun full_new_user_on_boarding_navigation_no_biometric_test() {
        invoke {
            mockkStatic(Context::hasBiometric)
            every { this@OnBoardingNavigationTest.activity.hasBiometric() } returns false
            onBoardingFinishEnterCredential()
            hasTestTag(UiConstants.TestTag.Screen.CongratulationOnBoarding)
                .waitAndPrintRootToCacheDir(printRule, "_${UiConstants.TestTag.Screen.CongratulationOnBoarding}Screen")
                .assertIsDisplayed()
            hasText(getString(OSString.onBoarding_congratulationScreen_goButton))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.Login)
                .waitAndPrintRootToCacheDir(printRule, "_${UiConstants.TestTag.Screen.Home}Screen")
                .assertIsDisplayed()
            hasText(getString(OSString.signInScreen_firstTime_welcome))
        }
    }

    private fun ComposeUiTest.onBoardingFinishEnterCredential() {
        val password = "password"

        onRoot()
            .printToCacheDir(printRule, "_presentation")
        hasText(getString(OSString.appPresentation_security_action))
            .waitUntilExactlyOneExists()
            .performClick()
        onboardingPasswordCreation(password)
    }
}

context(OSMainActivityTest)
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.onboardingPasswordCreation(password: String) {
    hasTestTag(UiConstants.TestTag.Screen.PasswordCreation)
        .waitAndPrintRootToCacheDir(printRule, "_${UiConstants.TestTag.Screen.PasswordCreation}Screen")
        .assertIsDisplayed()
    hasTestTag(UiConstants.TestTag.Item.PasswordCreationTextField)
        .waitUntilExactlyOneExists()
        .performTextInput(password)
    hasText(getString(OSString.common_confirm))
        .waitUntilExactlyOneExists()
        .performScrollTo()
        .performClick()
    hasTestTag(UiConstants.TestTag.Screen.PasswordConfirmation)
        .waitAndPrintRootToCacheDir(printRule, "_${UiConstants.TestTag.Screen.PasswordConfirmation}Screen")
        .assertIsDisplayed()
    hasTestTag(UiConstants.TestTag.Item.PasswordConfirmationTextField)
        .waitUntilExactlyOneExists()
        .performTextInput(password)
    hasText(getString(OSString.common_confirm))
        .waitUntilExactlyOneExists()
        .performScrollTo()
        .performClick()
}
