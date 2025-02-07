package studio.lunabee.onesafe.atom.button

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSIconAlertDecorationButton
import studio.lunabee.onesafe.atom.OSIconDecorationButton
import studio.lunabee.onesafe.atom.OSSmallSpacer
import studio.lunabee.onesafe.atom.button.defaults.OSTextButtonDefaults
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.extension.drawableSample
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

/**
 * Common [TextButton] to use in the whole application.
 * By default, style will be align on a Secondary type (common case).
 * This button will never have a background (i.e containerColor will always be [androidx.compose.ui.graphics.Color.Transparent])
 *
 * @see [ButtonDefaults.textButtonColors] for more details.
 */
@Composable
fun OSTextButton(
    text: LbcTextSpec,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: OSActionState = OSActionState.Enabled,
    buttonColors: ButtonColors = OSTextButtonDefaults.secondaryTextButtonColors(state = state),
    maxLines: Int? = 1,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    TextButton(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        enabled = state.enabled,
        onClick = onClick,
        colors = buttonColors,
        elevation = null,
    ) {
        leadingIcon?.let { LeadingIcon ->
            LeadingIcon()
            OSSmallSpacer()
        }

        OSText(
            text = text,
            overflow = TextOverflow.Ellipsis,
            maxLines = maxLines ?: Int.MAX_VALUE,
            style = style,
        )
    }
}

@OsDefaultPreview
@Composable
private fun OSTextButtonPreview() {
    OSPreviewBackgroundTheme {
        Column {
            OSActionState.entries.forEach { actionState ->
                val buttonColors = listOf(
                    OSTextButtonDefaults.secondaryTextButtonColors(state = actionState),
                    OSTextButtonDefaults.secondaryAlertTextButtonColors(state = actionState),
                )
                OSText(text = LbcTextSpec.Raw("For state ${actionState.name}"))
                buttonColors.forEachIndexed { index, colors ->
                    OSTextButton(
                        text = loremIpsumSpec(2),
                        onClick = { },
                        state = actionState,
                        buttonColors = colors,
                    )

                    OSTextButton(
                        text = loremIpsumSpec(2),
                        onClick = { },
                        state = actionState,
                        buttonColors = colors,
                        leadingIcon = {
                            if (index == 0) {
                                OSIconDecorationButton(
                                    image = drawableSample,
                                )
                            } else {
                                OSIconAlertDecorationButton(
                                    image = drawableSample,
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}
