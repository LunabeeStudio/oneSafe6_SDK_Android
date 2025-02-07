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
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.error.BubblesDomainError

fun BubblesDomainError.localizedTitle(): LbcTextSpec? = when (this.code) {
    BubblesDomainError.Code.LOCAL_ENCRYPTION_FAILED,
    BubblesDomainError.Code.LOCAL_DECRYPTION_FAILED,
    BubblesDomainError.Code.NOT_AN_INVITATION_MESSAGE,
    BubblesDomainError.Code.WRONG_CONTACT,
    BubblesDomainError.Code.NO_MATCHING_CONTACT,
    BubblesDomainError.Code.CONTACT_KEY_NOT_FOUND,
    BubblesDomainError.Code.NOT_A_BUBBLES_MESSAGE,
    -> null
}

fun BubblesDomainError.localizedDescription(): LbcTextSpec? = when (this.code) {
    BubblesDomainError.Code.NO_MATCHING_CONTACT -> LbcTextSpec.StringResource(OSString.bubbles_error_noMatchingContact)
    BubblesDomainError.Code.LOCAL_ENCRYPTION_FAILED,
    BubblesDomainError.Code.LOCAL_DECRYPTION_FAILED,
    BubblesDomainError.Code.NOT_AN_INVITATION_MESSAGE,
    BubblesDomainError.Code.WRONG_CONTACT,
    BubblesDomainError.Code.CONTACT_KEY_NOT_FOUND,
    BubblesDomainError.Code.NOT_A_BUBBLES_MESSAGE,
    -> null
}
