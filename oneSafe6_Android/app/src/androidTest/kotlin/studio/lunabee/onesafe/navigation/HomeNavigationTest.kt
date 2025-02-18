package studio.lunabee.onesafe.navigation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.printToCacheDir
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilAtLeastOneExists
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.MoveToBinItemUseCase
import studio.lunabee.onesafe.hasExcludeSearch
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.test
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.ui.UiConstants
import javax.inject.Inject

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class HomeNavigationTest : OSMainActivityTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    private val itemName = "test item"
    override val initialTestState: InitialTestState = InitialTestState.Home {
        createItemUseCase(itemName, null, false, null, null)
    }

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var moveToBinItemUseCase: MoveToBinItemUseCase

    @Test
    fun home_to_item_details_test() {
        invoke {
            hasExcludeSearch(hasText(itemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(testUUIDs[0]))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun home_to_item_details_favorite_test() {
        val favoriteItemName = "favorite"
        invoke {
            runBlocking {
                createItemUseCase.test(name = favoriteItemName, isFavorite = true)
            }
            hasText(favoriteItemName)
                .waitUntilAtLeastOneExists()
                .filterToOne(hasAnyAncestor(hasTestTag(UiConstants.TestTag.Item.HomeItemSectionRow)))
                .assertIsDisplayed()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(testUUIDs[1]))
                .waitAndPrintRootToCacheDir(printRule)
                .assertIsDisplayed()
        }
    }

    @Test
    fun home_to_bin_other_item_test() {
        invoke {
            runBlocking {
                loginUseCase(testPassword.toCharArray())
                val item = createItemUseCase.test()
                moveToBinItemUseCase(item)
            }
            hasText(getString(OSString.common_bin))
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.Bin)
                .waitAndPrintRootToCacheDir(printRule)
                .assertIsDisplayed()
        }
    }

    @Test
    fun home_to_settings_other_item_test() {
        invoke {
            hasText(getString(OSString.home_settings_title))
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .performClick()
            onNodeWithTag(UiConstants.TestTag.Screen.Settings)
                .assertIsDisplayed()
                .printToCacheDir(printRule)
        }
    }
}
