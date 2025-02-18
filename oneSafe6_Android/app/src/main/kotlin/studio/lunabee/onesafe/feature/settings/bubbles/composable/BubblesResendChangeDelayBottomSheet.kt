package studio.lunabee.onesafe.feature.settings.bubbles.composable

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.feature.settings.bubbles.model.BubblesResendMessageDelay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BubblesResendChangeDelayBottomSheet(
    isVisible: Boolean,
    onBottomSheetClosed: () -> Unit,
    onSelect: (entry: BubblesResendMessageDelay) -> Unit,
    selectedAutoLockAppChangeDelay: BubblesResendMessageDelay,
) {
    BottomSheetHolder(
        isVisible = isVisible,
        onBottomSheetClosed = onBottomSheetClosed,
        skipPartiallyExpanded = true,
    ) { closeBottomSheet, paddingValues ->
        BubblesResendDelayBottomSheetContent(
            paddingValues = paddingValues,
            onSelect = { clearDelay ->
                onSelect(clearDelay)
                closeBottomSheet()
            },
            selectedChangeDelay = selectedAutoLockAppChangeDelay,
        )
    }
}
