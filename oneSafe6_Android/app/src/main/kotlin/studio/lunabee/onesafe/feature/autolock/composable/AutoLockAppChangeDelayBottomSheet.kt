package studio.lunabee.onesafe.feature.autolock.composable

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.commonui.settings.AutoLockBackgroundDelay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoLockAppChangeDelayBottomSheet(
    isVisible: Boolean,
    onBottomSheetClosed: () -> Unit,
    onSelect: (entry: AutoLockBackgroundDelay) -> Unit,
    selectedAutoLockAppChangeDelay: AutoLockBackgroundDelay,
) {
    BottomSheetHolder(
        isVisible = isVisible,
        onBottomSheetClosed = onBottomSheetClosed,
        skipPartiallyExpanded = true,
    ) { closeBottomSheet, paddingValues ->
        AutoLockAppChangeDelayBottomSheetContent(
            paddingValues = paddingValues,
            onSelect = { clearDelay ->
                onSelect(clearDelay)
                closeBottomSheet()
            },
            selectedAutoLockAppChangeDelay = selectedAutoLockAppChangeDelay,
        )
    }
}
