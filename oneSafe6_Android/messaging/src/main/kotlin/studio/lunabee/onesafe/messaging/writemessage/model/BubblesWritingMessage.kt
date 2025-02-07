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
 * Created by Lunabee Studio / Date - 5/28/2024 - for the oneSafe6 SDK.
 * Last modified 5/28/24, 10:08 AM
 */

package studio.lunabee.onesafe.messaging.writemessage.model

import androidx.compose.ui.text.input.TextFieldValue

/**
 * @property preview null if preview is disabled
 */
data class BubblesWritingMessage(
    val plainMessage: TextFieldValue,
    val preview: String?,
)
