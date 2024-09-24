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
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:24 AM
 */

package studio.lunabee.onesafe.error

data class BubblesDomainError(
    override val code: Code,
    override val message: String = code.message,
    override val cause: Throwable? = null,
) : OSError(message, cause, code) {

    enum class Code(override val message: String) : ErrorCode<Code, BubblesDomainError> {
        LOCAL_ENCRYPTION_FAILED("Fail to encrypt the data with local contact key"),
        LOCAL_DECRYPTION_FAILED("Fail to decrypt the data with local contact key"),
        NOT_AN_INVITATION_MESSAGE("this message is not an invitation message"),
        NOT_A_BUBBLES_MESSAGE("this message is not a bubbles message"),
        WRONG_CONTACT("this message is not for this contact"),
        NO_MATCHING_CONTACT("No contact key matching the encrypted message"),
        CONTACT_KEY_NOT_FOUND("The contact key does not exist"),
    }
}
