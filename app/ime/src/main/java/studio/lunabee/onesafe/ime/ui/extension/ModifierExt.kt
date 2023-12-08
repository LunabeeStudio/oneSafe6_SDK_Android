/*
 * Copyright (c) 2023 Lunabee Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Lunabee Studio / Date - 6/12/2023 - for the oneSafe6 SDK.
 * Last modified 6/12/23, 10:31 AM
 */

package studio.lunabee.onesafe.ime.ui.extension

import android.text.InputType
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.ImeOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.view.inputmethod.EditorInfoCompat
import dev.patrickgold.florisboard.editorInstance
import dev.patrickgold.florisboard.ime.editor.FlorisEditorInfo
import studio.lunabee.onesafe.ime.InterceptEditorInstance
import studio.lunabee.onesafe.ime.ui.OSKeyboardActionRunner

@Suppress("LongParameterList")
fun Modifier.keyboardTextfield(
    isKeyboardVisible: () -> Boolean,
    toggleKeyboardVisibility: () -> Unit,
    textFieldValue: () -> TextFieldValue,
    setTextFieldValue: (TextFieldValue) -> Unit,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
): Modifier {
    val osKeyboardActionRunner = OSKeyboardActionRunner(keyboardActions)
    return this then Modifier
        .composed {
            val context = LocalContext.current
            val editorInstance by remember(context) {
                mutableStateOf((context.editorInstance().value as InterceptEditorInstance))
            }
            onFocusChanged { state ->
                if (state.hasFocus) {
                    if (!isKeyboardVisible()) {
                        toggleKeyboardVisibility()
                    }
                    val editorInfo = EditorInfo().apply {
                        update(keyboardOptions.toImeOptions(), textFieldValue())
                    }
                    editorInstance.handleStartInputView(
                        editorInfo = FlorisEditorInfo.wrap(editorInfo),
                        isRestart = true,
                    )
                    editorInstance.interceptAction = { _ ->
                        osKeyboardActionRunner.runAction(keyboardOptions.imeAction)
                        true
                    }
                    editorInstance.intercept = { newText ->
                        val fieldValue = textFieldValue()
                        val selection = fieldValue.selection
                        val text = fieldValue.text.replaceRange(selection.start, selection.end, newText)
                        setTextFieldValue(TextFieldValue(text, TextRange(selection.start + newText.length)))
                        true
                    }
                    editorInstance.deleteBackwards = {
                        val fieldValue = textFieldValue()
                        val selection = fieldValue.selection
                        val text: String
                        val position: Int
                        if (selection.start == selection.end) {
                            text = fieldValue.text.removeRange(
                                (selection.start - 1).coerceAtLeast(0),
                                selection.start,
                            )
                            position = (selection.start - 1).coerceAtLeast(0)
                        } else {
                            text = fieldValue.text.removeRange(selection.start, selection.end)
                            position = selection.start
                        }
                        setTextFieldValue(TextFieldValue(text, TextRange(position)))
                        true
                    }
                } else {
                    // reset to default values
                    editorInstance.intercept = null
                    editorInstance.deleteBackwards = null
                    editorInstance.interceptAction = { false }
                }
            }
        }
}

// https://android.googlesource.com/platform//frameworks/support/+/refs/heads/androidx-main/compose/ui/ui/src/androidMain/kotlin/androidx/compose/ui/text/input/TextInputServiceAndroid.android.kt#482
private fun EditorInfo.update(imeOptions: ImeOptions, textFieldValue: TextFieldValue) {
    this.imeOptions = when (imeOptions.imeAction) {
        ImeAction.Default -> {
            if (imeOptions.singleLine) {
                // this is the last resort to enable single line
                // Android IME still show return key even if multi line is not send
                // TextView.java#onCreateInputConnection
                EditorInfo.IME_ACTION_DONE
            } else {
                EditorInfo.IME_ACTION_UNSPECIFIED
            }
        }
        ImeAction.None -> EditorInfo.IME_ACTION_NONE
        ImeAction.Go -> EditorInfo.IME_ACTION_GO
        ImeAction.Next -> EditorInfo.IME_ACTION_NEXT
        ImeAction.Previous -> EditorInfo.IME_ACTION_PREVIOUS
        ImeAction.Search -> EditorInfo.IME_ACTION_SEARCH
        ImeAction.Send -> EditorInfo.IME_ACTION_SEND
        ImeAction.Done -> EditorInfo.IME_ACTION_DONE
        else -> error("invalid ImeAction")
    }
    when (imeOptions.keyboardType) {
        KeyboardType.Text -> this.inputType = InputType.TYPE_CLASS_TEXT
        KeyboardType.Ascii -> {
            this.inputType = InputType.TYPE_CLASS_TEXT
            this.imeOptions = this.imeOptions or EditorInfo.IME_FLAG_FORCE_ASCII
        }
        KeyboardType.Number -> this.inputType = InputType.TYPE_CLASS_NUMBER
        KeyboardType.Phone -> this.inputType = InputType.TYPE_CLASS_PHONE
        KeyboardType.Uri ->
            this.inputType = InputType.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_URI
        KeyboardType.Email ->
            this.inputType =
                InputType.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        KeyboardType.Password -> {
            this.inputType =
                InputType.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
        }
        KeyboardType.NumberPassword -> {
            this.inputType =
                InputType.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD
        }
        KeyboardType.Decimal -> {
            this.inputType =
                InputType.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_FLAG_DECIMAL
        }
        else -> error("Invalid Keyboard Type")
    }
    if (!imeOptions.singleLine) {
        if (hasFlag(this.inputType, InputType.TYPE_CLASS_TEXT)) {
            // TextView.java#setInputTypeSingleLine
            this.inputType = this.inputType or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            if (imeOptions.imeAction == ImeAction.Default) {
                this.imeOptions = this.imeOptions or EditorInfo.IME_FLAG_NO_ENTER_ACTION
            }
        }
    }
    if (hasFlag(this.inputType, InputType.TYPE_CLASS_TEXT)) {
        when (imeOptions.capitalization) {
            KeyboardCapitalization.Characters -> {
                this.inputType = this.inputType or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            }
            KeyboardCapitalization.Words -> {
                this.inputType = this.inputType or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            }
            KeyboardCapitalization.Sentences -> {
                this.inputType = this.inputType or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            }
            else -> {
                /* do nothing */
            }
        }
        if (imeOptions.autoCorrect) {
            this.inputType = this.inputType or InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
        }
    }
    this.initialSelStart = textFieldValue.selection.start
    this.initialSelEnd = textFieldValue.selection.end
    EditorInfoCompat.setInitialSurroundingText(this, textFieldValue.text)
    this.imeOptions = this.imeOptions or EditorInfo.IME_FLAG_NO_FULLSCREEN
}

private fun hasFlag(bits: Int, flag: Int): Boolean = (bits and flag) == flag

private fun KeyboardOptions.toImeOptions(singleLine: Boolean = true) = ImeOptions(
    singleLine = singleLine,
    capitalization = capitalization,
    autoCorrect = autoCorrect,
    keyboardType = keyboardType,
    imeAction = imeAction,
)
