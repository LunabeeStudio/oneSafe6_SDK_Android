package studio.lunabee.onesafe.atom.button.defaults

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import studio.lunabee.onesafe.atom.button.defaults.OSFilledButtonDefaults.buttonColors
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.UiConstants

/**
 * Provides defaults colors for a [androidx.compose.material3.Button].
 * This object should only contains methods that are reusable across the app.
 * If you have a specific case, either use directly [ButtonDefaults] or [buttonColors].
 */
object OSFilledButtonDefaults {
    @Composable
    fun primaryButtonColors(state: OSActionState = OSActionState.Enabled): ButtonColors =
        buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            state = state,
        )

    @Composable
    fun primaryAlertButtonColors(state: OSActionState = OSActionState.Enabled): ButtonColors =
        buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
            state = state,
        )

    @Composable
    fun secondaryButtonColors(state: OSActionState = OSActionState.Enabled): ButtonColors =
        buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            state = state,
        )

    @Composable
    fun buttonColors(
        containerColor: Color,
        contentColor: Color,
        state: OSActionState = OSActionState.Enabled,
    ): ButtonColors {
        return when (state) {
            OSActionState.Enabled,
            OSActionState.Disabled,
            -> ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor)
            OSActionState.DisabledWithAction -> ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = UiConstants.GoogleInternalApi.DisabledContainerAlpha),
                contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = UiConstants.GoogleInternalApi.DisabledContentAlpha),
            )
        }
    }
}
