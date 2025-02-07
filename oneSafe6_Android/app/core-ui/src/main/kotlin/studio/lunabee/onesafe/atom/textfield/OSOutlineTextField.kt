package studio.lunabee.onesafe.atom.textfield

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem

@Composable
internal fun OSOutlineTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: LbcTextSpec?,
    label: LbcTextSpec?,
    onTouch: () -> Unit,
    modifier: Modifier = Modifier,
    inputTextStyle: TextStyle = LocalTextStyle.current,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    trailingAction: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    errorLabel: LbcTextSpec? = null,
    isError: Boolean = false,
    readOnly: Boolean = false,
    colors: TextFieldColors = LocalDesignSystem.current.outlinedTextFieldColors(),
) {
    OutlinedTextField(
        value = value,
        onValueChange = {
            onValueChange(it)
            onTouch()
        },
        modifier = modifier
            .fillMaxWidth(),
        textStyle = inputTextStyle,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        maxLines = maxLines,
        minLines = minLines,
        singleLine = maxLines == 1, // For horizontal scrolling
        placeholder = placeholder?.let {
            {
                OSText(text = placeholder)
            }
        },
        label = label?.let {
            {
                OSText(text = label)
            }
        },
        colors = colors,
        trailingIcon = trailingAction,
        enabled = enabled,
        isError = isError,
        keyboardActions = keyboardActions,
        supportingText = if (errorLabel != null && isError) {
            {
                OSText(
                    text = errorLabel,
                    modifier = Modifier
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        } else {
            null
        },
        readOnly = readOnly,
    )
}
