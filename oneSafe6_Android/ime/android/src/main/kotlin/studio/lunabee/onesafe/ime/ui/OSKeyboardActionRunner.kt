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
 * Created by Lunabee Studio / Date - 7/27/2023 - for the oneSafe6 SDK.
 * Last modified 7/27/23, 10:35 AM
 */

package studio.lunabee.onesafe.ime.ui

import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction

/**
 * @see androidx.compose.foundation.text.KeyboardActionRunner
 */
class OSKeyboardActionRunner(
    val keyboardActions: KeyboardActions,
) : KeyboardActionScope {

    /**
     * Run the keyboard action corresponding to the specified imeAction.
     */
    fun runAction(imeAction: ImeAction): Boolean {
        val keyboardAction = when (imeAction) {
            ImeAction.Done -> keyboardActions.onDone
            ImeAction.Go -> keyboardActions.onGo
            ImeAction.Next -> keyboardActions.onNext
            ImeAction.Previous -> keyboardActions.onPrevious
            ImeAction.Search -> keyboardActions.onSearch
            ImeAction.Send -> keyboardActions.onSend
            ImeAction.Default, ImeAction.None -> null
            else -> error("invalid ImeAction")
        }
        return if (keyboardAction != null) {
            keyboardAction(this)
            true
        } else {
            false
        }
    }

    override fun defaultKeyboardAction(imeAction: ImeAction) {
        // no-op
    }
}
