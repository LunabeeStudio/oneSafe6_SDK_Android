package studio.lunabee.onesafe.atom.button

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.compose.foundation.haptic.LbcHapticFeedback
import studio.lunabee.compose.foundation.haptic.rememberLbcHapticFeedback
import studio.lunabee.onesafe.atom.OSImage
import studio.lunabee.onesafe.atom.OSSmallSpacer
import studio.lunabee.onesafe.atom.button.defaults.OSFilledButtonDefaults
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.extension.drawableSample
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.model.OSHapticEffect
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun OSFilledButton(
    text: LbcTextSpec,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: OSActionState = OSActionState.Enabled,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    buttonColors: ButtonColors = OSFilledButtonDefaults.primaryButtonColors(state = state),
    shape: CornerBasedShape = MaterialTheme.shapes.medium,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    hapticEffect: OSHapticEffect? = OSHapticEffect.Primary.takeIf { buttonColors.containerColor == MaterialTheme.colorScheme.primary },
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val hapticFeedback: LbcHapticFeedback = rememberLbcHapticFeedback()
    Button(
        modifier = modifier,
        contentPadding = contentPadding,
        shape = shape,
        enabled = state.enabled,
        onClick = {
            hapticEffect?.perform(hapticFeedback)
            onClick()
        },
        colors = buttonColors,
        elevation = null,
    ) {
        leadingIcon?.let {
            leadingIcon()
            OSSmallSpacer()
        }

        OSText(
            text = text,
            style = style,
        )
    }
}

@OsDefaultPreview
@Composable
private fun OSPrimaryButtonPreview() {
    OSPreviewBackgroundTheme {
        Column {
            OSActionState.entries.forEach { actionState ->
                val buttonColors = listOf(
                    OSFilledButtonDefaults.primaryButtonColors(state = actionState),
                    OSFilledButtonDefaults.secondaryButtonColors(state = actionState),
                    OSFilledButtonDefaults.primaryAlertButtonColors(state = actionState),
                )
                OSText(text = LbcTextSpec.Raw("For state ${actionState.name}"))
                buttonColors.forEach { colors ->
                    OSFilledButton(
                        text = loremIpsumSpec(2),
                        onClick = { },
                        state = actionState,
                        buttonColors = colors,
                    )

                    OSFilledButton(
                        text = loremIpsumSpec(2),
                        onClick = { },
                        state = actionState,
                        buttonColors = colors,
                        leadingIcon = {
                            OSImage(image = drawableSample)
                        },
                    )
                }
            }
        }
    }
}
