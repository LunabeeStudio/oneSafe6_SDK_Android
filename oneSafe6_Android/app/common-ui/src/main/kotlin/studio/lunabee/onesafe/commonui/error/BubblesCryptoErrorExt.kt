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
import studio.lunabee.onesafe.error.BubblesCryptoError

fun BubblesCryptoError.localizedTitle(): LbcTextSpec? = when (this.code) {
    BubblesCryptoError.Code.BUBBLES_ENCRYPTION_FAILED_BAD_KEY,
    BubblesCryptoError.Code.BUBBLES_DECRYPTION_FAILED_WRONG_KEY,
    BubblesCryptoError.Code.BUBBLES_ENCRYPTION_FAILED_BAD_CONTACT_KEY,
    BubblesCryptoError.Code.BUBBLES_DECRYPTION_FAILED_WRONG_CONTACT_KEY,
    BubblesCryptoError.Code.BUBBLES_DECRYPTION_FAILED_QUEUE_KEY,
    BubblesCryptoError.Code.BUBBLES_ENCRYPTION_FAILED_QUEUE_KEY,
    BubblesCryptoError.Code.BUBBLES_DECRYPTION_FAILED_WRONG_MESSAGE_KEY,
    BubblesCryptoError.Code.BUBBLES_MASTER_KEY_ENCRYPTION_FAIL,
    BubblesCryptoError.Code.BUBBLES_CONTACT_KEY_ENCRYPTION_FAIL,
    BubblesCryptoError.Code.BUBBLES_CONTACT_KEY_DECRYPTION_FAIL,
    BubblesCryptoError.Code.ENCRYPTION_FAILED_BAD_KEY,
    BubblesCryptoError.Code.DECRYPTION_FAILED_BAD_KEY,
    BubblesCryptoError.Code.BUBBLES_MASTER_KEY_NOT_LOADED,
    BubblesCryptoError.Code.ILLEGAL_VALUE,
    -> null
}

fun BubblesCryptoError.localizedDescription(): LbcTextSpec? = when (this.code) {
    BubblesCryptoError.Code.BUBBLES_ENCRYPTION_FAILED_BAD_KEY,
    BubblesCryptoError.Code.BUBBLES_DECRYPTION_FAILED_WRONG_KEY,
    BubblesCryptoError.Code.BUBBLES_ENCRYPTION_FAILED_BAD_CONTACT_KEY,
    BubblesCryptoError.Code.BUBBLES_DECRYPTION_FAILED_WRONG_CONTACT_KEY,
    BubblesCryptoError.Code.BUBBLES_DECRYPTION_FAILED_QUEUE_KEY,
    BubblesCryptoError.Code.BUBBLES_ENCRYPTION_FAILED_QUEUE_KEY,
    BubblesCryptoError.Code.BUBBLES_DECRYPTION_FAILED_WRONG_MESSAGE_KEY,
    BubblesCryptoError.Code.BUBBLES_MASTER_KEY_ENCRYPTION_FAIL,
    BubblesCryptoError.Code.BUBBLES_CONTACT_KEY_ENCRYPTION_FAIL,
    BubblesCryptoError.Code.BUBBLES_CONTACT_KEY_DECRYPTION_FAIL,
    BubblesCryptoError.Code.ENCRYPTION_FAILED_BAD_KEY,
    BubblesCryptoError.Code.DECRYPTION_FAILED_BAD_KEY,
    BubblesCryptoError.Code.BUBBLES_MASTER_KEY_NOT_LOADED,
    BubblesCryptoError.Code.ILLEGAL_VALUE,
    -> null
}
