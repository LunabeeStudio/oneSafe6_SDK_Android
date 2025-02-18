package studio.lunabee.onesafe.feature.itemaction

import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitAndPrintWholeScreenToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilAtLeastOneExists
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.MainActivity
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.MoveToBinItemUseCase
import studio.lunabee.onesafe.hasExcludeSearch
import studio.lunabee.onesafe.test.test
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.ui.UiConstants
import javax.inject.Inject

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class QuickActionNavigationTest : OSMainActivityTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var moveToBinItemUseCase: MoveToBinItemUseCase

    private val deletedItemName = "item1"
    private val itemName: String = "item2"
    private lateinit var deletedItem: SafeItem

    override val initialTestState: InitialTestState = InitialTestState.Home {
        deletedItem = createItemUseCase.test(deletedItemName)
        createItemUseCase.test(itemName)
    }

    /**
     * Assert the number of quick actions available to not forgot new actions
     */
    // FIXME <Flaky>
    @Test
    fun count_quick_action_menu_stability_test(): TestResult = runTest {
        return@runTest
        moveToBinItemUseCase(deletedItem)
        invoke {
            hasExcludeSearch(hasText(itemName))
                .waitUntilExactlyOneExists()
                .performTouchInput { longClick(durationMillis = 5000) }
            hasTestTag(UiConstants.TestTag.Item.QuickItemActionMenu)
                .waitAndPrintWholeScreenToCacheDir(printRule, "_action_menu")
                .onChildren()
                .assertCountEquals(5)
            onAllNodes(isRoot())
                .onFirst()
                .performClick()
            navToBin()
            hasExcludeSearch(hasText(deletedItemName))
                .waitUntilExactlyOneExists()
                .performTouchInput { longClick(durationMillis = 5000) }
            hasTestTag(UiConstants.TestTag.Item.QuickItemActionMenu)
                .waitAndPrintWholeScreenToCacheDir(printRule, "_action_menu_deleted")
                .onChildren()
                .assertCountEquals(2)
        }
    }

    // FIXME <Flaky>
    @Test
    fun display_quick_action_menu_not_deleted() {
        return
        invoke {
            hasExcludeSearch(hasText(deletedItemName))
                .waitUntilExactlyOneExists()
                .performTouchInput { longClick(durationMillis = 5000) }
            hasTestTag(UiConstants.TestTag.Item.QuickItemActionMenu)
                .waitAndPrintWholeScreenToCacheDir(printRule, "_action_menu")
                .assertIsDisplayed()
            hasText(getString(OSString.safeItemDetail_actionCard_addToFavorites))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasText(getString(OSString.safeItemDetail_actionCard_share))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasText(getString(OSString.safeItemDetail_actionCard_move))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasText(getString(OSString.safeItemDetail_actionCard_duplicate))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasText(getString(OSString.safeItemDetail_actionCard_delete))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    // FIXME <Flaky>
    @Test
    fun display_quick_action_menu_deleted() {
        return
        runTest {
            moveToBinItemUseCase.invoke(deletedItem)
        }
        invoke {
            navToBin()
            hasText(deletedItemName)
                .waitUntilExactlyOneExists()
                .performTouchInput {
                    longClick(durationMillis = 5000)
                }
            hasTestTag(UiConstants.TestTag.Item.QuickItemActionMenu)
                .waitAndPrintWholeScreenToCacheDir(printRule, "_action_deleted_item")
                .assertIsDisplayed()
            hasText(getString(OSString.safeItemDetail_actionCard_restore))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasText(getString(OSString.safeItemDetail_actionCard_remove))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    private fun AndroidComposeUiTest<MainActivity>.navToBin() {
        onNode(hasTestTag(UiConstants.TestTag.Item.HomeItemGrid))
            .performScrollToNode(hasText(getString(OSString.common_bin)))
        hasText(getString(OSString.common_bin))
            .waitAndPrintWholeScreenToCacheDir(printRule, "_bin")
            .performClick()
    }

    // FIXME <Flaky>
    @Test
    fun quick_action_favorite() {
        return
        invoke {
            hasText(deletedItemName)
                .waitUntilExactlyOneExists()
                .performTouchInput {
                    longClick(durationMillis = 5000)
                }
            hasTestTag(UiConstants.TestTag.Item.QuickItemActionMenu)
                .waitAndPrintWholeScreenToCacheDir(printRule, "_action_menu")
                .assertIsDisplayed()
            hasText(getString(OSString.safeItemDetail_actionCard_addToFavorites))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(deletedItemName)
                .waitUntilAtLeastOneExists()[0]
                .performTouchInput {
                    longClick(durationMillis = 5000)
                }
            hasText(getString(OSString.safeItemDetail_actionCard_removeFromFavorites))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(deletedItemName)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    // FIXME <Flaky>
    @Test
    fun quick_action_share() {
        return
        invoke {
            hasText(deletedItemName)
                .waitUntilExactlyOneExists()
                .performTouchInput {
                    longClick(durationMillis = 5000)
                }
            hasTestTag(UiConstants.TestTag.Item.QuickItemActionMenu)
                .waitAndPrintWholeScreenToCacheDir(printRule, "_action_menu")
                .assertIsDisplayed()
            hasText(getString(OSString.safeItemDetail_actionCard_share))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.ShareFileScreen)
                .waitUntilAtLeastOneExists()[0]
                .assertIsDisplayed()
        }
    }

    // FIXME <Flaky>
    @Test
    fun quick_action_move() {
        return
        invoke {
            hasText(deletedItemName)
                .waitUntilExactlyOneExists()
                .performTouchInput {
                    longClick(durationMillis = 5000)
                }
            hasTestTag(UiConstants.TestTag.Item.QuickItemActionMenu)
                .waitAndPrintWholeScreenToCacheDir(printRule, "_action_menu")
                .assertIsDisplayed()
            hasText(getString(OSString.safeItemDetail_actionCard_move))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.MoveHostScreen)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasText(itemName)
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.move_selectDestination_moveHereButton))
                .waitUntilExactlyOneExists(useUnmergedTree = true)
                .performClick()
            hasText(getString(OSString.move_dialog_confirmButton))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.move_success_message))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.common_see))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(deletedItem.id))
                .waitUntilExactlyOneExists()
                .performClick()
        }
    }

    // FIXME <Flaky>
    @Test
    fun quick_action_duplicate_test() {
        return
        invoke {
            hasText(deletedItemName)
                .waitUntilExactlyOneExists()
                .performTouchInput {
                    longClick(durationMillis = 5000)
                }
            hasTestTag(UiConstants.TestTag.Item.QuickItemActionMenu)
                .waitAndPrintWholeScreenToCacheDir(printRule, "_action_menu")
                .assertIsDisplayed()
            hasText(getString(OSString.safeItemDetail_actionCard_duplicate))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.safeItemDetail_actionCard_duplicate))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.safeItemDetail_duplicateDialog_ok))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.safeItemDetail_duplicateFeedback_successAction))
                .waitUntilExactlyOneExists()
                .performClick()
        }
    }

    // FIXME <Flaky>
    @Test
    fun quick_action_move_to_bin() {
        return
        invoke {
            hasText(deletedItemName)
                .waitUntilExactlyOneExists()
                .performTouchInput {
                    longClick(durationMillis = 5000)
                }
            hasTestTag(UiConstants.TestTag.Item.QuickItemActionMenu)
                .waitAndPrintWholeScreenToCacheDir(printRule, "_action_menu")
                .assertIsDisplayed()
            hasText(getString(OSString.safeItemDetail_actionCard_delete))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.safeItemDetail_delete_alert_button_confirm))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.safeItemDetail_moveIntoBin_success_message))
                .waitUntilExactlyOneExists()
                .performClick()
            onNode(hasTestTag(UiConstants.TestTag.Item.HomeItemGrid)).performTouchInput { swipeUp() }
            hasText(getString(OSString.common_bin))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(deletedItemName)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    // FIXME <Flaky>
    @Test
    fun display_quick_action_menu_restore() {
        return
        runTest {
            moveToBinItemUseCase.invoke(deletedItem)
        }
        invoke {
            onNode(hasTestTag(UiConstants.TestTag.Item.HomeItemGrid)).performTouchInput { swipeUp() }
            hasText(getString(OSString.common_bin))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(deletedItemName)
                .waitUntilExactlyOneExists()
                .performTouchInput {
                    longClick(durationMillis = 5000)
                }
            hasText(getString(OSString.safeItemDetail_actionCard_restore))
                .waitAndPrintWholeScreenToCacheDir(printRule, "_action_restore")
                .performClick()
            hasText(getString(OSString.restoreFeedback_successMessage))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    // FIXME <Flaky>
    @Test
    fun display_quick_action_menu_delete_permanently() {
        return
        runTest {
            moveToBinItemUseCase.invoke(deletedItem)
        }
        invoke {
            onNode(hasTestTag(UiConstants.TestTag.Item.HomeItemGrid)).performTouchInput { swipeUp() }
            hasText(getString(OSString.common_bin))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(deletedItemName)
                .waitUntilExactlyOneExists()
                .performTouchInput {
                    longClick(durationMillis = 5000)
                }
            hasText(getString(OSString.safeItemDetail_actionCard_remove))
                .waitAndPrintWholeScreenToCacheDir(printRule, "_action_menu")
                .performClick()
            hasText(getString(OSString.safeItemDetail_remove_alert_button_confirm))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.safeItemDetail_delete_success_message))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }
}
