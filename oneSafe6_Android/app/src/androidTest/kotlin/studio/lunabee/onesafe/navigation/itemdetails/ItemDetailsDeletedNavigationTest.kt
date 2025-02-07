package studio.lunabee.onesafe.navigation.itemdetails

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToKey
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.MoveToBinItemUseCase
import studio.lunabee.onesafe.hasExcludeSearch
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.ui.UiConstants
import javax.inject.Inject
import studio.lunabee.onesafe.test.test

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class ItemDetailsDeletedNavigationTest : OSMainActivityTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    lateinit var item: SafeItem
    val itemName: String = "test item"

    override val initialTestState: InitialTestState = InitialTestState.Home {
        item = createItemUseCase.test(itemName)
        moveToBinItemUseCase(item)
    }

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject
    lateinit var moveToBinItemUseCase: MoveToBinItemUseCase

    private fun ComposeUiTest.moveToDetailsFromBin() {
        hasText(getString(OSString.common_bin))
            .waitUntilExactlyOneExists()
            .performClick()
        hasExcludeSearch(hasText(itemName))
            .waitUntilExactlyOneExists()
            .performClick()
        hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(testUUIDs[0]))
            .waitAndPrintRootToCacheDir(printRule, "_itemDetails_screen")
    }

    @Test
    fun item_details_deleted_nav_back_to_bin_on_remove() {
        invoke {
            moveToDetailsFromBin()
            val deleteText = getString(OSString.safeItemDetail_actionCard_remove)
            val deleteDialogButtonText = getString(OSString.safeItemDetail_remove_alert_button_confirm)
            hasTestTag(testTag = UiConstants.TestTag.ScrollableContent.ItemDetailLazyColumn)
                .waitUntilExactlyOneExists()
                .performScrollToKey(key = UiConstants.TestTag.Item.ItemDetailsRegularActionCard)
            hasText(deleteText)
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(deleteDialogButtonText)
                .waitUntilExactlyOneExists()
                .performClick()
            isRoot()
                .waitAndPrintRootToCacheDir(printRule, suffix = "_bin_screen")
            hasTestTag(UiConstants.TestTag.Screen.Bin)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun item_details_deleted_nav_back_to_bin_on_restore() {
        invoke {
            moveToDetailsFromBin()
            val restoreText = getString(OSString.safeItemDetail_actionCard_restore)
            hasText(restoreText)
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .performClick()
            isRoot()
                .waitAndPrintRootToCacheDir(printRule, suffix = "_bin_screen")
            hasTestTag(UiConstants.TestTag.Screen.Bin)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }
}
