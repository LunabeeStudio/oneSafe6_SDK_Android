package studio.lunabee.onesafe.navigation.settings

import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.MainActivity
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.model.safe.BiometricCryptoMaterial
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.StaticIdProvider
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.ui.UiConstants

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class ChangePasswordNavigationTest : OSMainActivityTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Test
    fun change_password_without_biometric_test(): Unit = invoke {
        SettingsNavigationTest.navToSettings()

        runChangePasswordFlow()

        hasTestTag(UiConstants.TestTag.Screen.Settings)
            .waitAndPrintRootToCacheDir(printRule)
    }

    @Test
    fun change_password_already_used_test(): Unit = invoke {
        runBlocking {
            StaticIdProvider.id = testUUIDs[1]
            signup("b".toCharArray(), SafeId(testUUIDs[1]))
        }
        SettingsNavigationTest.navToSettings()

        val changePasswordLabel = getString(OSString.settings_section_changePassword_label)
        val passwordField = getString(OSString.onBoarding_passwordCreationScreen_passwordLabel)
        val confirm = getString(OSString.common_confirm)

        hasText(changePasswordLabel)
            .waitUntilExactlyOneExists()
            .performScrollTo()
            .performClick()

        hasTestTag(UiConstants.TestTag.BottomSheet.EnterPasswordBottomSheet)
            .waitAndPrintRootToCacheDir(printRule)
        onNodeWithText(passwordField)
            .performTextInput(testPassword)
        onNodeWithText(confirm)
            .performClick()

        onNodeWithTag(UiConstants.TestTag.Item.PasswordCreationTextField)
            .performTextInput("b")
        onNodeWithText(confirm)
            .performScrollTo()
            .performClick()

        hasText(getString(OSString.changePassword_error_samePasswordAsOtherSafe)).waitUntilExactlyOneExists().assertIsDisplayed()
    }

    @Test
    fun change_password_with_biometric_noButton_test() {
        doChangePasswordWithBiometricTest {
            val biometricNo = getString(OSString.onBoarding_fastIdScreen_noButton)

            onNodeWithText(biometricNo)
                .performScrollTo()
                .performClick()
        }
    }

    @Test
    fun change_password_with_biometric_backPress_test() {
        doChangePasswordWithBiometricTest {
            Espresso.pressBack()
        }
    }

    private fun AndroidComposeUiTest<MainActivity>.runChangePasswordFlow(newPassword: String = "z") {
        val changePasswordLabel = getString(OSString.settings_section_changePassword_label)
        val passwordField = getString(OSString.onBoarding_passwordCreationScreen_passwordLabel)
        val confirm = getString(OSString.common_confirm)

        hasText(changePasswordLabel)
            .waitUntilExactlyOneExists()
            .performScrollTo()
            .performClick()

        hasTestTag(UiConstants.TestTag.BottomSheet.EnterPasswordBottomSheet)
            .waitAndPrintRootToCacheDir(printRule)
        onNodeWithText(passwordField)
            .performTextInput(testPassword)
        onNodeWithText(confirm)
            .performClick()

        hasTestTag(UiConstants.TestTag.Screen.PasswordCreation)
            .waitAndPrintRootToCacheDir(printRule)
        onNodeWithText(passwordField)
            .performTextInput(testPassword) // the old user's password
        onNodeWithText(confirm)
            .performScrollTo()
            .performClick()

        hasText(getString(OSString.changePassword_error_samePasswordAsOtherSafe)).waitUntilExactlyOneExists().assertIsDisplayed()

        onNodeWithTag(UiConstants.TestTag.Item.PasswordCreationTextField)
            .performTextClearance()
        onNodeWithTag(UiConstants.TestTag.Item.PasswordCreationTextField)
            .performTextInput(newPassword)
        onNodeWithText(confirm)
            .performScrollTo()
            .performClick()

        hasTestTag(UiConstants.TestTag.Screen.PasswordConfirmation)
            .waitUntilExactlyOneExists()
        onNodeWithText(passwordField)
            .performTextInput(newPassword)
        onNodeWithText(confirm)
            .performScrollTo()
            .performClick()
    }

    // Biometry must be disabled on the device to make the test works (to avoid the biometric prompt)
    private fun doChangePasswordWithBiometricTest(
        exit: AndroidComposeUiTest<MainActivity>.() -> Unit,
    ): Unit = invoke {
        SettingsNavigationTest.navToSettings()
        runBlocking { safeRepository.setBiometricMaterial(firstSafeId, BiometricCryptoMaterial(OSTestConfig.random.nextBytes(64))) }
        runChangePasswordFlow()
        hasTestTag(UiConstants.TestTag.Screen.BiometricSetup)
            .waitUntilExactlyOneExists()
        exit()

        hasTestTag(UiConstants.TestTag.Screen.Settings)
            .waitAndPrintRootToCacheDir(printRule)
    }
}
