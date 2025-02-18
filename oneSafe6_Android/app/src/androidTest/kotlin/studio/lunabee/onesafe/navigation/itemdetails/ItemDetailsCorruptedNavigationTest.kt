package studio.lunabee.onesafe.navigation.itemdetails

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.printToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.hasExcludeSearch
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.jvm.toByteArray
import studio.lunabee.onesafe.ui.UiConstants
import javax.inject.Inject

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class ItemDetailsCorruptedNavigationTest : OSMainActivityTest() {

    private val itemName: String = "test item"
    private val itemName2: String = "test item 2"

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home {
        val item = createItemUseCase(itemName, null, false, null, null).data!!
        createItemUseCase(itemName2, item.id, false, null, null).data!!
        mainDatabase.openHelper.writableDatabase.execSQL(
            "DELETE FROM SafeItemKey WHERE id = ?",
            arrayOf(item.id.toByteArray()),
        )
    }

    @Test
    fun corrupted_placeholder_pagination_safe_item_test() {
        invoke {
            onRoot()
                .printToCacheDir(printRule, "_home_screen")
            hasExcludeSearch(hasText(getString(OSString.common_corrupted)))
                .waitUntilExactlyOneExists()
                .assertExists()
        }
    }

    @Test
    fun corrupted_item_detail_test() {
        invoke {
            onRoot()
                .printToCacheDir(printRule, "_home_screen")

            hasExcludeSearch(hasText(getString(OSString.common_corrupted)))
                .waitUntilExactlyOneExists()
                .performClick()
            onRoot()
                .printToCacheDir(printRule, "_item_detail_corrupted")

            hasExcludeSearch(hasText(getString(OSString.safeItemDetail_corruptedCard_title)))
                .waitUntilExactlyOneExists(useUnmergedTree = true)
                .assertExists()
        }
    }

    @Test
    fun corrupted_item_detail_no_info_test() {
        invoke {
            onRoot()
                .printToCacheDir(printRule, "_home_screen")

            hasExcludeSearch(hasText(getString(OSString.common_corrupted)))
                .waitUntilExactlyOneExists()
                .performClick()

            hasExcludeSearch(hasText(getString(OSString.safeItemDetail_contentCard_tab_informations)))
                .waitUntilExactlyOneExists()
                .performClick()

            onRoot()
                .printToCacheDir(printRule, "_item_detail_information")

            hasExcludeSearch(hasText(getString(OSString.safeItemDetail_contentCard_informations_corrupted)))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun corrupted_item_detail_children_test() {
        invoke {
            onRoot()
                .printToCacheDir(printRule, "_home_screen")
            hasExcludeSearch(hasText(getString(OSString.common_corrupted)))
                .waitUntilExactlyOneExists()
                .performClick()
            onRoot()
                .printToCacheDir(printRule, "_item_detail_corrupted_item")
            hasExcludeSearch(hasText(itemName2))
                .waitUntilExactlyOneExists()
                .assertExists()
        }
    }

    @Test
    fun breadcrumb_item_corrupted_test() {
        invoke {
            onRoot()
                .printToCacheDir(printRule, "_home_screen")
            val textToFind = getString(OSString.common_corrupted)
            hasExcludeSearch(hasText(getString(OSString.common_corrupted)))
                .waitUntilExactlyOneExists()
                .performClick()
            onAllNodesWithContentDescription(textToFind)
                .filterToOne(hasAnyAncestor(hasTestTag(UiConstants.TestTag.BreadCrumb.BreadCrumbLayout)))
                .assertExists()
        }
    }
}
