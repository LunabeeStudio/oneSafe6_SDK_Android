package studio.lunabee.onesafe.feature.settings.bubbles.composable

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolderColumnContent
import studio.lunabee.onesafe.feature.settings.bubbles.model.BubblesResendMessageDelay
import studio.lunabee.onesafe.molecule.OSOptionRow
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
internal fun BubblesResendDelayBottomSheetContent(
    paddingValues: PaddingValues,
    onSelect: (entry: BubblesResendMessageDelay) -> Unit,
    selectedChangeDelay: BubblesResendMessageDelay,
) {
    BottomSheetHolderColumnContent(
        paddingValues = paddingValues,
        modifier = Modifier
            .selectableGroup()
            .wrapContentHeight()
            .padding(vertical = OSDimens.SystemSpacing.Small),
    ) {
        OSText(
            text = LbcTextSpec.StringResource(id = OSString.bubblesSettings_changeResendDelayScreen_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.Small),
        )
        OSText(
            text = LbcTextSpec.StringResource(id = OSString.bubblesSettings_changeResendDelayScreen_description),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.Small),
        )
        BubblesResendMessageDelay.entries.forEach { entry ->
            OSOptionRow(
                text = entry.text,
                onSelect = {
                    onSelect(entry)
                },
                isSelected = entry == selectedChangeDelay,
            )
        }
    }
}
