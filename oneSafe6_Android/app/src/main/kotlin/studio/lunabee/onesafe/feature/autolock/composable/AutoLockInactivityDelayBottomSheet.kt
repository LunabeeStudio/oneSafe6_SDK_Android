package studio.lunabee.onesafe.feature.autolock.composable

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.commonui.settings.AutoLockInactivityDelay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoLockInactivityDelayBottomSheet(
    isVisible: Boolean,
    onBottomSheetClosed: () -> Unit,
    onSelect: (delay: AutoLockInactivityDelay) -> Unit,
    selectedAutoLockInactivityDelay: AutoLockInactivityDelay,
) {
    BottomSheetHolder(
        isVisible = isVisible,
        onBottomSheetClosed = onBottomSheetClosed,
        skipPartiallyExpanded = true,
    ) { closeBottomSheet, paddingValues ->
        AutoLockInactivityDelayBottomSheetContent(
            paddingValues = paddingValues,
            onSelect = { clearDelay ->
                onSelect(clearDelay)
                closeBottomSheet()
            },
            selectedAutoLockInactivityDelay = selectedAutoLockInactivityDelay,
        )
    }
}
