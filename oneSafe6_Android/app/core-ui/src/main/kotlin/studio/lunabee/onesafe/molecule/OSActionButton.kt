package studio.lunabee.onesafe.molecule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.accessibility.accessibilityClick
import studio.lunabee.onesafe.atom.OSClickableRow
import studio.lunabee.onesafe.atom.OSClickableRowText
import studio.lunabee.onesafe.atom.OSIconAlertDecorationButton
import studio.lunabee.onesafe.atom.OSIconDecorationButton
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSInputChip
import studio.lunabee.onesafe.atom.button.defaults.OSTextButtonDefaults
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.extension.drawableSample
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

enum class OSActionButtonStyle {
    Default, Destructive
}

/**
 * https://www.figma.com/file/4BXpDBVU9mT11uquBLvvnj/%F0%9F%A6%AF-OneSafe-Design-System?node-id=172%3A8007&mode=dev
 */
open class OSActionButton(
    private val text: LbcTextSpec,
    private val onClick: () -> Unit,
    private val contentPadding: PaddingValues,
    private val style: OSActionButtonStyle = OSActionButtonStyle.Default,
    private val state: OSActionState = OSActionState.Enabled,
    private val startIcon: OSImageSpec? = null,
    private val chip: @Composable (() -> Unit)? = null,
    private val contentDescription: LbcTextSpec? = null,
    private val clickLabel: LbcTextSpec? = null,
) {
    @Composable
    fun Composable(modifier: Modifier = Modifier) {
        Box(
            modifier = Modifier
                .height(IntrinsicSize.Max)
                .fillMaxWidth()
                .then(modifier),
        ) {
            OSClickableRow(
                modifier = Modifier
                    .clearAndSetSemantics { },
                onClick = {
                    // handled by sibling box
                },
                state = state,
                label = {
                    Row(
                        modifier = it,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Small),
                    ) {
                        OSClickableRowText(text = text, modifier = Modifier.weight(1f))
                        chip?.invoke()
                    }
                },
                contentPadding = contentPadding,
                buttonColors = when (style) {
                    OSActionButtonStyle.Default -> OSTextButtonDefaults.secondaryTextButtonColors(state = state)
                    OSActionButtonStyle.Destructive -> OSTextButtonDefaults.secondaryAlertTextButtonColors(state = state)
                },
                leadingIcon = startIcon?.let {
                    {
                        when (style) {
                            OSActionButtonStyle.Default -> OSIconDecorationButton(image = startIcon)
                            OSActionButtonStyle.Destructive -> OSIconAlertDecorationButton(image = startIcon)
                        }
                    }
                },
            )
            // Use sibling box to handle interaction so the whole row is clickable, including the chip
            // https://issuetracker.google.com/issues/289087869#comment7
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onClick() }
                    .composed {
                        val templateText = buildString {
                            append(text.string)
                            contentDescription?.let {
                                append(".${it.string}")
                            }
                        }
                        val clickLabel: String? = clickLabel?.string
                        semantics {
                            this.text = AnnotatedString(templateText)
                            this.role = Role.Button
                            if (!state.enabled) {
                                this.disabled()
                            }
                            accessibilityClick(label = clickLabel, action = onClick)
                        }
                    },
            )
        }
    }
}

@OsDefaultPreview
@Composable
private fun OSActionButtonPreview() {
    OSPreviewOnSurfaceTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            OSActionButtonStyle.entries.forEach { style ->
                OSActionState.entries.forEach { state ->
                    OSActionButton(
                        text = LbcTextSpec.Raw(style.name + "|" + state.name),
                        onClick = {},
                        contentPadding = PaddingValues(0.dp),
                        style = style,
                        state = state,
                        startIcon = drawableSample,
                        chip = {
                            OSInputChip(selected = true, onClick = { }, label = {
                                OSText(
                                    text = loremIpsumSpec(1),
                                )
                            })
                        },
                    ).Composable()
                }
            }
        }
    }
}
