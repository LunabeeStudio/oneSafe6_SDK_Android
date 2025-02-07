package studio.lunabee.onesafe.atom.button.defaults

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import studio.lunabee.onesafe.atom.button.defaults.OSTextButtonDefaults.textButtonColors
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.UiConstants

/**
 * Provides defaults colors for a [androidx.compose.material3.TextButton].
 * This object should only contains methods that are reusable across the app.
 * If you have a specific case, either use directly [ButtonDefaults] or [textButtonColors].
 */
object OSTextButtonDefaults {
    @Composable
    fun primaryTextButtonColors(state: OSActionState): ButtonColors =
        textButtonColors(color = MaterialTheme.colorScheme.primary, state = state)

    @Composable
    fun secondaryTextButtonColors(state: OSActionState): ButtonColors =
        textButtonColors(color = MaterialTheme.colorScheme.secondary, state = state)

    @Composable
    fun secondaryAlertTextButtonColors(state: OSActionState): ButtonColors =
        textButtonColors(color = MaterialTheme.colorScheme.error, state = state)

    @Composable
    fun textButtonColors(
        color: Color,
        state: OSActionState = OSActionState.Enabled,
    ): ButtonColors {
        return when (state) {
            OSActionState.Enabled,
            OSActionState.Disabled,
            -> ButtonDefaults.textButtonColors(contentColor = color)
            OSActionState.DisabledWithAction -> ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = UiConstants.GoogleInternalApi.DisabledContentAlpha),
            )
        }
    }
}
