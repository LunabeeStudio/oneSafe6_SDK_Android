package studio.lunabee.onesafe.navigation.search

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.platform.app.InstrumentationRegistry
import com.lunabee.lbextensions.lazyFast
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
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
class SearchIdleScreenNavigationTest : OSMainActivityTest() {

    private val context by lazyFast { InstrumentationRegistry.getInstrumentation().targetContext }

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home {
        createItemUseCase(firstItemName, null, false, null, null)
        createItemUseCase(secondItemName, null, false, null, null)
        createItemUseCase(thirdItemName, null, false, null, null)
    }

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    private val firstItemName = "first"
    private val secondItemName = "second"
    private val thirdItemName = "third"

    /**
     * Assert that consulting an item make it appears in first place on the recently consulted items
     */
    @Test
    fun recently_consulted_update_test() {
        invoke {
            hasExcludeSearch(hasText(secondItemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(testUUIDs[1]))
                .waitAndPrintRootToCacheDir(printRule, "_itemDetails_screen")
            openSearchScreen()
            hasOnlySearch(hasText(secondItemName))
                .waitUntilExactlyOneExists().assertIsDisplayed()
        }
    }

    @Test
    fun add_recent_search_test() {
        invoke {
            openSearchScreen()
            hasOnlySearch(hasText(context.getString(OSString.searchScreen_recent_search_title)))
                .waitUntilDoesNotExist().assertDoesNotExist()

            // Search but not click -> Should not add recent search
            hasTestTag(UiConstants.TestTag.Item.SearchTextField)
                .waitUntilExactlyOneExists()
                .performTextReplacement("fir")
            hasText(context.getString(OSString.searchScreen_result_title, 1)).waitUntilExactlyOneExists()
            hasOnlySearch(hasTestTag(UiConstants.TestTag.OSAppBarMenu))
                .waitUntilExactlyOneExists()
                .performClick()
            hasOnlySearch(hasText(context.getString(OSString.searchScreen_recent_search_title)))
                .waitUntilDoesNotExist().assertDoesNotExist()

            // Search and click on item -> Should add recent search
            hasTestTag(UiConstants.TestTag.Item.SearchTextField)
                .waitUntilExactlyOneExists()
                .performTextReplacement("fir")
            hasText(context.getString(OSString.searchScreen_result_title, 1))
                .waitUntilExactlyOneExists()
            hasOnlySearch(hasText(firstItemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(testUUIDs[0]))
                .waitAndPrintRootToCacheDir(printRule, "_itemDetails_screen")
            openSearchScreen()
            hasOnlySearch(hasTestTag(UiConstants.TestTag.OSAppBarMenu))
                .waitUntilExactlyOneExists()
                .performClick()
            hasOnlySearch(hasTestTag(UiConstants.TestTag.Item.RecentSearchItem))
                .waitUntilExactlyOneExists()
        }
    }

    private fun ComposeUiTest.openSearchScreen() {
        hasContentDescription(getString(OSString.breadcrumb_accessibility_search))
            .waitUntilExactlyOneExists()
            .performClick()
        hasTestTag(UiConstants.TestTag.Screen.SearchScreen)
            .waitUntilExactlyOneExists()
            .assertIsDisplayed()
    }
}
