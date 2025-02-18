package studio.lunabee.onesafe.feature.clipboard.composable

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.feature.clipboard.model.ClipboardClearDelay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipboardClearDelayBottomSheet(
    isVisible: Boolean,
    onBottomSheetClosed: () -> Unit,
    onSelect: (entry: ClipboardClearDelay) -> Unit,
    selectedClipboardClearDelay: ClipboardClearDelay,
) {
    BottomSheetHolder(
        isVisible = isVisible,
        onBottomSheetClosed = onBottomSheetClosed,
        skipPartiallyExpanded = true,
    ) { _, paddingValues ->
        ClipboardClearDelayBottomSheetContent(
            paddingValues = paddingValues,
            onSelect = onSelect,
            selectedClipboardClearDelay = selectedClipboardClearDelay,
        )
    }
}
