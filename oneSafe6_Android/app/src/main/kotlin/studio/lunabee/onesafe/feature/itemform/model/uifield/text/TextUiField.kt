package studio.lunabee.onesafe.feature.itemform.model.uifield.text

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import studio.lunabee.compose.accessibility.state.AccessibilityState
import studio.lunabee.onesafe.accessibility.accessibilityInvisibleToUser
import studio.lunabee.onesafe.accessibility.rememberOSAccessibilityState
import studio.lunabee.onesafe.atom.textfield.OSTextField
import studio.lunabee.onesafe.domain.model.safeitem.FieldMask
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemInputType
import studio.lunabee.onesafe.feature.itemform.model.uifield.UiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.cleaner.ValueCleaner
import studio.lunabee.onesafe.feature.itemform.screen.MaskVisualTransformation
import studio.lunabee.onesafe.feature.itemform.screen.NumberVisualTransformation
import studio.lunabee.onesafe.ui.UiConstants

abstract class TextUiField(
    private val valueCleaner: ValueCleaner? = null,
) : UiField() {
    protected var rawValue: String by mutableStateOf("")

    private val maxLine
        get() = when (safeItemFieldKind) {
            SafeItemFieldKind.Note, SafeItemFieldKind.Password -> Int.MAX_VALUE
            else -> 1
        }

    override fun initValues(isIdentifier: Boolean, initialValue: String?) {
        this.isIdentifier = isIdentifier
        this.rawValue = initialValue ?: ""
    }

    open fun getVisualTransformation(): VisualTransformation {
        return when {
            safeItemFieldKind.maskList.isNotEmpty() -> {
                val matchingMask = FieldMask.getMatchingMask(safeItemFieldKind.maskList, rawValue)
                matchingMask?.let { MaskVisualTransformation(it.formattingMask.orEmpty()) } ?: VisualTransformation.None
            }
            safeItemFieldKind is SafeItemFieldKind.Number -> NumberVisualTransformation()
            else -> VisualTransformation.None
        }
    }

    // Boolean to display error only if the field was in focus
    var wasCaptured: Boolean = false
    open fun onFocusEvent(focusState: FocusState) {
        if (wasCaptured) {
            wasCaptured = false
            isErrorDisplayed = isInError()
        } else if (focusState.hasFocus && focusState.isFocused) {
            wasCaptured = true
        }
    }

    open fun onValueChanged(value: String) {
        rawValue = valueCleaner?.invoke(value) ?: value
    }

    @Composable
    abstract fun getTextStyle(): TextStyle

    @Composable
    override fun InnerComposable(modifier: Modifier, hasNext: Boolean) {
        val focusManager = LocalFocusManager.current
        val accessibilityState: AccessibilityState = rememberOSAccessibilityState()
        OSTextField(
            value = rawValue,
            placeholder = if (accessibilityState.isTouchExplorationEnabled) {
                null
            } else {
                placeholder
            },
            label = fieldDescription.value,
            onValueChange = ::onValueChanged,
            modifier = modifier
                .fillMaxWidth()
                .onFocusEvent(::onFocusEvent)
                .testTag(UiConstants.TestTag.Item.ItemFormField),
            inputTextStyle = getTextStyle(),
            visualTransformation = getVisualTransformation(),
            maxLines = maxLine,
            keyboardOptions = getKeyboardOptions(
                hasNext = hasNext,
            ),
            keyboardActions = if (!hasNext) {
                KeyboardActions {
                    focusManager.clearFocus(true)
                }
            } else {
                KeyboardActions.Default
            },
            isError = isErrorDisplayed,
            trailingAction = if (options.isEmpty()) {
                null
            } else {
                {
                    Row {
                        options.forEach { uiFieldOption ->
                            uiFieldOption.ComposableLayout(
                                modifier = modifier
                                    .accessibilityInvisibleToUser(), // handle by parent in custom actions.
                            )
                        }
                    }
                }
            },
            errorLabel = errorFieldLabel,
        )
    }

    private fun getKeyboardOptions(hasNext: Boolean): KeyboardOptions {
        val imeAction: ImeAction = when {
            safeItemFieldKind == SafeItemFieldKind.Note -> ImeAction.Default
            hasNext -> ImeAction.Next
            else -> ImeAction.Done
        }
        return when (safeItemFieldKind.inputType) {
            SafeItemInputType.Default -> KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = imeAction,
            )
            SafeItemInputType.Email -> KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Email,
                imeAction = imeAction,
            )
            SafeItemInputType.Phone -> KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Phone,
                imeAction = imeAction,
            )
            SafeItemInputType.Number -> KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.NumberPassword,
                imeAction = imeAction,
            )
            SafeItemInputType.Password -> KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Password,
                imeAction = imeAction,
            )
            SafeItemInputType.Url -> KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Uri,
                imeAction = imeAction,
            )
            SafeItemInputType.DefaultCap -> KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = imeAction,
            )
        }
    }
}
