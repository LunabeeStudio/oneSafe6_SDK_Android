package studio.lunabee.onesafe.navigation.bubbles

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitAndPrintWholeScreenToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.ui.UiConstants

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class BubblesOnBoardingNavigationTest : OSMainActivityTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Test
    fun navigate_to_bubbles_on_boarding_only_one_time() {
        invoke {
            hasContentDescription(getString(OSString.accessibility_home_contacts_button_clickLabel))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.OnBoardingBubblesScreen)
                .waitAndPrintWholeScreenToCacheDir(printRule, "_onBoarding")
            hasText(getString(OSString.bubbles_welcomeScreen_startButton))
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.BubblesHomeScreen)
                .waitAndPrintWholeScreenToCacheDir(printRule, "_bubbles")
            Espresso.pressBack()
            hasContentDescription(getString(OSString.accessibility_home_contacts_button_clickLabel))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.BubblesHomeScreen)
                .waitAndPrintWholeScreenToCacheDir(printRule, "_bubbles")
        }
    }
}
