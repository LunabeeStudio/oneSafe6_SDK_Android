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
 * Created by Lunabee Studio / Date - 7/3/2023 - for the oneSafe6 SDK.
 * Last modified 03/07/2023 15:44
 */

package studio.lunabee.onesafe.error

data class BubblesDoubleRatchetError(
    override val code: Code,
    override val message: String = code.message,
    override val cause: Throwable? = null,
) : OSError(message, cause, code) {

    enum class Code(override val message: String) : ErrorCode<Code, BubblesDoubleRatchetError> {
        ConversationNotSetup("This contact did not respond to your invitation yet"),
        ConversationNotFound("the conversation doesn't exist"),
        MessageKeyNotFound("This message has already been decrypted"),
        RequiredChainKeyMissing("can't setup conversation because initial chainKey is missing from message header"),
        CantDecryptSentMessage("you can't decrypt your own message"),
        OutdatedMessage("This message is outdated due to a conversation reset"),
    }
}
