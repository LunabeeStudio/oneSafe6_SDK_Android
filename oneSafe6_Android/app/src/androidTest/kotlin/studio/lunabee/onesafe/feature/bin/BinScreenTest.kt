package studio.lunabee.onesafe.feature.bin

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.paging.PagingData
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbextensions.lazyFast
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.printToCacheDir
import studio.lunabee.compose.androidtest.extension.waitAndPrintWholeScreenToCacheDir
import studio.lunabee.onesafe.AppAndroidTestUtils
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.usecase.item.CountSafeItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.RemoveAllDeletedItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.RestoreItemUseCase
import studio.lunabee.onesafe.feature.itemactions.GetSafeItemActionHelper
import studio.lunabee.onesafe.ui.UiConstants

@OptIn(ExperimentalTestApi::class)
class BinScreenTest : LbcComposeTest() {
    private val countSafeItemUseCase: CountSafeItemUseCase = mockk()
    private val restoreItemUseCase: RestoreItemUseCase = mockk {
        coEvery<LBResult<Unit>> { this@mockk.invoke(null) } returns LBResult.Success(Unit)
    }
    private val removeAllDeletedItemUseCase: RemoveAllDeletedItemUseCase = mockk {
        coEvery<LBResult<Unit>> { this@mockk.invoke() } returns LBResult.Success(Unit)
    }

    private val getSafeItemActionHelper: GetSafeItemActionHelper = mockk {
        coEvery { getQuickActions(any(), any()) } returns { listOf() }
        every { navigationAction } returns MutableStateFlow(null)
        every { itemActionDialogState } returns MutableStateFlow(null)
        every { itemActionSnackbarState } returns MutableStateFlow(null)
    }

    private val navigateBack: () -> Unit = spyk({ })

    private val pagedPlainItemDataUseCase: BinPagedPlainItemDataUseCase = mockk {
        every { this@mockk.invoke() } returns flowOf(PagingData.empty(sourceLoadStates = AppAndroidTestUtils.loadedPagingStates()))
    }

    // BinViewModel cannot be mockk because of getActions returning a LinkedHashSet
    // https://github.com/mockk/mockk/issues/340#issuecomment-709122959
    private val viewModel by lazyFast {
        BinViewModel(
            countSafeItemUseCase,
            restoreItemUseCase,
            removeAllDeletedItemUseCase,
            getSafeItemActionHelper,
            pagedPlainItemDataUseCase,
        )
    }

    @Test
    fun bin_empty_state_test() {
        coEvery { countSafeItemUseCase.deleted(any()) } returns LBResult.Success(0)

        setBinScreen(viewModel) {
            onRoot().printToCacheDir(printRule)
            onNodeWithTag(UiConstants.TestTag.Screen.Bin).assertIsDisplayed()
            onNodeWithText(getString(OSString.bin_empty_card_title)).assertIsDisplayed()
            onNodeWithTag(UiConstants.TestTag.Item.BinItemGrid).assertDoesNotExist()
        }
    }

    @Test
    fun bin_filled_state_test() {
        val itemSize = 100
        val lastIndex = itemSize - 1
        val testSuffix = "_test"

        val safeItemPaginationList = AppAndroidTestUtils.createPlainItemData(
            size = itemSize,
            itemNameProvider = { DefaultNameProvider("${it}$testSuffix") },
        )

        every {
            pagedPlainItemDataUseCase.invoke()
        } returns flowOf(PagingData.from(safeItemPaginationList, sourceLoadStates = AppAndroidTestUtils.loadedPagingStates()))
        coEvery { countSafeItemUseCase.deleted(any()) } returns LBResult.Success(safeItemPaginationList.size)

        setBinScreen(viewModel) {
            onRoot()
                .printToCacheDir(printRule, suffix = "_top")
            onNodeWithTag(UiConstants.TestTag.Screen.Bin)
                .assertIsDisplayed()

            val gridNode = onNodeWithTag(UiConstants.TestTag.Item.BinItemGrid)
            gridNode.assertIsDisplayed()

            val firstItemNode = onNodeWithText("0$testSuffix")
            val lastItemNode = onNodeWithText("${lastIndex}$testSuffix")

            firstItemNode.assertIsDisplayed()
            lastItemNode.assertDoesNotExist()

            gridNode
                .assert(hasScrollToIndexAction())
                .assert(hasScrollAction())
                .performScrollToIndex(lastIndex)

            onRoot().printToCacheDir(printRule, suffix = "_bottom")

            firstItemNode.assertIsNotDisplayed()
            lastItemNode.assertIsDisplayed()

            onAllNodesWithText(text = testSuffix, substring = true)
                .assertAll(hasClickAction())
        }
    }

    @Test
    fun bin_actions_test() {
        every {
            pagedPlainItemDataUseCase.invoke()
        } returns flowOf(
            PagingData.from(
                listOf(AppAndroidTestUtils.createPlainItemData()),
                sourceLoadStates = AppAndroidTestUtils.loadedPagingStates(),
            ),
        )
        coEvery { countSafeItemUseCase.deleted(any()) } returns LBResult.Success(1)

        setBinScreen(viewModel) {
            onRoot()
                .printToCacheDir(printRule)
            onNodeWithContentDescription(getString(OSString.bin_topBar_menu_accessibility_description))
                .performClick()
            isPopup()
                .waitAndPrintWholeScreenToCacheDir(printRule, suffix = "_menu")
                .assertIsDisplayed()

            onNodeWithText(getString(OSString.bin_topBar_menu_removeAll))
                .performClick()
            isDialog()
                .waitAndPrintWholeScreenToCacheDir(printRule, suffix = "_removeAll")
                .assertIsDisplayed()

            onNodeWithText(getString(OSString.common_cancel))
                .performClick()

            onNodeWithContentDescription(getString(OSString.bin_topBar_menu_accessibility_description))
                .performClick()
            onNodeWithText(getString(OSString.bin_topBar_menu_restoreAll))
                .performClick()
            onNodeWithText(getString(OSString.common_restore))
                .performClick()
        }

        verify(exactly = 1) { navigateBack.invoke() }
    }

    private fun setBinScreen(
        viewModel: BinViewModel,
        block: ComposeUiTest.() -> Unit,
    ) {
        invoke {
            setContent {
                with(AppAndroidTestUtils.composeItemActionNavScopeTest()) {
                    BinRoute(
                        navigateBack = this@BinScreenTest.navigateBack,
                        showSnackBar = {},
                        navigateToItemDetails = {},
                        viewModel = viewModel,
                    )
                }
            }
            block()
        }
    }
}
