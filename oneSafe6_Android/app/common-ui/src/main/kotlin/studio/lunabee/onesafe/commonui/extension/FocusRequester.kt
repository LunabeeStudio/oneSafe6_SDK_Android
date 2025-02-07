/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 8/28/2024 - for the oneSafe6 SDK.
 * Last modified 8/28/24, 11:37 AM
 */

package studio.lunabee.onesafe.commonui.extension

import androidx.compose.ui.focus.FocusRequester
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Make sure we don't request focus if we leaved the composition.
 */
fun FocusRequester.safeRequestFocus(coroutineScope: CoroutineScope) {
    coroutineScope.launch {
        if (isActive) {
            requestFocus()
        }
    }
}
