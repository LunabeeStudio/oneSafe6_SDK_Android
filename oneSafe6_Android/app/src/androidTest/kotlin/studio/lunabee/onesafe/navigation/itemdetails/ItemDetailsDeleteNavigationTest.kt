package studio.lunabee.onesafe.navigation.itemdetails

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitAndPrintWholeScreenToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.ui.UiConstants

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class ItemDetailsDeleteNavigationTest : ItemDetailsNavigationTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Test
    fun item_details_nav_to_delete_test() {
        invoke {
            navToItemDetails()
            val deleteText = getString(OSString.safeItemDetail_actionCard_delete)
            val deleteDialogButtonText = getString(OSString.safeItemDetail_delete_alert_button_confirm)
            val deleteActionMatcher = hasText(deleteText)
            hasTestTag(UiConstants.TestTag.ScrollableContent.ItemDetailLazyColumn)
                .waitUntilExactlyOneExists()
                .performScrollToNode(deleteActionMatcher)

            deleteActionMatcher
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(deleteDialogButtonText)
                .waitAndPrintWholeScreenToCacheDir(printRule, suffix = "_dialog")
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.Home)
                .waitAndPrintRootToCacheDir(printRule, suffix = "_home_screen")
            hasText(getString(OSString.safeItemDetail_moveIntoBin_success_message))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasTestTag(UiConstants.TestTag.Screen.Home)
                .waitAndPrintRootToCacheDir(printRule)
                .assertIsDisplayed()
        }
    }
}
