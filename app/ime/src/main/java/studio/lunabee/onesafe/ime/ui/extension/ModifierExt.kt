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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import dev.patrickgold.florisboard.editorInstance
import studio.lunabee.onesafe.ime.InterceptEditorInstance

fun Modifier.keyboardTextfield(
    isKeyboardVisible: () -> Boolean,
    toggleKeyboardVisibility: () -> Unit,
    textFieldValue: () -> TextFieldValue,
    setTextFieldValue: (TextFieldValue) -> Unit,
): Modifier {
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
                    editorInstance.intercept = null
                    editorInstance.deleteBackwards = null
                }
            }
        }
}
