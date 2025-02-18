package studio.lunabee.onesafe.commonui.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.accessibility.accessibilityClick
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.dialog.OSDefaultAlertDialog
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

interface DialogState {
    val actions: List<DialogAction>
    val title: LbcTextSpec
    val message: LbcTextSpec?
    val dismiss: () -> Unit
    val customContent: (@Composable () -> Unit)?
}

@Composable
fun rememberDialogState(): MutableState<DialogState?> = remember {
    mutableStateOf(null)
}

@Composable
fun DialogState.DefaultAlertDialog(
    modifier: Modifier = Modifier,
    applyAccessibilityDefaultModifier: Boolean = true, // if true and custom Modifier set, your semantics implementation will be overridden
) {
    OSDefaultAlertDialog(
        onDismissRequest = dismiss,
        title = title,
        message = message,
        actionContent = {
            actions.forEach { dialogAction ->
                dialogAction.ActionButton()
            }
        },
        modifier = modifier
            .accessibilityDefaultModifier(
                dialogState = this,
                applyAccessibilityDefaultModifier = applyAccessibilityDefaultModifier,
            ),
        additionalContent = {
            customContent?.invoke()
        },
    )
}

/**
 * By default, content of the dialog is merged (i.e title + description is a unique block).
 * Buttons are still accessible individually. If dialog as only one action, action can be directly
 * triggered by double clicking on the dialog content.
 */
fun Modifier.accessibilityDefaultModifier(
    dialogState: DialogState,
    applyAccessibilityDefaultModifier: Boolean,
): Modifier {
    return if (applyAccessibilityDefaultModifier) {
        composed {
            val hasUniqueAction = dialogState.actions.size == 1
            val labelWithAction = if (hasUniqueAction) {
                val clickLabel: String? = dialogState.actions.first().clickLabel?.string
                val clickAction: () -> Unit = dialogState.actions.first().onClick
                clickLabel to clickAction
            } else {
                null
            }

            val warningDescription: String = stringResource(id = OSString.common_warning)

            semantics(mergeDescendants = true) {
                contentDescription = warningDescription
                labelWithAction?.let {
                    accessibilityClick(label = labelWithAction.first, action = labelWithAction.second)
                }
            }
        }
    } else {
        this
    }
}

@OsDefaultPreview
@Composable
private fun DefaultAlertDialogPreview() {
    OSPreviewOnSurfaceTheme {
        object : DialogState {
            override val actions = listOf(
                DialogAction.commonCancel { },
                DialogAction.commonOk { },
            )
            override val title = LbcTextSpec.Raw("Title")
            override val message = loremIpsumSpec(10)
            override val dismiss = {}
            override val customContent:
                @Composable()
                (() -> Unit)? = null
        }.DefaultAlertDialog()
    }
}
