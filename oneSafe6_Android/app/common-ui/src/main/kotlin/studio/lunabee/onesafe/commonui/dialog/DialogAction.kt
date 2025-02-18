package studio.lunabee.onesafe.commonui.dialog

import androidx.compose.material3.ButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.semantics
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.accessibility.accessibilityClick
import studio.lunabee.onesafe.atom.button.OSTextButton
import studio.lunabee.onesafe.atom.button.defaults.OSTextButtonDefaults
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.model.OSActionState

data class DialogAction(
    val text: LbcTextSpec,
    val clickLabel: LbcTextSpec? = null,
    val type: Type = Type.Normal,
    val onClick: () -> Unit,
) {
    enum class Type {
        Normal, Dangerous;

        val colors: ButtonColors
            @Composable
            get() = when (this) {
                Normal -> OSTextButtonDefaults.secondaryTextButtonColors(state = OSActionState.Enabled)
                Dangerous -> OSTextButtonDefaults.secondaryAlertTextButtonColors(state = OSActionState.Enabled)
            }
    }

    @Composable
    fun ActionButton(
        modifier: Modifier = Modifier,
        applyAccessibilityDefaultModifier: Boolean = true, // if true and custom Modifier set, your semantics will be overridden
    ) {
        OSTextButton(
            text = text,
            onClick = onClick,
            modifier = modifier
                .accessibilityDefaultDialogAction(applyAccessibilityDefaultModifier),
            buttonColors = type.colors,
        )
    }

    private fun Modifier.accessibilityDefaultDialogAction(
        applyAccessibilityDefaultModifier: Boolean = true, // if true and custom Modifier set, your semantics will be overridden
    ): Modifier {
        return if (applyAccessibilityDefaultModifier) {
            composed {
                val clickLabel = clickLabel?.string
                semantics {
                    accessibilityClick(label = clickLabel, action = onClick)
                }
            }
        } else {
            this
        }
    }

    companion object {
        fun commonOk(onClick: () -> Unit): DialogAction {
            return DialogAction(
                text = LbcTextSpec.StringResource(id = OSString.common_ok),
                type = Type.Normal,
                onClick = onClick,
                clickLabel = LbcTextSpec.StringResource(id = OSString.common_accessibility_dialog_ok),
            )
        }

        fun commonCancel(onClick: () -> Unit): DialogAction {
            return DialogAction(
                text = LbcTextSpec.StringResource(id = OSString.common_cancel),
                type = Type.Normal,
                onClick = onClick,
                clickLabel = LbcTextSpec.StringResource(id = OSString.common_accessibility_dialog_cancel),
            )
        }
    }
}
