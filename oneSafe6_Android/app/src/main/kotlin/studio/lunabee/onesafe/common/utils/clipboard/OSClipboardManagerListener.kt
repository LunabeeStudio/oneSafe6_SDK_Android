package studio.lunabee.onesafe.common.utils.clipboard

import android.content.ClipboardManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OSClipboardManagerListener(
    private val clipboardManager: ClipboardManager,
    private val clipboardFilter: OSClipboardFilter,
    initialValue: String?,
) : ClipboardManager.OnPrimaryClipChangedListener {
    private val _clipboardContent: MutableStateFlow<String?> = MutableStateFlow(initialValue)
    internal val clipboardContent: StateFlow<String?> = _clipboardContent.asStateFlow()

    override fun onPrimaryClipChanged() {
        val primaryClip = clipboardManager.primaryClip
        primaryClip?.getItemAt(0)?.text?.toString()?.let { clipText ->
            if (clipboardFilter.isValid(clipText)) {
                _clipboardContent.value = clipboardFilter.mapText(clipText)
            } else {
                _clipboardContent.value = null
            }
        }
    }
}
