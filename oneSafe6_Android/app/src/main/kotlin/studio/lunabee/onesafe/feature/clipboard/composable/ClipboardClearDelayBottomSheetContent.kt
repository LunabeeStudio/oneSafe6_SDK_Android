package studio.lunabee.onesafe.feature.clipboard.composable

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolderColumnContent
import studio.lunabee.onesafe.feature.clipboard.model.ClipboardClearDelay
import studio.lunabee.onesafe.molecule.OSOptionRow
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun ClipboardClearDelayBottomSheetContent(
    paddingValues: PaddingValues,
    onSelect: (entry: ClipboardClearDelay) -> Unit,
    selectedClipboardClearDelay: ClipboardClearDelay,
) {
    BottomSheetHolderColumnContent(
        paddingValues = paddingValues,
        modifier = Modifier
            .selectableGroup()
            .wrapContentHeight()
            .padding(vertical = OSDimens.SystemSpacing.Small),
    ) {
        OSText(
            text = LbcTextSpec.StringResource(id = OSString.settings_security_section_clipboard_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.Small),
        )
        OSText(
            text = LbcTextSpec.StringResource(id = OSString.settings_security_section_clipboard_information),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.Small),
        )
        ClipboardClearDelay.filteredValues().forEach { entry ->
            OSOptionRow(
                text = entry.text,
                onSelect = {
                    onSelect(entry)
                },
                isSelected = entry == selectedClipboardClearDelay,
            )
        }
    }
}

@OsDefaultPreview
@Composable
private fun ClipboardClearDelayBottomSheetContentPreview() {
    OSPreviewOnSurfaceTheme {
        ClipboardClearDelayBottomSheetContent(
            paddingValues = PaddingValues(0.dp),
            onSelect = {},
            selectedClipboardClearDelay = ClipboardClearDelay.THIRTY_SECONDS,
        )
    }
}
