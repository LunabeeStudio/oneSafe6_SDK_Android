package studio.lunabee.onesafe.navigation.search

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.platform.app.InstrumentationRegistry
import com.lunabee.lbextensions.lazyFast
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitAndPrintWholeScreenToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.ui.UiConstants
import javax.inject.Inject

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class SearchScreenBehaviourNavigationTest : OSMainActivityTest() {

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
    fun search_with_search_text_field_focus_on_open_test() {
        invoke {
            openSearchScreen()
            hasTestTag(UiConstants.TestTag.Item.SearchTextField)
                .waitUntilExactlyOneExists()
                .assertIsFocused()
        }
    }

    @Test
    fun close_keyboard_on_leaving_search() {
        invoke {
            openSearchScreen()
            hasTestTag(UiConstants.TestTag.Item.SearchTextField)
                .waitAndPrintWholeScreenToCacheDir(printRule, "_0_search_screen")
                .assertIsFocused()
            hasTestTag(UiConstants.TestTag.Screen.SearchScreen)
                .waitUntilExactlyOneExists()
                .performTouchInput { swipeDown() }
            hasTestTag(UiConstants.TestTag.Item.SearchTextField)
                .waitUntilDoesNotExist()
        }
    }

    @Test
    fun search_with_search_text_field_not_focus_on_open_if_already_in_search_test() {
        invoke {
            openSearchScreen()
            hasTestTag(UiConstants.TestTag.Item.SearchTextField)
                .waitAndPrintWholeScreenToCacheDir(printRule, "_0_search_screen")
                .performTextReplacement("item")
            hasTestTag(UiConstants.TestTag.Screen.SearchScreen)
                .waitAndPrintWholeScreenToCacheDir(printRule, "_1_home_screen")
                .performTouchInput { swipeDown() }
            hasTestTag(UiConstants.TestTag.Screen.SearchScreen)
                .waitUntilDoesNotExist()
                .assertDoesNotExist()
            openSearchScreen()
            hasTestTag(UiConstants.TestTag.Item.SearchTextField)
                .waitAndPrintWholeScreenToCacheDir(printRule, "_2_search_screen")
            hasTestTag(UiConstants.TestTag.Item.SearchTextField)
                .waitUntilExactlyOneExists()
                .assertIsNotFocused()
        }
    }

    @Test
    fun focus_text_field_on_clear_search_test() {
        invoke {
            openSearchScreen()
            hasTestTag(UiConstants.TestTag.Item.SearchTextField)
                .waitUntilExactlyOneExists()
                .performTextReplacement("item")
            hasTestTag(UiConstants.TestTag.Item.SearchTextField)
                .waitUntilExactlyOneExists()
                .performImeAction()
            hasTestTag(UiConstants.TestTag.Item.SearchTextField)
                .waitAndPrintWholeScreenToCacheDir(printRule)
                .assertIsNotFocused()
            onAllNodesWithContentDescription(context.getString(OSString.common_cancel))
                .filterToOne(hasAnyAncestor(hasTestTag(UiConstants.TestTag.Screen.SearchScreen)))
                .performClick()
            hasTestTag(UiConstants.TestTag.Item.SearchTextField)
                .waitUntilExactlyOneExists()
                .assertIsFocused()
        }
    }

    private fun ComposeUiTest.openSearchScreen() {
        onNodeWithContentDescription(context.getString(OSString.breadcrumb_accessibility_search))
            .performClick()
        hasTestTag(UiConstants.TestTag.Screen.SearchScreen)
            .waitUntilExactlyOneExists()
            .assertIsDisplayed()
    }
}
