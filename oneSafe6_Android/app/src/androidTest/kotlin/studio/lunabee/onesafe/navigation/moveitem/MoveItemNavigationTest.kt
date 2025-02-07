package studio.lunabee.onesafe.navigation.moveitem

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso
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
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.test
import studio.lunabee.onesafe.ui.UiConstants
import java.util.UUID
import javax.inject.Inject

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class MoveItemNavigationTest : OSMainActivityTest() {

    override val initialTestState: InitialTestState = InitialTestState.Home {
        val item = createItemUseCase.test(name = lunabeeItemName)
        lunabeeItemId = item.id

        val itemWithChild = createItemUseCase.test(name = macItemName)
        macItemId = itemWithChild.id
        ideItemId = createItemUseCase.test(name = ideItemName, parentId = macItemId).id
    }

    @get:Rule(order = 0)
    override var hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var createItemUseCase: CreateItemUseCase

    private val lunabeeItemName: String = "Lunabee"
    private val macItemName: String = "Mac"
    private val ideItemName: String = "Ide"

    private lateinit var lunabeeItemId: UUID
    private lateinit var macItemId: UUID
    private lateinit var ideItemId: UUID

    @Test
    fun item_detail_to_move_test() {
        invoke {
            hasExcludeSearch(hasText(lunabeeItemName))
                .waitAndPrintRootToCacheDir(printRule, suffix = "_init")
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(lunabeeItemId))
                .waitAndPrintRootToCacheDir(printRule, "_${lunabeeItemName}_detailsScreen")
                .assertIsDisplayed()
            hasText(getString(OSString.safeItemDetail_actionCard_move))
                .waitAndPrintRootToCacheDir(printRule, "_move_action")
                .performScrollTo()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.SelectMoveDestinationScreen)
                .waitAndPrintRootToCacheDir(printRule, "_selectMoveDestinationScreen")
            hasText(lunabeeItemName)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun cannot_move_at_same_place_test() {
        invoke {
            hasExcludeSearch(hasText(lunabeeItemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(lunabeeItemId))
                .waitAndPrintRootToCacheDir(printRule, "_${lunabeeItemName}_detailsScreen")
                .assertIsDisplayed()
            hasText(getString(OSString.safeItemDetail_actionCard_move))
                .waitAndPrintRootToCacheDir(printRule, "_move_action")
                .performScrollTo()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.SelectMoveDestinationScreen)
                .waitAndPrintRootToCacheDir(printRule, "_selectMoveDestinationScreen")
            hasTestTag(UiConstants.TestTag.Item.MoveHereButton)
                .waitUntilExactlyOneExists()
                .assertHasNoClickAction()
        }
    }

    @Test
    fun cannot_move_into_self_alert_test() {
        invoke {
            hasExcludeSearch(hasText(lunabeeItemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(lunabeeItemId))
                .waitAndPrintRootToCacheDir(printRule, "_${lunabeeItemName}_detailsScreen")
                .assertIsDisplayed()
            hasText(getString(OSString.safeItemDetail_actionCard_move))
                .waitAndPrintRootToCacheDir(printRule, "_move_action")
                .performScrollTo()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.SelectMoveDestinationScreen)
                .waitAndPrintRootToCacheDir(printRule, "_selectMoveDestinationScreen")
            hasExcludeSearch(hasText(macItemName))
                .waitUntilExactlyOneExists()
            hasExcludeSearch(hasText(ideItemName))
                .waitUntilDoesNotExist()
            hasExcludeSearch(hasText(lunabeeItemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.move_selectDestination_moveIntoItself_warning))
                .waitAndPrintRootToCacheDir(printRule, "_feedback")
                .assertIsDisplayed()
            hasExcludeSearch(hasText(macItemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasExcludeSearch(hasText(ideItemName))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun move_end_to_end_navigate_to_item_after() {
        invoke {
            hasExcludeSearch(hasText(lunabeeItemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(lunabeeItemId))
                .waitAndPrintRootToCacheDir(printRule, "_${lunabeeItemName}_detailsScreen")
                .assertIsDisplayed()
            hasText(getString(OSString.safeItemDetail_actionCard_move))
                .waitAndPrintRootToCacheDir(printRule, "_move_action")
                .performScrollTo()
                .performClick()
            hasTestTag(UiConstants.TestTag.Item.MoveHereButton)
                .waitUntilExactlyOneExists()
                .assertHasNoClickAction()
            hasText(macItemName)
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(ideItemName)
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Item.MoveHereButton)
                .waitUntilExactlyOneExists()
                .performClick()
            isDialog()
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasText(getString(OSString.move_dialog_message, ideItemName))
            hasText(getString(OSString.move_dialog_confirmButton))
                .waitUntilExactlyOneExists()
                .performClick()

            // Initial parent was home so test that we move into home after move
            hasTestTag(UiConstants.TestTag.Screen.Home)
                .waitAndPrintRootToCacheDir(printRule, "_${lunabeeItemName}_detailsScreen")
                .assertIsDisplayed()

            // Test that the snack bar is display and that there is a see button
            hasText(getString(OSString.move_success_message))
            hasText(getString(OSString.common_see))
                .waitUntilExactlyOneExists()
                .performClick()

            // Test that we navigate to the moved item detail
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(lunabeeItemId))
                .waitAndPrintRootToCacheDir(printRule)
        }
    }

    @Test
    fun move_end_to_end_item_with_children() {
        invoke {
            hasExcludeSearch(hasText(macItemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(macItemId))
                .waitAndPrintRootToCacheDir(printRule, "_${lunabeeItemName}_detailsScreen")
                .assertIsDisplayed()
            hasText(getString(OSString.safeItemDetail_actionCard_move))
                .waitAndPrintRootToCacheDir(printRule, "_move_action")
                .performScrollTo()
                .performClick()
            hasTestTag(UiConstants.TestTag.Item.MoveHereButton)
                .waitUntilExactlyOneExists()
                .assertHasNoClickAction()
            hasText(lunabeeItemName)
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Item.MoveHereButton)
                .waitUntilExactlyOneExists()
                .performClick()
            isDialog()
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()

            hasText(getString(OSString.move_dialog_message, lunabeeItemName))

            hasText(getString(OSString.move_dialog_confirmButton))
                .waitUntilExactlyOneExists()
                .performClick()

            // Initial parent was home so test that we move into home after move
            hasTestTag(UiConstants.TestTag.Screen.Home)
                .waitAndPrintRootToCacheDir(printRule, "_${UiConstants.TestTag.Screen.Home}")
                .assertIsDisplayed()

            // Test that the snack bar is display and that there is a see button
            hasText(getString(OSString.move_success_message))
            hasText(getString(OSString.common_see))
                .waitUntilExactlyOneExists()
                .performClick()

            // Test that we navigate to the moved item detail
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(macItemId))
                .waitAndPrintRootToCacheDir(printRule)
        }
    }

    @Test
    fun move_cancel_back_test() {
        invoke {
            hasExcludeSearch(hasText(lunabeeItemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(lunabeeItemId))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasText(getString(OSString.safeItemDetail_actionCard_move))
                .waitAndPrintRootToCacheDir(printRule, "_move_action")
                .performScrollTo()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.SelectMoveDestinationScreen)
                .waitAndPrintRootToCacheDir(printRule, "_selectMoveDestinationScreen")
            Espresso.pressBack()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(lunabeeItemId))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }
}
