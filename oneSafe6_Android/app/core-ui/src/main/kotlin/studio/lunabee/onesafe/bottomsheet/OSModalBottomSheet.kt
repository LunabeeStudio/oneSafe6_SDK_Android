package studio.lunabee.onesafe.bottomsheet

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import studio.lunabee.onesafe.window.LocalOnTouchWindow
import studio.lunabee.onesafe.window.TouchInterceptorBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OSModalBottomSheet(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val onTouch = LocalOnTouchWindow.current
    ModalBottomSheet(
        onDismissRequest = {
            onTouch()
            onDismissRequest()
        },
        sheetState = sheetState,
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        TouchInterceptorBox {
            content()
        }
    }
}
