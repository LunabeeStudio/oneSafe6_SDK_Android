package studio.lunabee.onesafe.atom.textfield

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import studio.lunabee.onesafe.window.LocalOnTouchWindow

typealias TextFieldExternalModifier = (
    textFieldValue: () -> TextFieldValue,
    setTextFieldValue: (TextFieldValue) -> Unit,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
) -> Modifier

val LocalTextFieldInteraction: ProvidableCompositionLocal<TextFieldExternalModifier?> = staticCompositionLocalOf {
    null
}

/**
 * oneSafe Keyboard -> In oSK we need to maintain the textfield state manually. So when using the String API of text field, we map
 * it to the [TextFieldValue] API by copying code from the [androidx.compose.foundation.text.BasicTextField].
 */
@Composable
fun OSTextField(
    value: String,
    label: LbcTextSpec?,
    placeholder: LbcTextSpec?,
    onValueChange: (newValue: String) -> Unit,
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
    // ########## SEE BasicTextField.kt ##########
    // Holds the latest internal TextFieldValue state. We need to keep it to have the correct value
    // of the composition.
    var textFieldValueState by remember { mutableStateOf(TextFieldValue(text = value)) }
    // Holds the latest TextFieldValue that BasicTextField was recomposed with. We couldn't simply
    // pass `TextFieldValue(text = value)` to the CoreTextField because we need to preserve the
    // composition.
    val textFieldValue = textFieldValueState.copy(text = value)

    SideEffect {
        if (textFieldValue.selection != textFieldValueState.selection ||
            textFieldValue.composition != textFieldValueState.composition
        ) {
            textFieldValueState = textFieldValue
        }
    }
    // Last String value that either text field was recomposed with or updated in the onValueChange
    // callback. We keep track of it to prevent calling onValueChange(String) for same String when
    // CoreTextField's onValueChange is called multiple times without recomposition in between.
    var lastTextValue by remember(value) { mutableStateOf(value) }
    // ########## SEE BasicTextField.kt ##########

    val onTouch = LocalOnTouchWindow.current
    val textFieldExternalModifier = LocalTextFieldInteraction.current?.invoke(
        { textFieldValueState }, // textFieldValue
        {
            onValueChange(it.text)
            textFieldValueState = it
            onTouch()
        }, // setTextFieldValue
        keyboardOptions, // keyboardOptions
        keyboardActions, // keyboardActions
    )
    OSOutlineTextField(
        value = textFieldValue,
        onValueChange = { newTextFieldValueState ->
            // ########## SEE BasicTextField.kt ##########
            textFieldValueState = newTextFieldValueState

            val stringChangedSinceLastInvocation = lastTextValue != newTextFieldValueState.text
            lastTextValue = newTextFieldValueState.text

            if (stringChangedSinceLastInvocation) {
                onValueChange(newTextFieldValueState.text)
            }
            // ########## SEE BasicTextField.kt ##########
        },
        placeholder = placeholder,
        label = label,
        onTouch = onTouch,
        modifier = modifier
            .fillMaxWidth()
            .then(textFieldExternalModifier ?: Modifier),
        inputTextStyle = inputTextStyle,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        maxLines = maxLines,
        minLines = minLines,
        colors = colors,
        trailingAction = trailingAction,
        enabled = enabled,
        isError = isError,
        keyboardActions = keyboardActions,
        readOnly = readOnly,
        errorLabel = errorLabel,
    )
}

@Composable
fun OSTextField(
    textFieldValue: TextFieldValue,
    label: LbcTextSpec?,
    placeholder: LbcTextSpec?,
    onValueChange: (newValue: TextFieldValue) -> Unit,
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
    val onTouch = LocalOnTouchWindow.current
    val textFieldExternalModifier = LocalTextFieldInteraction.current?.invoke(
        { textFieldValue },
        {
            onValueChange(it)
            onTouch()
        },
        keyboardOptions,
        keyboardActions,
    )

    OSOutlineTextField(
        value = textFieldValue,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .then(textFieldExternalModifier ?: Modifier),
        inputTextStyle = inputTextStyle,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        maxLines = maxLines,
        minLines = minLines,
        colors = colors,
        trailingAction = trailingAction,
        enabled = enabled,
        isError = isError,
        keyboardActions = keyboardActions,
        readOnly = readOnly,
        label = label,
        placeholder = placeholder,
        onTouch = onTouch,
        errorLabel = errorLabel,
    )
}

@Composable
@OsDefaultPreview
private fun OSTextFieldTextPreview() {
    OSPreviewOnSurfaceTheme {
        OSTextField(
            value = loremIpsum(words = 2),
            onValueChange = { },
            label = loremIpsumSpec(words = 2),
            placeholder = loremIpsumSpec(words = 2),
            modifier = Modifier
                .padding(all = OSDimens.SystemSpacing.Regular),
        )
    }
}

@Composable
@OsDefaultPreview
private fun OSTextFieldErrorPreview() {
    OSPreviewOnSurfaceTheme {
        OSTextField(
            value = loremIpsum(words = 2),
            onValueChange = { },
            label = loremIpsumSpec(words = 2),
            placeholder = loremIpsumSpec(words = 2),
            modifier = Modifier
                .padding(all = OSDimens.SystemSpacing.Regular),
            errorLabel = loremIpsumSpec(words = 3),
        )
    }
}
