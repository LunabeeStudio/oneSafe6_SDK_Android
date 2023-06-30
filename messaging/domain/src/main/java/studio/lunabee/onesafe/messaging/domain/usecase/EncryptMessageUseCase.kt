/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 6/7/2023 - for the oneSafe6 SDK.
 * Last modified 6/7/23, 4:41 PM
 */

package studio.lunabee.onesafe.messaging.domain.usecase

import com.google.protobuf.timestamp
import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.onesafe.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.onesafe.bubbles.domain.repository.ContactRepository
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.messagecompanion.OSMessage
import studio.lunabee.onesafe.messagecompanion.messageData
import studio.lunabee.onesafe.messaging.domain.extension.now
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Encrypt a message for a contact
 */
class EncryptMessageUseCase @Inject constructor(
    private val contactRepository: ContactRepository,
    private val contactKeyRepository: ContactKeyRepository,
    private val bubblesCryptoRepository: BubblesCryptoRepository,
) {
    @OptIn(ExperimentalEncodingApi::class)
    suspend operator fun invoke(plainMessage: String, contactId: UUID): LBResult<String> = OSError.runCatching {
        val sharedKey = contactRepository.getSharedKey(contactId)
        val localKey = contactKeyRepository.getContactLocalKey(contactId)
        val osMessage: OSMessage.MessageData = messageData {
            this.content = plainMessage
            this.recipientId = contactId.toString()
            this.sentAt = timestamp { now() } // TODO don't get "now" here, add param
        }
        ByteArrayOutputStream().use { byteArrayOutputStream ->
            bubblesCryptoRepository.sharedEncrypt(byteArrayOutputStream, localKey, sharedKey).use { outputStream ->
                osMessage.writeTo(outputStream)
            }
            Base64.encode(byteArrayOutputStream.toByteArray())
        }
    }
}
