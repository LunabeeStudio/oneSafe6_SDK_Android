package studio.lunabee.onesafe.common.extensions

import androidx.compose.ui.focus.FocusRequester
import co.touchlab.kermit.Logger
import com.lunabee.lblogger.e

// Used to avoid crash during test
fun FocusRequester.requestFocusSafely() {
    try {
        requestFocus()
    } catch (e: Exception) {
        Logger.e(e, "Error during focus request")
    }
}
