package studio.lunabee.onesafe.navigation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.espresso.Espresso
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.hasExcludeSearch
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.ui.UiConstants
import javax.inject.Inject

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class CreateNewItemBottomSheetNavigationTest : OSMainActivityTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home {
        createItemUseCase(itemName, null, false, null, null)
    }

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    private val itemName = "test item"

    @Test
    fun home_to_create_bottom_sheet_and_back() {
        invoke {
            hasTestTag(UiConstants.TestTag.Screen.Home)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasTestTag(UiConstants.TestTag.BreadCrumb.OSCreateItemButton)
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.BottomSheet.CreateItemBottomSheet)
                .waitUntilExactlyOneExists()
                .performTouchInput { swipeUp() }
                .assertIsDisplayed()
            Espresso.pressBack()
            hasTestTag(UiConstants.TestTag.BottomSheet.CreateItemBottomSheet)
                .waitUntilDoesNotExist()
            hasTestTag(testTag = UiConstants.TestTag.Screen.Home)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun item_details_to_create_bottom_sheet_and_back() {
        invoke {
            hasExcludeSearch(hasText(itemName))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()
            hasTestTag(UiConstants.TestTag.BreadCrumb.OSCreateItemButton)
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(itemId = testUUIDs[0]))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasTestTag(UiConstants.TestTag.BottomSheet.CreateItemBottomSheet)
                .waitUntilExactlyOneExists()
                .performTouchInput { swipeUp() }
                .assertIsDisplayed()
            Espresso.pressBack()
            hasTestTag(UiConstants.TestTag.BottomSheet.CreateItemBottomSheet)
                .waitUntilDoesNotExist()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(itemId = testUUIDs[0]))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }
}
