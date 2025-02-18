package studio.lunabee.onesafe.navigation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitAndPrintWholeScreenToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.ui.UiConstants

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class BreadCrumbNavigationTest : OSMainActivityTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Test
    fun bread_crumb_to_create_new_element_bottom_sheet_test() {
        invoke {
            hasTestTag(UiConstants.TestTag.BottomSheet.CreateItemBottomSheet)
                .waitUntilDoesNotExist()
            hasTestTag(UiConstants.TestTag.BreadCrumb.OSCreateItemButton)
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.BottomSheet.CreateItemBottomSheet)
                .waitAndPrintWholeScreenToCacheDir(printRule)
                .performTouchInput { swipeUp() }
                .assertIsDisplayed()
        }
    }

    // Non reg test for crash when clicking home breadcrumb item, from home
    @Test
    fun bread_crumb_home_to_home_test() {
        invoke {
            hasTestTag(UiConstants.TestTag.Screen.Home)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasContentDescription(getString(OSString.common_home))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.Home)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }
}
