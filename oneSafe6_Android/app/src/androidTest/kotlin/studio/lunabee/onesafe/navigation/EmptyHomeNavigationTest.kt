package studio.lunabee.onesafe.navigation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.ui.UiConstants

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class EmptyHomeNavigationTest : OSMainActivityTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Test
    fun home_to_import_test() {
        invoke {
            hasTestTag(UiConstants.TestTag.Item.HomeEmptyCarousel)
                .waitUntilExactlyOneExists()
                .performTouchInput {
                    swipeLeft() // oS5 migration
                    swipeLeft() // discover
                }
            hasText(getString(OSString.home_section_welcome_emptyTab_import_button))
                .waitUntilExactlyOneExists(useUnmergedTree = true)
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.ImportFileScreen)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }
}
