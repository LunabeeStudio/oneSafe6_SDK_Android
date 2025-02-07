package studio.lunabee.onesafe.navigation.itemdetails

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.OSPlurals
import studio.lunabee.onesafe.hasExcludeSearch
import studio.lunabee.onesafe.module.AppTestFrameworkModule
import studio.lunabee.onesafe.test.test
import studio.lunabee.compose.androidtest.extension.printToCacheDir
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitAndPrintWholeScreenToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.ui.UiConstants

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class ItemDetailsDuplicateNavigationTest : ItemDetailsNavigationTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Test
    fun item_details_nav_to_duplicate_test() {
        invoke {
            navToItemDetails()
            hasText(getString(OSString.safeItemDetail_actionCard_duplicate))
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .performClick()
            isDialog()
                .waitAndPrintWholeScreenToCacheDir(printRule, "_dialog")
            hasText(getString(OSString.safeItemDetail_duplicateDialog_ok))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.safeItemDetail_duplicateFeedback_successAction))
                .waitAndPrintRootToCacheDir(printRule, suffix = "_snackbar_feedback")
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(testUUIDs[1]))
                .waitAndPrintRootToCacheDir(printRule, suffix = "_after_nav_to_copy")
                .assertIsDisplayed()
            Espresso.pressBack()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(testUUIDs[0]))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun item_details_nav_back_then_nav_to_duplicate_test() {
        val childItemName = "child"
        invoke {
            runBlocking { createItemUseCase.test(name = childItemName, parentId = item.id) }
            navToItemDetails()
            val duplicateText = getString(OSString.safeItemDetail_actionCard_duplicate)
            val duplicateDialogButtonText = getString(OSString.safeItemDetail_duplicateDialog_ok)
            val duplicateFeedbackActionText = getString(OSString.safeItemDetail_duplicateFeedback_successAction)
            // Open elements tab
            hasText(getQuantityString(OSPlurals.safeItemDetail_contentCard_tab_elements, 1, 1))
                .waitUntilExactlyOneExists()
                .performClick()
            // nav to child
            hasExcludeSearch(hasText(childItemName))
                .waitUntilExactlyOneExists()
                .performClick()
            // duplicate child & wait the feedback
            hasText(duplicateText)
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .performClick()
            hasText(duplicateDialogButtonText)
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(duplicateFeedbackActionText)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            // back to item
            Espresso.pressBack()
            onNodeWithTag(UiConstants.TestTag.Screen.itemDetailsScreen(item.id))
                .assertIsDisplayed()
            onRoot()
                .printToCacheDir(printRule, suffix = "_on_back_to_item")
            // nav to duplicated child using snackbar
            onNodeWithText(duplicateFeedbackActionText)
                .performClick()
            onNodeWithTag(UiConstants.TestTag.Screen.itemDetailsScreen(testUUIDs[2]))
                .assertIsDisplayed()
            // back to item
            Espresso.pressBack()
            onNodeWithTag(UiConstants.TestTag.Screen.itemDetailsScreen(item.id))
                .assertIsDisplayed()
            // duplicate item & wait the feedback
            onNodeWithText(duplicateText)
                .performScrollTo()
                .performClick()
            onNodeWithText(duplicateDialogButtonText)
                .performClick()
            hasText(duplicateFeedbackActionText)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            // back to home
            Espresso.pressBack()
            onNodeWithTag(UiConstants.TestTag.Screen.Home)
                .assertIsDisplayed()
            onRoot()
                .printToCacheDir(printRule, suffix = "_on_back_to_home")
            // nav to duplicated item using snackbar
            onNodeWithText(duplicateFeedbackActionText)
                .performClick()
            onNodeWithTag(UiConstants.TestTag.Screen.itemDetailsScreen(testUUIDs[3]))
                .assertIsDisplayed()
            // back to home
            Espresso.pressBack()
            onNodeWithTag(UiConstants.TestTag.Screen.Home)
                .assertIsDisplayed()
        }
    }

    @Test
    fun item_details_nav_to_duplicated_dismiss_snackbar_test() {
        invoke {
            navToItemDetails()
            val duplicateText = getString(OSString.safeItemDetail_actionCard_duplicate)
            val duplicateDialogButtonText = getString(OSString.safeItemDetail_duplicateDialog_ok)
            val duplicateFeedbackActionText = getString(OSString.safeItemDetail_duplicateFeedback_successAction)
            // Action duplicate
            onNodeWithText(duplicateText)
                .performScrollTo()
                .performClick()
            onNodeWithText(duplicateDialogButtonText)
                .performClick()
            hasText(duplicateFeedbackActionText)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            // Back to home
            Espresso.pressBack()
            // Nav to duplicated manually
            hasExcludeSearch(hasText(AppTestFrameworkModule.COPY_STRING.format(itemName)))
                .waitUntilExactlyOneExists()
                .performClick()
            onRoot()
                .printToCacheDir(printRule, suffix = "_snackbar_feedback_dismiss")
            onNodeWithText(duplicateFeedbackActionText)
                .assertDoesNotExist()
        }
    }
}
