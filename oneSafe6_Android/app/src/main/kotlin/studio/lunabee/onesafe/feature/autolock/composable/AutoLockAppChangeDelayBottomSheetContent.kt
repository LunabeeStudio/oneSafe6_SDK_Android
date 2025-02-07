package studio.lunabee.onesafe.feature.autolock.composable

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
import studio.lunabee.onesafe.commonui.settings.AutoLockBackgroundDelay
import studio.lunabee.onesafe.molecule.OSOptionRow
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
internal fun AutoLockAppChangeDelayBottomSheetContent(
    paddingValues: PaddingValues,
    onSelect: (entry: AutoLockBackgroundDelay) -> Unit,
    selectedAutoLockAppChangeDelay: AutoLockBackgroundDelay,
) {
    BottomSheetHolderColumnContent(
        paddingValues = paddingValues,
        modifier = Modifier
            .selectableGroup()
            .wrapContentHeight()
            .padding(vertical = OSDimens.SystemSpacing.Small),
    ) {
        OSText(
            text = LbcTextSpec.StringResource(id = OSString.byChangingAppAutolockScreen_header_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.Small),
        )
        OSText(
            text = LbcTextSpec.StringResource(id = OSString.byChangingAppAutolockScreen_footer_title),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.Small),
        )
        AutoLockBackgroundDelay.entries.forEach { entry ->
            OSOptionRow(
                text = entry.text,
                onSelect = {
                    onSelect(entry)
                },
                isSelected = entry == selectedAutoLockAppChangeDelay,
            )
        }
    }
}

@OsDefaultPreview
@Composable
private fun AutoLockAppChangeDelayBottomSheetContentPreview() {
    OSPreviewOnSurfaceTheme {
        AutoLockAppChangeDelayBottomSheetContent(
            paddingValues = PaddingValues(0.dp),
            onSelect = {},
            selectedAutoLockAppChangeDelay = AutoLockBackgroundDelay.IMMEDIATELY,
        )
    }
}
