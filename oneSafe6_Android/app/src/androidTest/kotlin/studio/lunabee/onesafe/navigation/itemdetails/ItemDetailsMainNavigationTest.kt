package studio.lunabee.onesafe.navigation.itemdetails

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.compose.androidtest.extension.printToCacheDir
import studio.lunabee.onesafe.ui.UiConstants

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class ItemDetailsMainNavigationTest : ItemDetailsNavigationTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Test
    fun item_details_top_back_home_test() {
        invoke {
            navToItemDetails()
            val backContentDescription = getString(OSString.common_accessibility_back)
            onAllNodesWithContentDescription(backContentDescription)
                .filterToOne(!hasAnyAncestor(hasTestTag(UiConstants.TestTag.Screen.SearchScreen)))
                .performClick()
            onRoot()
                .printToCacheDir(printRule, "_home_screen")
            onNodeWithTag(UiConstants.TestTag.Screen.Home)
                .assertIsDisplayed()
        }
    }

    @Test
    fun item_details_nav_back_home_test() {
        invoke {
            navToItemDetails()
            Espresso.pressBack()
            onRoot()
                .printToCacheDir(printRule, "_home_screen")
            onNodeWithTag(UiConstants.TestTag.Screen.Home)
                .assertIsDisplayed()
        }
    }
}
