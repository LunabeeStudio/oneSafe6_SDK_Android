package studio.lunabee.onesafe.navigation

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.ui.UiConstants

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class LoginNavigationTest : OSMainActivityTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.SignedUp()

    @Test
    fun signin_to_home_test() {
        invoke {
            navigateAndAssert()
        }
    }

    @Test
    fun signin_to_home_wrong_credentials_test() {
        invoke {
            hasTestTag(UiConstants.TestTag.Screen.Login)
                .waitAndPrintRootToCacheDir(printRule)
                .assertIsDisplayed()
            hasTestTag(UiConstants.TestTag.Item.LoginPasswordTextField)
                .waitUntilExactlyOneExists()
                .performTextInput("wrong_password")
            hasTestTag(UiConstants.TestTag.Item.LoginButtonIcon)
                .waitAndPrintRootToCacheDir(useUnmergedTree = true, printRule = printRule)
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.Login)
                .waitAndPrintRootToCacheDir(printRule, "_after_wrong_password")
                .assertIsDisplayed()
        }
    }

    private fun ComposeUiTest.navigateAndAssert() {
        onNodeWithTag(UiConstants.TestTag.Item.LoginPasswordTextField)
            .performTextInput(testPassword)
        hasTestTag(UiConstants.TestTag.Item.LoginButtonIcon)
            .waitAndPrintRootToCacheDir(printRule, "_login")
            .performScrollTo()
            .performClick()
        hasTestTag(UiConstants.TestTag.Screen.Home)
            .waitAndPrintRootToCacheDir(printRule, "_home_screen")
            .assertIsDisplayed()
    }
}
