package studio.lunabee.onesafe.navigation

import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitAndPrintWholeScreenToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.MainActivity
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.usecase.item.MoveToBinItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.hasExcludeSearch
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.ui.UiConstants
import javax.inject.Inject

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class BinNavigationTest : OSMainActivityTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home {
        val item = createItemUseCase(itemName, null, false, null, null).data!!
        moveToBinItemUseCase(item)
    }

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var moveToBinItemUseCase: MoveToBinItemUseCase

    private val itemName = "test item"

    private fun AndroidComposeUiTest<MainActivity>.navToBin() {
        onNodeWithTag(UiConstants.TestTag.Item.HomeItemGrid)
            .performScrollToNode(hasText(getString(OSString.common_bin)))

        hasText(getString(OSString.common_bin))
            .waitUntilExactlyOneExists()
            .performClick()
    }

    @Test
    fun bin_nav_item_details() {
        invoke {
            navToBin()
            hasExcludeSearch(hasText(itemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(testUUIDs[0]))
                .waitAndPrintRootToCacheDir(printRule, "itemDetails_screen")
                .assertIsDisplayed()
        }
    }

    @Test
    fun bin_nav_back_home_on_delete_all_test() {
        invoke {
            navToBin()
            hasContentDescription(getString(OSString.bin_topBar_menu_accessibility_description))
                .waitUntilExactlyOneExists()
                .performClick()
            isPopup()
                .waitAndPrintWholeScreenToCacheDir(printRule, "_menu")
                .assertIsDisplayed()
            hasText(getString(OSString.bin_topBar_menu_removeAll))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.common_delete))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.bin_deleteAll_success_message))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasTestTag(UiConstants.TestTag.Screen.Home)
                .waitAndPrintRootToCacheDir(printRule, "_home_screen")
                .assertIsDisplayed()
        }
    }

    @Test
    fun bin_nav_back_home_on_restore_all_test() {
        invoke {
            navToBin()
            hasContentDescription(getString(OSString.bin_topBar_menu_accessibility_description))
                .waitUntilExactlyOneExists()
                .performClick()
            isPopup()
                .waitAndPrintWholeScreenToCacheDir(printRule, "_menu")
                .assertIsDisplayed()
            hasText(getString(OSString.bin_topBar_menu_restoreAll))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.common_restore))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.bin_restoreAll_success_message))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasTestTag(UiConstants.TestTag.Screen.Home)
                .waitAndPrintRootToCacheDir(printRule, "_home_screen")
                .assertIsDisplayed()
        }
    }
}
