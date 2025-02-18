package studio.lunabee.onesafe.common.utils.clipboard

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.flow.StateFlow

class OSClipboardManager(
    context: Context,
    osClipboardFilter: OSClipboardFilter,
    initialValue: String?,
) {
    private val clipboardManager: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private val osClipboardManagerListener: OSClipboardManagerListener = OSClipboardManagerListener(
        clipboardManager = clipboardManager,
        clipboardFilter = osClipboardFilter,
        initialValue = initialValue,
    )
    val clipboardContent: StateFlow<String?> = osClipboardManagerListener.clipboardContent

    @Composable
    fun HandleClipboardContent() {
        val view = LocalView.current

        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
            view.post {
                // clipboard is accessible only if view is rendered and focused.
                osClipboardManagerListener.onPrimaryClipChanged()
            }
        }

        DisposableEffect(clipboardManager) {
            clipboardManager.addPrimaryClipChangedListener(osClipboardManagerListener)
            onDispose { clipboardManager.removePrimaryClipChangedListener(osClipboardManagerListener) }
        }
    }
}

@Composable
fun rememberOSClipboardManager(
    osClipboardFilter: OSClipboardFilter = OSClipboardFilter.None,
    context: Context = LocalContext.current,
    initialValue: String? = LocalClipboardManager.current.getText()?.toString()?.takeIf { osClipboardFilter.isValid(it) },
): OSClipboardManager {
    return remember {
        OSClipboardManager(
            context = context,
            osClipboardFilter = osClipboardFilter,
            initialValue = initialValue,
        )
    }
}
