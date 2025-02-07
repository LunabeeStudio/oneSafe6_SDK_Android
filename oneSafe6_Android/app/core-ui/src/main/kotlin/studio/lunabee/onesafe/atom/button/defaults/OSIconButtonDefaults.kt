package studio.lunabee.onesafe.atom.button.defaults

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults.iconButtonColors
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.UiConstants

/**
 * Provides defaults colors for a [studio.lunabee.onesafe.atom.button.OSIconButton].
 * We do not use directly [androidx.compose.material3.IconButtonDefaults] for this type of button as [androidx.compose.material3.IconButton]
 * size is limited by API. But for semantic, we keep the "iconButton" naming.
 * This object should only contains methods that are reusable across the app.
 * If you have a specific case, either use directly [ButtonDefaults] or [iconButtonColors].
 */
object OSIconButtonDefaults {
    @Composable
    fun primaryIconButtonColors(state: OSActionState = OSActionState.Enabled): ButtonColors =
        iconButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            state = state,
        )

    @Composable
    fun secondaryIconButtonColors(state: OSActionState = OSActionState.Enabled): ButtonColors =
        iconButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            state = state,
        )

    @Composable
    fun transparentIconButtonColors(state: OSActionState = OSActionState.Enabled): ButtonColors =
        iconButtonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            state = state,
        )

    @Composable
    fun tertiaryIconButtonColors(state: OSActionState = OSActionState.Enabled): ButtonColors =
        iconButtonColors(
            containerColor = MaterialTheme.colorScheme.onPrimary,
            contentColor = MaterialTheme.colorScheme.primary,
            state = state,
        )

    @Composable
    fun iconButtonColors(
        containerColor: Color,
        contentColor: Color,
        state: OSActionState = OSActionState.Enabled,
    ): ButtonColors {
        return when (state) {
            OSActionState.Enabled -> ButtonDefaults.buttonColors(
                contentColor = contentColor,
                containerColor = containerColor,
            )
            OSActionState.Disabled -> ButtonDefaults.buttonColors() // Let Compose handle it
            OSActionState.DisabledWithAction -> ButtonDefaults.buttonColors(
                contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = UiConstants.GoogleInternalApi.DisabledContentAlpha),
                containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = UiConstants.GoogleInternalApi.DisabledContainerAlpha),
            ) // map disabled colors to create a fake disabled state.
        }
    }
}
