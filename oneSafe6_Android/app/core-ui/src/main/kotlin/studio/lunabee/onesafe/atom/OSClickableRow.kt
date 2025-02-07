package studio.lunabee.onesafe.atom

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.accessibility.accessibilityMergeDescendants
import studio.lunabee.onesafe.atom.button.defaults.OSTextButtonDefaults
import studio.lunabee.onesafe.atom.text.OSRowLabel
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.extension.drawableSample
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun OSClickableRow(
    text: LbcTextSpec,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: LbcTextSpec? = null,
    state: OSActionState = OSActionState.Enabled,
    buttonColors: ButtonColors = OSTextButtonDefaults.secondaryTextButtonColors(state = state),
    contentPadding: PaddingValues = LocalDesignSystem.current.rowClickablePaddingValues,
    maxTextWidth: Dp = Dp.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int? = 1,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    contentDescription: String? = null,
    trailingText: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    OSClickableRow(onClick, {
        OSClickableRowText(
            text,
            it,
            maxTextWidth,
            textAlign,
            maxLines,
            style,
            label = label,
            contentDescription = contentDescription,
            state = state,
        )
    }, modifier, state, buttonColors, contentPadding, trailingText, leadingIcon, trailingIcon)
}

@Composable
fun OSClickableRow(
    onClick: () -> Unit,
    label: @Composable (RowScope.(Modifier) -> Unit),
    modifier: Modifier = Modifier,
    state: OSActionState = OSActionState.Enabled,
    buttonColors: ButtonColors = OSTextButtonDefaults.secondaryTextButtonColors(state = state),
    contentPadding: PaddingValues = LocalDesignSystem.current.rowClickablePaddingValues,
    trailingText: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    TextButton(
        modifier = Modifier
            .fillMaxWidth()
            .then(other = modifier),
        contentPadding = contentPadding,
        shape = RoundedCornerShape(size = OSDimens.SystemCornerRadius.None),
        enabled = state.enabled,
        onClick = onClick,
        colors = buttonColors,
        elevation = null,
    ) {
        val colorScheme = if (state == OSActionState.Enabled) {
            MaterialTheme.colorScheme
        } else {
            MaterialTheme.colorScheme.copy(
                secondaryContainer = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = UiConstants.GoogleInternalApi.DisabledContainerAlpha,
                ),
            )
        }
        MaterialTheme(
            colorScheme = colorScheme,
        ) {
            leadingIcon?.let { LeadingIcon ->
                LeadingIcon()
                OSSmallSpacer()
            }

            label(
                Modifier
                    .weight(weight = 1f),
            )

            trailingText?.let { TrailingText ->
                OSSmallSpacer()
                TrailingText()
            }

            trailingIcon?.let { TrailingIcon ->
                OSSmallSpacer()
                TrailingIcon()
            }
        }
    }
}

@Composable
fun OSClickableRowText(
    text: LbcTextSpec,
    modifier: Modifier = Modifier,
    maxTextWidth: Dp = Dp.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int? = 1,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    color: Color = Color.Unspecified,
    label: LbcTextSpec? = null,
    contentDescription: String? = null,
    state: OSActionState = OSActionState.Enabled,
) {
    val textsModifier = if (contentDescription != null) {
        modifier.clearAndSetSemantics { this.text = AnnotatedString(contentDescription) }
    } else {
        modifier.accessibilityMergeDescendants()
    }

    fun Color.withState(): Color = if (state != OSActionState.Enabled) {
        this.copy(alpha = UiConstants.GoogleInternalApi.DisabledContentAlpha)
    } else {
        this
    }

    Column(
        modifier = textsModifier.height(IntrinsicSize.Min),
    ) {
        label?.let { label ->
            OSRowLabel(
                text = label,
                color = LocalDesignSystem.current.rowLabelColor.withState(),
            )
        }
        OSText(
            text = text,
            modifier = modifier
                .widthIn(max = maxTextWidth),
            textAlign = textAlign,
            overflow = TextOverflow.Ellipsis,
            maxLines = maxLines ?: Int.MAX_VALUE,
            style = style,
            color = color,
        )
    }
}

@OsDefaultPreview
@Composable
private fun OSTextButtonPreview() {
    OSPreviewBackgroundTheme {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            OSActionState.entries.forEach { actionState ->
                val buttonColors = listOf(
                    OSTextButtonDefaults.secondaryTextButtonColors(state = actionState),
                    OSTextButtonDefaults.secondaryAlertTextButtonColors(state = actionState),
                )
                OSText(text = LbcTextSpec.Raw("For state ${actionState.name}"))
                buttonColors.forEachIndexed { index, colors ->
                    OSClickableRow(
                        text = loremIpsumSpec(2),
                        onClick = { },
                        state = actionState,
                        buttonColors = colors,
                    )

                    OSClickableRow(
                        text = loremIpsumSpec(2),
                        label = loremIpsumSpec(3),
                        onClick = { },
                        state = actionState,
                        buttonColors = colors,
                    )

                    OSClickableRow(
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
                        trailingIcon = {
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
                        trailingText = {
                            OSText(text = LbcTextSpec.Raw("trailing"))
                        },
                    )

                    OSClickableRow(
                        text = loremIpsumSpec(2),
                        label = loremIpsumSpec(3),
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
                        trailingIcon = {
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
                        trailingText = {
                            OSText(text = LbcTextSpec.Raw("trailing"))
                        },
                    )
                }
            }
        }
    }
}
