package studio.lunabee.onesafe.feature.verifypassword

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.common.extensions.getLabel
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolderColumnContent
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.molecule.OSOptionRow
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyPasswordIntervalBottomSheet(
    isVisible: Boolean,
    onBottomSheetClosed: () -> Unit,
    selectedInterval: VerifyPasswordInterval,
    onSelectInterval: (VerifyPasswordInterval) -> Unit,
) {
    BottomSheetHolder(
        isVisible = isVisible,
        onBottomSheetClosed = onBottomSheetClosed,
        skipPartiallyExpanded = true,
    ) { closeBottomSheet, paddingValues ->
        VerifyPasswordIntervalBottomSheetContent(
            paddingValues = paddingValues,
            selectedInterval = selectedInterval,
            onSelectInterval = {
                onSelectInterval(it)
                closeBottomSheet()
            },
        )
    }
}

@Composable
private fun VerifyPasswordIntervalBottomSheetContent(
    paddingValues: PaddingValues,
    selectedInterval: VerifyPasswordInterval,
    onSelectInterval: (VerifyPasswordInterval) -> Unit,
    intervals: List<VerifyPasswordInterval> = VerifyPasswordInterval.entries.sortedBy { it.ordinal },
) {
    BottomSheetHolderColumnContent(
        paddingValues = paddingValues,
        modifier = Modifier
            .selectableGroup()
            .wrapContentHeight()
            .padding(vertical = OSDimens.SystemSpacing.Small)
            .testTag(UiConstants.TestTag.BottomSheet.VerifyPasswordBottomSheetInterval),
    ) {
        OSText(
            text = LbcTextSpec.StringResource(id = OSString.verifyPassword_bottomSheet_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.Small),
        )
        OSText(
            text = LbcTextSpec.StringResource(id = OSString.settings_security_section_verifyPassword_description),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.Small),
        )

        intervals.forEach { interval ->
            OSOptionRow(
                text = interval.getLabel(),
                onSelect = { onSelectInterval(interval) },
                isSelected = interval == selectedInterval,
            )
        }
    }
}

@Composable
@OsDefaultPreview
fun VerifyPasswordIntervalBottomSheetPreview() {
    OSPreviewBackgroundTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.surface)) {
            VerifyPasswordIntervalBottomSheetContent(
                paddingValues = PaddingValues(0.dp),
                selectedInterval = VerifyPasswordInterval.EVERY_MONTH,
                onSelectInterval = {},
            )
        }
    }
}
