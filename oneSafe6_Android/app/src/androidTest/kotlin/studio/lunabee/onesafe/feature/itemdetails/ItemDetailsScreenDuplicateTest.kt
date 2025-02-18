package studio.lunabee.onesafe.feature.itemdetails

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.printToCacheDir
import studio.lunabee.compose.androidtest.extension.waitAndPrintWholeScreenToCacheDir
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.extension.illustrationSample
import studio.lunabee.onesafe.feature.dialog.DuplicateItemDialogState
import studio.lunabee.onesafe.feature.itemdetails.model.ItemDetailsScreenUiState
import studio.lunabee.onesafe.feature.itemdetails.model.ItemDetailsTab
import studio.lunabee.onesafe.feature.snackbar.DuplicateSucceedSnackbarState
import studio.lunabee.onesafe.test.testUUIDs

@OptIn(ExperimentalTestApi::class)
class ItemDetailsScreenDuplicateTest : ItemDetailsTest() {
    @Test
    fun duplicate_root_item_dialog() {
        val vm = mockItemDetailsViewModel
        val itemName = "item"
        every { vm.uiState } returns MutableStateFlow(
            ItemDetailsScreenUiState.Data.Default(
                itemNameProvider = DefaultNameProvider(itemName),
                icon = illustrationSample,
                tabs = linkedSetOf(*ItemDetailsTab.entries.toTypedArray()),
                informationTab = emptyList(),
                moreTab = emptyList(),
                children = flowOf(PagingData.empty()),
                childrenCount = 0,
                actions = listOf(),
                color = null,
                initialTab = ItemDetailsTab.Information,
                isCorrupted = false,
                notSupportedKindsList = listOf(),
                shouldShowEditTips = false,
            ),
        )

        val action = spyk({ })
        every { vm.itemActionDialogState } returns MutableStateFlow(
            DuplicateItemDialogState(
                itemName = DefaultNameProvider(itemName),
                parentName = null,
                duplicate = action,
                dismiss = {},
            ),
        )

        setItemDetailsScreen(viewModel = vm) {
            val duplicateDialogText = getString(OSString.safeItemDetail_duplicateDialog_rootMessage, itemName)
            val duplicateDialogButtonText = getString(OSString.safeItemDetail_duplicateDialog_ok)
            isDialog()
                .waitAndPrintWholeScreenToCacheDir(printRule)
                .assertIsDisplayed()
            onNodeWithText(duplicateDialogText).assertIsDisplayed()
            onNodeWithText(duplicateDialogButtonText).assertIsDisplayed()
            onNodeWithText(duplicateDialogButtonText).performClick()
        }

        verify {
            action()
        }
    }

    @Test
    fun duplicate_child_item_dialog() {
        val vm = mockItemDetailsViewModel
        val childName = "child"
        val parentName = "parent"

        every { vm.uiState } returns MutableStateFlow(
            ItemDetailsScreenUiState.Data.Default(
                itemNameProvider = DefaultNameProvider(childName),
                icon = illustrationSample,
                tabs = linkedSetOf(*ItemDetailsTab.entries.toTypedArray()),
                informationTab = emptyList(),
                moreTab = emptyList(),
                children = flowOf(PagingData.empty()),
                childrenCount = 0,
                actions = listOf(),
                color = null,
                initialTab = ItemDetailsTab.Information,
                isCorrupted = false,
                notSupportedKindsList = listOf(),
                shouldShowEditTips = false,
            ),
        )
        val action = spyk({ })
        every { vm.itemActionDialogState } returns MutableStateFlow(
            DuplicateItemDialogState(
                itemName = DefaultNameProvider(childName),
                parentName = DefaultNameProvider(parentName),
                duplicate = action,
                dismiss = {},
            ),
        )

        setItemDetailsScreen(viewModel = vm) {
            val duplicateDialogText = getString(
                OSString.safeItemDetail_duplicateDialog_childrenMessage,
                childName,
                parentName,
            )
            val duplicateDialogButtonText = getString(OSString.safeItemDetail_duplicateDialog_ok)

            isDialog()
                .waitAndPrintWholeScreenToCacheDir(printRule)
                .assertIsDisplayed()
            onNodeWithText(duplicateDialogText).assertIsDisplayed()
            onNodeWithText(duplicateDialogButtonText).assertIsDisplayed()
            onNodeWithText(duplicateDialogButtonText).performClick()
        }

        verify {
            action()
        }
    }

    @Test
    fun duplicate_item_feedback() {
        val vm = mockItemDetailsViewModel
        every { vm.uiState } returns MutableStateFlow(
            ItemDetailsScreenUiState.Data.Default(
                itemNameProvider = DefaultNameProvider(null),
                icon = illustrationSample,
                tabs = linkedSetOf(*ItemDetailsTab.entries.toTypedArray()),
                informationTab = emptyList(),
                moreTab = emptyList(),
                children = flowOf(PagingData.empty()),
                childrenCount = 0,
                actions = listOf(),
                color = null,
                initialTab = ItemDetailsTab.Information,
                isCorrupted = false,
                notSupportedKindsList = listOf(),
                shouldShowEditTips = false,
            ),
        )
        every { vm.snackbarState } returns MutableStateFlow(DuplicateSucceedSnackbarState(testUUIDs[0]) {})

        setItemDetailsScreen(viewModel = vm) {
            onRoot().printToCacheDir(printRule)
            val duplicateFeedbackText = getString(OSString.safeItemDetail_duplicateFeedback_successMessage)
            val duplicateFeedbackActionText = getString(OSString.safeItemDetail_duplicateFeedback_successAction)
            onNodeWithText(duplicateFeedbackText).assertIsDisplayed()
            onNodeWithText(duplicateFeedbackActionText).assertIsDisplayed()
        }
    }
}
