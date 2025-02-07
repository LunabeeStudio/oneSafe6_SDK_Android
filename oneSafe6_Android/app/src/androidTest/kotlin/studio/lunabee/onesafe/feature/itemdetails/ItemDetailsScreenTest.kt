package studio.lunabee.onesafe.feature.itemdetails

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowInsetsCompat
import androidx.paging.PagingData
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.printToCacheDir
import studio.lunabee.compose.androidtest.extension.waitAndPrintWholeScreenToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppAndroidTestUtils
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.ErrorNameProvider
import studio.lunabee.onesafe.commonui.OSPlurals
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.ErrorDialogState
import studio.lunabee.onesafe.commonui.item.ItemsLayout
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.extension.iconSample
import studio.lunabee.onesafe.extension.illustrationSample
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.feature.itemdetails.model.ItemDetailsDeletedCardData
import studio.lunabee.onesafe.feature.itemdetails.model.ItemDetailsScreenUiState
import studio.lunabee.onesafe.feature.itemdetails.model.ItemDetailsTab
import studio.lunabee.onesafe.feature.itemdetails.model.SafeItemAction
import studio.lunabee.onesafe.feature.itemdetails.model.informationtabentry.InformationTabEntryTextField
import studio.lunabee.onesafe.getLbcTextSpecResString
import studio.lunabee.onesafe.model.OSItemIllustration
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import java.util.UUID
import kotlin.reflect.full.primaryConstructor
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ItemDetailsScreenTest : ItemDetailsTest() {

    @Test
    fun secured_read_test() {
        val vm = mockItemDetailsViewModel
        every { vm.uiState } returns MutableStateFlow(
            ItemDetailsScreenUiState.Data.Default(
                itemNameProvider = DefaultNameProvider(loremIpsum(10)),
                icon = illustrationSample,
                tabs = linkedSetOf(*ItemDetailsTab.entries.toTypedArray()),
                informationTab = listOf(
                    InformationTabEntryTextField(
                        id = testUUIDs[0],
                        value = LbcTextSpec.Raw("none_value"),
                        label = LbcTextSpec.Raw("none_label"),
                        kind = SafeItemFieldKind.Text,
                        isSecured = false,
                        getDecryptedDisplayValue = { "none_value" },
                        getDecryptedRawValue = { "none_value" },
                    ),
                    InformationTabEntryTextField(
                        id = testUUIDs[1],
                        value = LbcTextSpec.Raw("show_value"),
                        label = LbcTextSpec.Raw("show_label"),
                        kind = SafeItemFieldKind.Text,
                        isSecured = false,
                        getDecryptedDisplayValue = { "show_value" },
                        getDecryptedRawValue = { "show_value" },
                    ),
                    InformationTabEntryTextField(
                        id = testUUIDs[2],
                        value = LbcTextSpec.Raw("hide_value"),
                        label = LbcTextSpec.Raw("hide_label"),
                        kind = SafeItemFieldKind.Password,
                        isSecured = true,
                        getDecryptedDisplayValue = { "value_visible" },
                        getDecryptedRawValue = { "value_visible" },
                    ),
                ),
                moreTab = emptyList(),
                children = flowOf(PagingData.empty()),
                childrenCount = 0,
                actions = listOf(),
                color = null,
                initialTab = ItemDetailsTab.Information,
                isCorrupted = false,
                notSupportedKindsList = null,
                shouldShowEditTips = false,
            ),
        )

        setItemDetailsScreen(viewModel = vm) {
            onRoot().printToCacheDir(printRule)
            onNodeWithText("none_value", useUnmergedTree = true).assertIsDisplayed()
            onNodeWithText("show_value", useUnmergedTree = true).assertIsDisplayed()
            onNodeWithText("hide_value", useUnmergedTree = true).assertIsDisplayed()
            onNodeWithTag(UiConstants.TestTag.Item.VisibilityAction).performClick()
            onNodeWithText("value_visible", useUnmergedTree = true).assertIsDisplayed()
        }
    }

    @Test
    fun topBar_visibility_test() {
        val vm = mockItemDetailsViewModel
        every { vm.uiState } returns MutableStateFlow(
            ItemDetailsScreenUiState.Data.Default(
                itemNameProvider = DefaultNameProvider(loremIpsum(10)),
                icon = illustrationSample,
                tabs = linkedSetOf(*ItemDetailsTab.entries.toTypedArray()),
                informationTab = (0..20).map { idx ->
                    val text = idx.toString()
                    InformationTabEntryTextField(
                        id = UUID.randomUUID(),
                        value = LbcTextSpec.Raw(text),
                        label = LbcTextSpec.Raw(text),
                        kind = SafeItemFieldKind.Text,
                        isSecured = false,
                        getDecryptedDisplayValue = { text },
                        getDecryptedRawValue = { text },
                    )
                },
                moreTab = emptyList(),
                children = flowOf(PagingData.empty()),
                childrenCount = 0,
                actions = listOf(),
                color = null,
                initialTab = ItemDetailsTab.Information,
                isCorrupted = false,
                notSupportedKindsList = null,
                shouldShowEditTips = false,
            ),
        )

        setItemDetailsScreen(viewModel = vm) { activity ->
            onRoot().printToCacheDir(printRule, "_before_scroll")

            val topBarNode = onNodeWithTag(UiConstants.TestTag.Item.ItemDetailsTopBar)

            var topBarScrollVisibilityThresholdPx: Float
            var topBarTop: Float
            with(Density(activity)) {
                topBarScrollVisibilityThresholdPx = density * OSDimens.ItemTopBar.TopBarScrollVisibilityThreshold.value
                topBarTop = topBarNode.getBoundsInRoot().top.toPx()
            }

            topBarNode.assertIsDisplayed()
            onRoot().performTouchInput {
                swipeUp(topBarTop + topBarScrollVisibilityThresholdPx, topBarTop)
            }
            onRoot().printToCacheDir(printRule, "_after_scroll")
            topBarNode.assertDoesNotExist()
            onNodeWithTag(UiConstants.TestTag.tab(1)).performClick()
            onRoot().printToCacheDir(printRule, "_after_tab_switch")
            topBarNode.assertIsDisplayed()
        }
    }

    // Only test Grid layout
    @Test
    fun children_layout_test() {
        val vm = mockItemDetailsViewModel
        every { vm.uiState } returns MutableStateFlow(
            ItemDetailsScreenUiState.Data.Default(
                itemNameProvider = DefaultNameProvider(""),
                icon = OSItemIllustration.Text(LbcTextSpec.Raw(""), null),
                tabs = linkedSetOf(ItemDetailsTab.Elements),
                informationTab = emptyList(),
                moreTab = emptyList(),
                children = flowOf(
                    PagingData.from(
                        AppAndroidTestUtils.createPlainItemData(
                            size = 50,
                            id = { UUID.randomUUID() },
                            itemNameProvider = { DefaultNameProvider(it.toString()) },
                            icon = { null },
                            color = { Color.Red },
                            actions = { null },
                            itemsLayout = ItemsLayout.entries.filterNot { it == ItemsLayout.List }.random(OSTestConfig.random),
                        ),
                    ),
                ),
                childrenCount = 49,
                actions = listOf(),
                color = null,
                initialTab = ItemDetailsTab.Elements,
                isCorrupted = false,
                notSupportedKindsList = null,
                shouldShowEditTips = false,
            ),
        )

        setItemDetailsScreen(viewModel = vm) { activity ->
            onRoot().printToCacheDir(printRule)

            val rowChildren = onAllNodesWithTag(UiConstants.TestTag.Item.ItemDetailsChildrenRow)
                .onFirst()
                .fetchSemanticsNode().children
            val childrenCoordinates = rowChildren.map { it.layoutInfo.coordinates }

            val insets = WindowInsetsCompat.toWindowInsetsCompat(activity.window.decorView.rootWindowInsets)
            val navBar = insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.navigationBars())
            val cutout = insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.displayCutout())

            val leftInset = cutout.left + navBar.left
            val rightInset = cutout.right + navBar.right

            val screenWidth = activity.window.decorView.width
            val regularSpacePx: Float
            val minItemSpacing: Float

            with(density) {
                regularSpacePx = OSDimens.SystemSpacing.Regular.toPx()
                minItemSpacing = OSDimens.AlternativeSpacing.ElementRowMinSpacing.toPx()
            }

            assertEquals(
                expected = leftInset + regularSpacePx * 2,
                actual = childrenCoordinates.first().positionInRoot().x,
                message = "First item position does not match design",
            )

            // Allow 1px diff per items due to spacing rounding
            val expectedLastPosition = screenWidth - rightInset - regularSpacePx * 2 - childrenCoordinates.last().size.width
            assertEquals(
                expectedLastPosition,
                childrenCoordinates.last().positionInRoot().x,
                childrenCoordinates.size.toFloat(),
            )

            val itemSpacing: Float = childrenCoordinates[1].positionInRoot().x -
                (childrenCoordinates[0].positionInRoot().x + childrenCoordinates[0].size.width)
            assert(itemSpacing >= minItemSpacing) { "Spacing between children does not respect min spacing from design" }

            childrenCoordinates.dropLast(1).forEachIndexed { idx, coordinates ->
                val nextChildPosition = childrenCoordinates[idx + 1].positionInRoot().x
                assertEquals(
                    expected = itemSpacing,
                    actual = nextChildPosition - coordinates.positionInRoot().x - coordinates.size.width,
                    message = "Spacing between children is not constant",
                )
            }
        }
    }

    @Test
    fun error_dialog_test() {
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
                notSupportedKindsList = null,
                shouldShowEditTips = false,
            ),
        )
        every { vm.itemActionDialogState } returns MutableStateFlow(
            ErrorDialogState(error = null, actions = emptyList(), dismiss = {}),
        )

        setItemDetailsScreen(viewModel = vm) {
            hasText(getString(OSString.error_defaultTitle))
                .waitAndPrintWholeScreenToCacheDir(printRule)
                .assertIsDisplayed()
        }
    }

    @Test
    fun show_actions_test() {
        val vm = mockItemDetailsViewModel
        val allActions = SafeItemAction::class.sealedSubclasses.map {
            it.primaryConstructor!!.call(spyk({}))
        }
        every { vm.uiState } returns MutableStateFlow(
            ItemDetailsScreenUiState.Data.Default(
                itemNameProvider = DefaultNameProvider(null),
                icon = OSItemIllustration.Image(OSImageSpec.Data(iconSample)),
                tabs = linkedSetOf(*ItemDetailsTab.entries.toTypedArray()),
                informationTab = emptyList(),
                moreTab = emptyList(),
                children = flowOf(PagingData.empty()),
                childrenCount = 0,
                actions = allActions,
                color = null,
                initialTab = ItemDetailsTab.Information,
                isCorrupted = false,
                notSupportedKindsList = null,
                shouldShowEditTips = false,
            ),
        )

        setItemDetailsScreen(viewModel = vm) { activity ->
            onRoot().printToCacheDir(printRule)

            onRoot().performTouchInput { swipeUp() }
            allActions.forEach { action ->
                val text = activity.getLbcTextSpecResString(action.text)
                onNodeWithText(text).performScrollTo().assertIsDisplayed().assertHasClickAction().performClick()
                verify(exactly = 1) {
                    action.onClick()
                }
            }
        }
    }

    @Test
    fun deleted_state_test() {
        val vm = mockItemDetailsViewModel
        val onClick = spyk({ })
        val messageRes = OSPlurals.safeItemDetail_deletedCard_message
        val actionRes = OSString.safeItemDetail_deletedCard_action
        val daysBeforeRemove = 123
        every { vm.uiState } returns MutableStateFlow(
            ItemDetailsScreenUiState.Data.Deleted(
                ItemDetailsScreenUiState.Data.Default(
                    itemNameProvider = DefaultNameProvider(null),
                    icon = OSItemIllustration.Text(LbcTextSpec.Raw(""), null),
                    tabs = linkedSetOf(*ItemDetailsTab.entries.toTypedArray()),
                    informationTab = emptyList(),
                    moreTab = emptyList(),
                    children = flowOf(PagingData.empty()),
                    childrenCount = 0,
                    actions = listOf(),
                    color = null,
                    initialTab = ItemDetailsTab.Information,
                    isCorrupted = false,
                    notSupportedKindsList = null,
                    shouldShowEditTips = false,
                ),
                deletedCardData = ItemDetailsDeletedCardData(
                    message = LbcTextSpec.PluralsResource(messageRes, daysBeforeRemove, daysBeforeRemove),
                    action = LbcTextSpec.StringResource(actionRes),
                    onClick = onClick,
                ),
            ),
        )

        setItemDetailsScreen(viewModel = vm) {
            onRoot().printToCacheDir(printRule)
            val messageText = getQuantityString(messageRes, daysBeforeRemove, daysBeforeRemove)
            onNodeWithText(messageText).assertIsDisplayed()
            onNodeWithText(getString(actionRes), useUnmergedTree = true).performClick()
            verify(exactly = 1) { onClick.invoke() }
        }
    }

    @Test
    fun corrupted_state_test() {
        val vm = mockItemDetailsViewModel
        val messageRes = OSString.safeItemDetail_corruptedCard_message
        every { vm.uiState } returns MutableStateFlow(
            ItemDetailsScreenUiState.Data.Default(
                itemNameProvider = ErrorNameProvider,
                icon = OSItemIllustration.Text(LbcTextSpec.Raw(""), null),
                tabs = linkedSetOf(*ItemDetailsTab.entries.toTypedArray()),
                informationTab = emptyList(),
                moreTab = emptyList(),
                children = flowOf(PagingData.empty()),
                childrenCount = 0,
                actions = listOf(),
                color = AppConstants.Ui.Item.ErrorColor,
                initialTab = ItemDetailsTab.Information,
                isCorrupted = true,
                notSupportedKindsList = null,
                shouldShowEditTips = false,
            ),
        )

        setItemDetailsScreen(viewModel = vm) {
            onRoot().printToCacheDir(printRule)
            onNodeWithText(getString(messageRes)).assertIsDisplayed()
        }
    }

    @Test
    fun not_supported_fields_test() {
        val vm = mockItemDetailsViewModel
        every { vm.uiState } returns MutableStateFlow(
            ItemDetailsScreenUiState.Data.Default(
                itemNameProvider = DefaultNameProvider(null),
                icon = OSItemIllustration.Text(LbcTextSpec.Raw(""), null),
                tabs = linkedSetOf(*ItemDetailsTab.entries.toTypedArray()),
                informationTab = emptyList(),
                moreTab = emptyList(),
                children = flowOf(PagingData.empty()),
                childrenCount = 0,
                actions = listOf(),
                color = AppConstants.Ui.Item.ErrorColor,
                initialTab = ItemDetailsTab.Information,
                isCorrupted = false,
                notSupportedKindsList = listOf(SafeItemFieldKind.Unknown("file")),
                shouldShowEditTips = false,
            ),
        )

        setItemDetailsScreen(viewModel = vm) {
            onRoot().printToCacheDir(printRule)
            onNodeWithText(getString(OSString.safeItemDetail_notSupportedFields_title)).assertIsDisplayed()
        }
    }

    @Test
    fun tooltip_displayed_and_dismissed_test() {
        val vm = mockItemDetailsViewModel
        every { vm.uiState } returns MutableStateFlow(
            ItemDetailsScreenUiState.Data.Default(
                itemNameProvider = DefaultNameProvider(null),
                icon = OSItemIllustration.Text(LbcTextSpec.Raw(""), null),
                tabs = linkedSetOf(*ItemDetailsTab.entries.toTypedArray()),
                informationTab = emptyList(),
                moreTab = emptyList(),
                children = flowOf(PagingData.empty()),
                childrenCount = 0,
                actions = listOf(),
                color = AppConstants.Ui.Item.ErrorColor,
                initialTab = ItemDetailsTab.Information,
                isCorrupted = false,
                notSupportedKindsList = listOf(SafeItemFieldKind.Unknown("file")),
                shouldShowEditTips = true,
            ),
        )

        setItemDetailsScreen(viewModel = vm) {
            onRoot().printToCacheDir(printRule)
            hasText(getString(OSString.safeItemDetail_tips_edit))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            onNodeWithText(getString(OSString.common_tips_gotIt)).assertIsDisplayed().performClick()
            hasText(getString(OSString.safeItemDetail_tips_edit))
                .waitUntilDoesNotExist()
                .assertDoesNotExist()
        }
    }

    @Test
    fun tooltip_not_displayed_test() {
        val vm = mockItemDetailsViewModel
        every { vm.uiState } returns MutableStateFlow(
            ItemDetailsScreenUiState.Data.Default(
                itemNameProvider = DefaultNameProvider(null),
                icon = OSItemIllustration.Text(LbcTextSpec.Raw(""), null),
                tabs = linkedSetOf(*ItemDetailsTab.entries.toTypedArray()),
                informationTab = emptyList(),
                moreTab = emptyList(),
                children = flowOf(PagingData.empty()),
                childrenCount = 0,
                actions = listOf(),
                color = AppConstants.Ui.Item.ErrorColor,
                initialTab = ItemDetailsTab.Information,
                isCorrupted = false,
                notSupportedKindsList = listOf(SafeItemFieldKind.Unknown("file")),
                shouldShowEditTips = true,
            ),
        )

        setItemDetailsScreen(viewModel = vm) {
            onRoot().printToCacheDir(printRule)
            onNodeWithText(getString(OSString.safeItemDetail_tips_edit)).assertDoesNotExist()
        }
    }
}
