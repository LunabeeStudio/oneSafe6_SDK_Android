package studio.lunabee.onesafe.common.utils

import android.content.Context
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import studio.lunabee.onesafe.common.utils.settings.AndroidSettings
import studio.lunabee.onesafe.ime.OneSafeKeyboardHelper

@Composable
fun observeIsOSKImeEnabledAsStateWithLifecycle(): State<Boolean> {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    val inputMethodManager: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val initialValue = OneSafeKeyboardHelper.parseIsOneSafeKeyboardEnabled(
        context,
        inputMethodManager.enabledInputMethodList.joinToString(OneSafeKeyboardHelper.Delimiter.toString()) { it.id },
    )
    // FIXME https://issuetracker.google.com/issues/349411310
    @Suppress("ProduceStateDoesNotAssignValue")
    return produceState(initialValue, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            this@produceState.value = OneSafeKeyboardHelper.parseIsOneSafeKeyboardEnabled(
                context,
                inputMethodManager.enabledInputMethodList.joinToString(OneSafeKeyboardHelper.Delimiter.toString()) { it.id },
            )
        }
    }
}

@Composable
fun observeIsOSKImeSelectedAsState(
    foregroundOnly: Boolean = false,
): State<Boolean> {
    val context = LocalContext.current
    return AndroidSettings.Secure.observeAsState(
        key = Settings.Secure.DEFAULT_INPUT_METHOD,
        foregroundOnly = foregroundOnly,
        transform = { OneSafeKeyboardHelper.parseIsOneSafeKeyboardSelected(context, it.toString()) },
    )
}
