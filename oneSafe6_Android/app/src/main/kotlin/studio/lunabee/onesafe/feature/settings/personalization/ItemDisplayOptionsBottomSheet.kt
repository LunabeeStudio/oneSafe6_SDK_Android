package studio.lunabee.onesafe.feature.settings.personalization

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImage
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolderColumnContent
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import studio.lunabee.onesafe.molecule.OSOptionRow
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Stable
class ItemDisplayOptionsBottomSheet(
    private val onSelectItemOrder: (ItemOrder) -> Unit,
    private val selectedItemOrder: ItemOrder,
    private val onSelectItemLayout: (ItemLayout) -> Unit,
    private val selectedItemLayout: ItemLayout,
) {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Composable(
        isVisible: Boolean,
        onBottomSheetClosed: () -> Unit,
    ) {
        BottomSheetHolder(
            isVisible = isVisible,
            onBottomSheetClosed = onBottomSheetClosed,
            skipPartiallyExpanded = true,
        ) { closeBottomSheet, paddingValues ->
            ItemDisplayOptionsBottomSheetContent(
                paddingValues = paddingValues,
                closeBottomSheet = closeBottomSheet,
            )
        }
    }

    @Composable
    private fun ItemDisplayOptionsBottomSheetContent(paddingValues: PaddingValues, closeBottomSheet: () -> Unit) {
        BottomSheetHolderColumnContent(
            paddingValues = paddingValues,
            modifier = Modifier
                .testTag(UiConstants.TestTag.BottomSheet.ItemDisplayOptionsBottomSheet)
                .wrapContentHeight()
                .selectableGroup()
                .padding(vertical = OSDimens.SystemSpacing.Small),
        ) {
            OSText(
                text = LbcTextSpec.StringResource(id = OSString.home_displayOptions_sorting_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.Small),
            )
            ItemOrder.entries.forEach { entry ->
                entry.text?.let { textSpec ->
                    OSOptionRow(
                        text = textSpec,
                        onSelect = {
                            onSelectItemOrder(entry)
                            closeBottomSheet()
                        },
                        isSelected = entry == selectedItemOrder,
                    )
                }
            }
            OSRegularSpacer()
            OSText(
                text = LbcTextSpec.StringResource(id = OSString.home_displayOptions_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.Small),
            )
            ItemLayout.entries.forEach { entry ->
                OSOptionRow(
                    text = entry.text,
                    onSelect = {
                        onSelectItemLayout(entry)
                        closeBottomSheet()
                    },
                    isSelected = entry == selectedItemLayout,
                    leadingIcon = { OSImage(image = entry.icon) },
                )
            }
        }
    }

    private val ItemOrder.text: LbcTextSpec?
        get() = when (this) {
            ItemOrder.Position -> LbcTextSpec.StringResource(OSString.home_displayOptions_sorting_default)
            ItemOrder.Alphabetic -> LbcTextSpec.StringResource(OSString.home_displayOptions_sorting_byTitle)
            ItemOrder.ConsultedAt -> LbcTextSpec.StringResource(OSString.home_displayOptions_sorting_byConsultationDate)
            ItemOrder.CreatedAt -> LbcTextSpec.StringResource(OSString.home_displayOptions_sorting_byDateAdded)
            ItemOrder.UpdatedAt,
            ItemOrder.DeletedAt,
            -> null
        }

    private val ItemLayout.text: LbcTextSpec
        get() = when (this) {
            ItemLayout.Grid -> LbcTextSpec.StringResource(OSString.home_displayOptions_grid)
            ItemLayout.LargeGrid -> LbcTextSpec.StringResource(OSString.home_displayOptions_largeGrid)
            ItemLayout.List -> LbcTextSpec.StringResource(OSString.home_displayOptions_list)
        }

    private val ItemLayout.icon: OSImageSpec
        get() = when (this) {
            ItemLayout.Grid -> OSImageSpec.Drawable(OSDrawable.ic_grid)
            ItemLayout.LargeGrid -> OSImageSpec.Drawable(OSDrawable.ic_grid_large)
            ItemLayout.List -> OSImageSpec.Drawable(OSDrawable.ic_list)
        }
}

@OsDefaultPreview
@Composable
private fun ItemDisplayOptionsBottomSheetPreview() {
    OSPreviewOnSurfaceTheme {
        ItemDisplayOptionsBottomSheet(
            {},
            ItemOrder.Alphabetic,
            {},
            ItemLayout.Grid,
        ).Composable(
            isVisible = true,
            onBottomSheetClosed = {},
        )
    }
}
