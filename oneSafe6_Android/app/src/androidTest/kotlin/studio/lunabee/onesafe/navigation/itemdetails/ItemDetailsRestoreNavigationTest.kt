package studio.lunabee.onesafe.navigation.itemdetails

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilAtLeastOneExists
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.commonui.OSPlurals
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.usecase.item.MoveToBinItemUseCase
import studio.lunabee.onesafe.hasExcludeBreadcrumb
import studio.lunabee.onesafe.hasExcludeSearch
import studio.lunabee.onesafe.test.test
import studio.lunabee.onesafe.ui.UiConstants
import javax.inject.Inject

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class ItemDetailsRestoreNavigationTest : ItemDetailsNavigationTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject lateinit var moveToBinItemUseCase: MoveToBinItemUseCase

    private val childItemName = "child"

    private fun createChildAndMoveToBin(): SafeItem {
        return runBlocking {
            val safeItem = createItemUseCase.test(name = childItemName, parentId = item.id)
            moveToBinItemUseCase(item)
            safeItem
        }
    }

    @Test
    fun item_details_nav_back_on_restore_card() {
        invoke {
            navToItemDetails()
            createChildAndMoveToBin()
            // Open elements tab
            hasText(getQuantityString(OSPlurals.safeItemDetail_contentCard_tab_elements, 1, 1))
                .waitUntilExactlyOneExists()
                .performClick()
            // nav to child
            hasExcludeSearch(hasText(childItemName))
                .waitUntilExactlyOneExists()
                .performClick()
            // restore child
            val restoreCardText = getQuantityString(OSPlurals.safeItemDetail_deletedCard_message, 123, 123)
                .substringBefore("123")
            val matcher = hasAnyAncestor(hasAnySibling(hasText(restoreCardText, substring = true)))
            hasText(getString(OSString.safeItemDetail_actionCard_restore))
                .waitUntilAtLeastOneExists()
                .filterToOne(matcher)
                .performClick()
            // assert back on parent item
            isRoot()
                .waitAndPrintRootToCacheDir(printRule, suffix = "_item_screen")
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(item.id))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun item_details_nav_back_on_restore_actions() {
        invoke {
            navToItemDetails()
            createChildAndMoveToBin()
            hasText(getString(OSString.safeItemDetail_actionCard_restore))
                .waitAndPrintRootToCacheDir(printRule)
                .performScrollTo()
                .performClick()

            hasTestTag(UiConstants.TestTag.Screen.Home)
                .waitAndPrintRootToCacheDir(printRule)
                .assertIsDisplayed()
        }
    }

    @Test
    fun item_details_nav_to_restored_test() {
        invoke {
            navToItemDetails()
            createChildAndMoveToBin()
            // Action restore
            hasText(getString(OSString.safeItemDetail_actionCard_restore))
                .waitUntilAtLeastOneExists()
                .onFirst()
                .performScrollTo()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.Home)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            // Nav to restored
            isRoot()
                .waitAndPrintRootToCacheDir(printRule, suffix = "_snackbar_feedback")
            hasText(getString(OSString.common_see))
                .waitUntilExactlyOneExists()
                .performClick()
            isRoot()
                .waitAndPrintRootToCacheDir(printRule, suffix = "_after_nav_to_restored")
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(item.id))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun item_details_nav_to_restored_dismiss_snackbar_test() {
        invoke {
            navToItemDetails()
            runBlocking {
                moveToBinItemUseCase(item)
            }
            // Action restore
            hasText(getString(OSString.safeItemDetail_actionCard_restore))
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .performClick()
            isRoot()
                .waitAndPrintRootToCacheDir(printRule, suffix = "_snackbar_feedback")
            hasTestTag(UiConstants.TestTag.Screen.Home)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasText(getString(OSString.common_see))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            // Nav to restored item manually
            hasExcludeSearch(hasExcludeBreadcrumb(hasText(itemName)))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(item.id))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            isRoot()
                .waitAndPrintRootToCacheDir(printRule, suffix = "_snackbar_feedback_dismiss")
            hasText(getString(OSString.common_see))
                .waitUntilDoesNotExist()
        }
    }

    @Test
    fun item_details_nav_to_restored_more_nav_test() {
        invoke {
            val otherItemName = "other"
            val otherItem = runBlocking { createItemUseCase.test(name = otherItemName) }
            navToItemDetails()
            createChildAndMoveToBin()
            // Action restore
            hasText(getString(OSString.safeItemDetail_actionCard_restore))
                .waitUntilAtLeastOneExists()
                .onFirst()
                .performScrollTo()
                .performClick()
            // Nav to other item
            hasExcludeSearch(hasText(otherItemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(otherItem.id))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            // Nav to restored using snackbar
            hasText(getString(OSString.common_see))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(item.id))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            // Back to other
            Espresso.pressBack()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(otherItem.id))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            // Back to home
            Espresso.pressBack()
            hasTestTag(UiConstants.TestTag.Screen.Home)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }
}
