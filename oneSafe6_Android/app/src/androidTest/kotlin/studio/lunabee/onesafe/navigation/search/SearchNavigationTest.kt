package studio.lunabee.onesafe.navigation.search

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.lunabee.lbextensions.lazyFast
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitAndPrintWholeScreenToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilAtLeastOneExists
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.hasExcludeSearch
import studio.lunabee.onesafe.hasOnlySearch
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.ui.UiConstants
import javax.inject.Inject

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class SearchNavigationTest : OSMainActivityTest() {

    private val context by lazyFast { InstrumentationRegistry.getInstrumentation().targetContext }

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home {
        createItemUseCase(firstItemName, null, false, null, null)
        createItemUseCase(secondItemName, null, false, null, null)
    }

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    private val firstItemName = "firstItem"
    private val secondItemName = "secondItem"

    @Test
    fun open_search_from_breadcrumb_and_close_test() {
        invoke {
            openSearchScreen()
            hasTestTag(UiConstants.TestTag.Screen.SearchScreen)
                .waitAndPrintWholeScreenToCacheDir(printRule)
                .performTouchInput { swipeDown() }
                .assertDoesNotExist()
        }
    }

    @Test
    fun open_search_and_navigate_to_item_test() {
        invoke {
            openSearchScreen()
            hasTestTag(UiConstants.TestTag.Item.SearchTextField)
                .waitAndPrintWholeScreenToCacheDir(printRule)
                .performTextReplacement("item")
            hasOnlySearch(hasText(firstItemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(testUUIDs[0]))
                .waitAndPrintWholeScreenToCacheDir(printRule)
                .assertIsDisplayed()
            hasTestTag(UiConstants.TestTag.Screen.SearchScreen)
                .waitUntilDoesNotExist()
                .assertDoesNotExist()
        }
    }

    @Test
    fun open_search_and_navigate_back_button_and_system_test() {
        invoke {
            openSearchScreen()
            // Test from button
            hasOnlySearch(hasContentDescription(context.getString(OSString.common_accessibility_back)))
                .waitUntilAtLeastOneExists()
                .filterToOne(hasAnyAncestor(hasTestTag(UiConstants.TestTag.Screen.SearchScreen)))
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.SearchScreen)
                .waitUntilDoesNotExist()
                .assertDoesNotExist()
            openSearchScreen()
            Espresso.closeSoftKeyboard()
            Espresso.pressBack()
            hasTestTag(UiConstants.TestTag.Screen.SearchScreen)
                .waitUntilDoesNotExist()
        }
    }

    @Test
    fun open_navigate_back_from_item_after_search() {
        invoke {
            hasExcludeSearch(hasText(firstItemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(testUUIDs[0]))
                .waitAndPrintWholeScreenToCacheDir(printRule)
                .assertIsDisplayed()
            openSearchScreen()
            hasTestTag(UiConstants.TestTag.Item.SearchTextField)
                .waitAndPrintWholeScreenToCacheDir(printRule)
                .performTextReplacement("second")
            hasOnlySearch(hasText(secondItemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(testUUIDs[1]))
                .waitAndPrintWholeScreenToCacheDir(printRule)
                .assertIsDisplayed()
            Espresso.closeSoftKeyboard()
            Espresso.pressBack()
            hasTestTag(UiConstants.TestTag.Screen.Home)
                .waitAndPrintWholeScreenToCacheDir(printRule)
                .assertIsDisplayed()
        }
    }

    private fun ComposeUiTest.openSearchScreen() {
        hasContentDescription(getString(OSString.breadcrumb_accessibility_search))
            .waitUntilExactlyOneExists()
            .performClick()
        hasTestTag(UiConstants.TestTag.Screen.SearchScreen)
            .waitUntilExactlyOneExists()
            .assertIsDisplayed()
        mainClock.advanceTimeBy(AppConstants.Ui.Animation.BottomSheet.AppearanceDelay)
        keyboardHelper.waitForKeyboardVisibility(visible = true)
        Espresso.closeSoftKeyboard()
        keyboardHelper.waitForKeyboardVisibility(visible = false)
    }
}
