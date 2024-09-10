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
 */

package studio.lunabee.bubbles.error

data class BubblesCryptoError(
    val code: Code,
    override val message: String = code.message,
    override val cause: Throwable? = null,
) : BubblesError(message, cause) {

    enum class Code(val message: String) {
        BUBBLES_ENCRYPTION_FAILED_BAD_KEY("Unable to encrypt with the provided key"),
        BUBBLES_DECRYPTION_FAILED_WRONG_KEY("Unable to decrypt with the provided key"),
        BUBBLES_ENCRYPTION_FAILED_BAD_CONTACT_KEY("Unable to encrypt with the provided key"),
        BUBBLES_DECRYPTION_FAILED_WRONG_CONTACT_KEY("Unable to decrypt with the provided key"),
        BUBBLES_DECRYPTION_FAILED_QUEUE_KEY("Unable to decrypt the queue with the provided key"),
        BUBBLES_ENCRYPTION_FAILED_QUEUE_KEY("Unable to encrypt the queue with the provided key"),
        BUBBLES_DECRYPTION_FAILED_WRONG_MESSAGE_KEY("Unable to decrypt the message with the provided key"),
        BUBBLES_MASTER_KEY_ENCRYPTION_FAIL("Fail to encrypt the bubbles master key"),
        BUBBLES_CONTACT_KEY_ENCRYPTION_FAIL("Fail to encrypt a bubbles contact key"),
        BUBBLES_CONTACT_KEY_DECRYPTION_FAIL("Fail to decrypt a bubbles contact key"),
        ENCRYPTION_FAILED_BAD_KEY("Unable to encrypt with the provided key"),
        DECRYPTION_FAILED_BAD_KEY("Unable to decrypt with the provided key"),
        BUBBLES_MASTER_KEY_NOT_LOADED("Bubbles contact key not loaded in memory"),
        MISSING_MAPPER("No ByteArray mapper found"),
        ILLEGAL_VALUE("Unexpected value"),
    }
}
