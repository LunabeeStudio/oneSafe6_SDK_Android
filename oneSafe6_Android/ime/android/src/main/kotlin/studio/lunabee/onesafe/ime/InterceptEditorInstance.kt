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
 * Last modified 6/12/23, 10:15 AM
 */

package studio.lunabee.onesafe.ime

import android.content.Context
import dev.patrickgold.florisboard.ime.editor.EditorInstance
import dev.patrickgold.florisboard.ime.editor.ImeOptions
import dev.patrickgold.florisboard.ime.editor.OperationUnit

class InterceptEditorInstance(context: Context) : EditorInstance(context) {

    var intercept: ((String) -> Boolean)? = null
    var deleteBackwards: (() -> Boolean)? = null
    var interceptAction: ((action: ImeOptions.Action) -> Boolean) = { false }
    var blockInput: Boolean = false

    override fun commitChar(char: String): Boolean {
        return when {
            intercept != null -> {
                intercept?.invoke(char)
                true
            }
            blockInput -> true
            else -> super.commitChar(char)
        }
    }

    override fun commitText(text: String): Boolean {
        return when {
            intercept != null -> {
                intercept?.invoke(text)
                true
            }
            blockInput -> true
            else -> super.commitText(text)
        }
    }

    override fun deleteBackwards(unit: OperationUnit): Boolean {
        return when {
            deleteBackwards != null -> {
                deleteBackwards?.invoke()
                true
            }
            blockInput -> true
            else -> super.deleteBackwards(unit)
        }
    }

    override fun performEnterAction(action: ImeOptions.Action): Boolean {
        return when {
            interceptAction(action) -> true // intercept action and forward to oSK
            blockInput -> true // block action even if not intercepted because oSK is shown
            else -> super.performEnterAction(action)
        }
    }
}
