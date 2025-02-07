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
 * Created by Lunabee Studio / Date - 8/19/2024 - for the oneSafe6 SDK.
 * Last modified 19/08/2024 11:35
 */

package studio.lunabee.onesafe.commonui.error

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.error.BubblesMessagingError

fun BubblesMessagingError.localizedTitle(): LbcTextSpec? = when (this.code) {
    BubblesMessagingError.Code.CONTACT_NOT_FOUND,
    BubblesMessagingError.Code.CONVERSATION_NOT_FOUND,
    BubblesMessagingError.Code.HANDSHAKE_DATA_NOT_FOUND,
    BubblesMessagingError.Code.UNKNOWN_ERROR,
    BubblesMessagingError.Code.DUPLICATED_MESSAGE,
    -> null
}

fun BubblesMessagingError.localizedDescription(): LbcTextSpec? = when (this.code) {
    BubblesMessagingError.Code.CONTACT_NOT_FOUND,
    BubblesMessagingError.Code.CONVERSATION_NOT_FOUND,
    BubblesMessagingError.Code.HANDSHAKE_DATA_NOT_FOUND,
    BubblesMessagingError.Code.UNKNOWN_ERROR,
    BubblesMessagingError.Code.DUPLICATED_MESSAGE,
    -> null
}
