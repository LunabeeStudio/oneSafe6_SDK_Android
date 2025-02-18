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
import studio.lunabee.onesafe.commonui.settings.AutoLockInactivityDelay
import studio.lunabee.onesafe.molecule.OSOptionRow
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
internal fun AutoLockInactivityDelayBottomSheetContent(
    paddingValues: PaddingValues,
    onSelect: (entry: AutoLockInactivityDelay) -> Unit,
    selectedAutoLockInactivityDelay: AutoLockInactivityDelay,
) {
    BottomSheetHolderColumnContent(
        paddingValues = paddingValues,
        modifier = Modifier
            .selectableGroup()
            .wrapContentHeight(),
    ) {
        OSText(
            text = LbcTextSpec.StringResource(id = OSString.inactivityAutolockScreen_header_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(all = OSDimens.SystemSpacing.Regular),
        )
        OSText(
            text = LbcTextSpec.StringResource(id = OSString.inactivityAutolockScreen_footer_title),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(all = OSDimens.SystemSpacing.Regular),
        )
        AutoLockInactivityDelay.entries.forEach { entry ->
            OSOptionRow(
                text = entry.text,
                onSelect = {
                    onSelect(entry)
                },
                isSelected = entry == selectedAutoLockInactivityDelay,
            )
        }
    }
}

@OsDefaultPreview
@Composable
private fun AutoLockInactivityDelayBottomSheetContentPreview() {
    OSPreviewOnSurfaceTheme {
        AutoLockInactivityDelayBottomSheetContent(
            paddingValues = PaddingValues(0.dp),
            onSelect = {},
            selectedAutoLockInactivityDelay = AutoLockInactivityDelay.THIRTY_SECONDS,
        )
    }
}
