package studio.lunabee.onesafe.feature.itemform.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.textfield.OSTextField
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.ui.UiConstants.TestTag.Item.RenameFieldTextField
import studio.lunabee.onesafe.ui.res.OSDimens

class RenameFieldDialogState(
    val forceDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onValueChange: (String) -> Unit,
    currentName: String,
) : DialogState {

    // So that click outside won't dismiss the dialog
    override val dismiss: () -> Unit = {}

    override val actions: List<DialogAction> = listOf(
        DialogAction.commonCancel(forceDismiss),
        DialogAction(
            text = LbcTextSpec.StringResource(OSString.common_confirm),
            onClick = { onConfirm() },
        ),
    )

    override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.itemForm_action_rename)

    override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.itemForm_renameAlert_description, currentName)

    override val customContent: @Composable () -> Unit = {
        var fieldName: TextFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
        val focusRequester = remember { FocusRequester() }

        Column(
            modifier = Modifier.padding(top = OSDimens.SystemSpacing.Regular),
        ) {
            OSTextField(
                textFieldValue = fieldName,
                label = null,
                placeholder = LbcTextSpec.StringResource(OSString.itemForm_renameAlert_textField),
                onValueChange = { newValue ->
                    fieldName = newValue
                    onValueChange(newValue.text)
                },
                modifier = Modifier
                    .testTag(RenameFieldTextField)
                    .focusRequester(focusRequester),
                maxLines = 1,
                keyboardActions = KeyboardActions(
                    onDone = {
                        onConfirm()
                    },
                ),
            )
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}
