package studio.lunabee.onesafe.feature.itemaction

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.MoveToBinItemUseCase
import studio.lunabee.onesafe.hasExcludeSearch
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.test
import studio.lunabee.onesafe.ui.UiConstants
import javax.inject.Inject

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class ItemDetailActionNavigationTest : OSMainActivityTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var moveToBinItemUseCase: MoveToBinItemUseCase

    private val itemName = "test item"
    private val itemName2: String = "item name 2"
    private var safeItem: SafeItem? = null

    override val initialTestState: InitialTestState = InitialTestState.Home {
        safeItem = createItemUseCase.test(itemName)
        createItemUseCase.test(itemName2)
    }

    @Test
    fun display_action_not_deleted() {
        invoke {
            val hasFavoriteText = hasText(getString(OSString.safeItemDetail_actionCard_addToFavorites))
            hasExcludeSearch(hasText(itemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(safeItem?.id!!))
                .waitUntilExactlyOneExists()
            hasTestTag(UiConstants.TestTag.ScrollableContent.ItemDetailLazyColumn)
                .waitUntilExactlyOneExists()
                .performScrollToNode(hasFavoriteText)
            hasFavoriteText
                .waitAndPrintRootToCacheDir(printRule, "_action_item_not_deleted")
                .assertExists()
            hasText(getString(OSString.safeItemDetail_actionCard_share))
                .waitUntilExactlyOneExists()
                .assertExists()
            hasText(getString(OSString.safeItemDetail_actionCard_move))
                .waitUntilExactlyOneExists()
                .assertExists()
            hasText(getString(OSString.safeItemDetail_actionCard_duplicate))
                .waitUntilExactlyOneExists()
                .assertExists()
            hasText(getString(OSString.safeItemDetail_actionCard_delete))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun display_action_deleted() {
        invoke {
            runTest {
                safeItem?.let {
                    moveToBinItemUseCase.invoke(it)
                }
            }
            val hasBinText = hasText(getString(OSString.common_bin))
            val hasRestoreText = hasText(getString(OSString.safeItemDetail_actionCard_restore))

            hasTestTag(UiConstants.TestTag.Item.HomeItemGrid)
                .waitUntilExactlyOneExists()
                .performScrollToNode(hasBinText)
            hasBinText
                .waitUntilExactlyOneExists()
                .performClick()
            hasExcludeSearch(hasText(itemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(safeItem?.id!!))
                .waitUntilExactlyOneExists()
            hasTestTag(UiConstants.TestTag.ScrollableContent.ItemDetailLazyColumn)
                .waitUntilExactlyOneExists()
                .performScrollToNode(hasRestoreText)
            hasRestoreText
                .waitAndPrintRootToCacheDir(printRule, "_action_item_deleted")
                .assertIsDisplayed()
            hasText(getString(OSString.safeItemDetail_actionCard_remove))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun action_favorite() {
        invoke {
            hasExcludeSearch(hasText(itemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(safeItem?.id!!))
                .waitUntilExactlyOneExists()
            hasText(getString(OSString.safeItemDetail_actionCard_addToFavorites))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.safeItemDetail_actionCard_removeFromFavorites))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun action_share() {
        invoke {
            val hasShareText = hasText(getString(OSString.safeItemDetail_actionCard_share))
            hasExcludeSearch(hasText(itemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(safeItem?.id!!))
                .waitUntilExactlyOneExists()
            hasTestTag(UiConstants.TestTag.ScrollableContent.ItemDetailLazyColumn)
                .waitUntilExactlyOneExists()
                .performScrollToNode(hasShareText)
            hasShareText
                .waitAndPrintRootToCacheDir(printRule, "_action_share")
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.ShareFileScreen)
                .waitAndPrintRootToCacheDir(printRule, "_share_screen")
                .assertIsDisplayed()
        }
    }

    @Test
    fun action_move() {
        invoke {
            val hasMoveText = hasText(getString(OSString.safeItemDetail_actionCard_move))

            hasExcludeSearch(hasText(itemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(safeItem?.id!!))
                .waitUntilExactlyOneExists()
            hasTestTag(UiConstants.TestTag.ScrollableContent.ItemDetailLazyColumn)
                .waitUntilExactlyOneExists()
                .performScrollToNode(hasMoveText)
            hasMoveText
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.MoveHostScreen)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasText(itemName2)
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(itemName)
                .waitUntilDoesNotExist()
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
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(safeItem?.id!!))
                .waitUntilExactlyOneExists()
                .performClick()
        }
    }

    @Test
    fun action_duplicate_test() {
        invoke {
            val hasDuplicateText = hasText(getString(OSString.safeItemDetail_actionCard_duplicate))

            hasExcludeSearch(hasText(itemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(safeItem?.id!!))
                .waitUntilExactlyOneExists()
            hasTestTag(UiConstants.TestTag.ScrollableContent.ItemDetailLazyColumn)
                .waitUntilExactlyOneExists()
                .performScrollToNode(hasDuplicateText)
            hasDuplicateText
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

    @Test
    fun action_move_to_bin() {
        invoke {
            val hasMoveToBinText = hasText(getString(OSString.safeItemDetail_actionCard_delete))
            val hasBinText = hasText(getString(OSString.common_bin))

            hasExcludeSearch(hasText(itemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(safeItem?.id!!))
                .waitUntilExactlyOneExists()
            hasTestTag(UiConstants.TestTag.ScrollableContent.ItemDetailLazyColumn)
                .waitUntilExactlyOneExists()
                .performScrollToNode(hasMoveToBinText)
            hasMoveToBinText
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.safeItemDetail_delete_alert_button_confirm))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.safeItemDetail_moveIntoBin_success_message))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.Home)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasTestTag(UiConstants.TestTag.Item.HomeItemGrid)
                .waitUntilExactlyOneExists()
                .performScrollToNode(hasBinText)
            hasBinText
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(itemName)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun display_action_menu_restore() {
        invoke {
            runTest {
                safeItem?.let {
                    moveToBinItemUseCase.invoke(it)
                }
            }
            val hasBinText = hasText(getString(OSString.common_bin))
            val hasRestoreText = hasText(getString(OSString.safeItemDetail_actionCard_restore))

            hasTestTag(UiConstants.TestTag.Item.HomeItemGrid)
                .waitUntilExactlyOneExists()
                .performScrollToNode(hasBinText)
            hasBinText
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(itemName)
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(safeItem?.id!!))
                .waitUntilExactlyOneExists()
            hasTestTag(UiConstants.TestTag.ScrollableContent.ItemDetailLazyColumn)
                .waitUntilExactlyOneExists()
                .performScrollToNode(hasRestoreText)
            hasRestoreText
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.restoreFeedback_successMessage))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasTestTag(UiConstants.TestTag.Screen.Bin)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun display_action_menu_delete_permanently() {
        invoke {
            runTest {
                safeItem?.let {
                    moveToBinItemUseCase.invoke(it)
                }
            }
            val hasBinText = hasText(getString(OSString.common_bin))
            val hasDeleteText = hasText(getString(OSString.safeItemDetail_actionCard_remove))

            hasTestTag(UiConstants.TestTag.Item.HomeItemGrid)
                .waitUntilExactlyOneExists()
                .performScrollToNode(hasBinText)
            hasBinText
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(itemName)
                .waitUntilExactlyOneExists()
                .performClick()
            val listMatcher = hasTestTag(UiConstants.TestTag.ScrollableContent.ItemDetailLazyColumn)
            listMatcher
                .waitUntilExactlyOneExists()
                .performScrollToNode(hasDeleteText)
            hasDeleteText
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.safeItemDetail_remove_alert_button_confirm))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.safeItemDetail_delete_success_message))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasTestTag(UiConstants.TestTag.Screen.Bin)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }
}
