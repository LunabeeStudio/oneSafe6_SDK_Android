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

data class OSMessagingError(
    override val code: Code,
    override val message: String = code.message,
    override val cause: Throwable? = null,
) : OSError(message, cause, code) {

    enum class Code(override val message: String) : ErrorCode<Code, OSMessagingError> {
        CONTACT_NOT_FOUND("Cannot find a contact with the provided ID"),
        CONVERSATION_NOT_FOUND("Cannot find a conversation with the provided ID"),
        HANDSHAKE_DATA_NOT_FOUND("Cannot find the handshake data with the provided ID"),
        UNKNOWN_ERROR("Something wrong happened"),
    }
}
