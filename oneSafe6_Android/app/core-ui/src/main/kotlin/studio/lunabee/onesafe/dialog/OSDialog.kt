package studio.lunabee.onesafe.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import studio.lunabee.onesafe.window.LocalOnTouchWindow
import studio.lunabee.onesafe.window.TouchInterceptorBox

@Composable
fun OSDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit,
) {
    val onTouch = LocalOnTouchWindow.current
    Dialog(
        onDismissRequest = {
            onTouch()
            onDismissRequest()
        },
        properties = properties,
    ) {
        TouchInterceptorBox {
            content()
        }
    }
}
