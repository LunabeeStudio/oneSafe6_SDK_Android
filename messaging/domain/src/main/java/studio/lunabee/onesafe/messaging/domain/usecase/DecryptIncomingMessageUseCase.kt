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

import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import studio.lunabee.onesafe.bubbles.domain.model.Contact
import studio.lunabee.onesafe.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.onesafe.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.onesafe.bubbles.domain.repository.ContactRepository
import studio.lunabee.onesafe.bubbles.domain.usecase.GetContactUseCase
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.messagecompanion.OSMessage
import studio.lunabee.onesafe.messaging.domain.extension.toInstant
import studio.lunabee.onesafe.messaging.domain.model.OSPlainMessage
import java.util.UUID
import javax.inject.Inject

/**
 * Try to decrypt a message with every contact key
 */
class DecryptIncomingMessageUseCase @Inject constructor(
    private val contactRepository: ContactRepository,
    private val contactKeyRepository: ContactKeyRepository,
    private val bubblesCryptoRepository: BubblesCryptoRepository,
    private val getContactUseCase: GetContactUseCase,
) {
    /**
     * @param messageData The raw message
     *
     * @return failure with NO_MATCHING_CONTACT error code if no contact shared key has been found to decrypt the message.
     */
    suspend operator fun invoke(messageData: ByteArray): LBResult<Pair<Contact, OSPlainMessage>> {
        return getContactUseCase()
            .asFlow()
            .map { contact ->
                OSError.runCatching {
                    val sharedKey = contactRepository.getSharedKey(contact.id)
                    val localKey = contactKeyRepository.getContactLocalKey(contact.id)
                    val plainProto = bubblesCryptoRepository.sharedDecrypt(messageData, localKey, sharedKey)
                    val plainMessage = OSMessage.MessageData.parseFrom(plainProto)
                    contact to OSPlainMessage(
                        content = plainMessage.content,
                        recipientId = UUID.fromString(plainMessage.recipientId),
                        sentAt = plainMessage.sentAt.toInstant(),
                    )
                }
            }.firstOrNull { result ->
                // Get the first result different from BUBBLES_DECRYPTION_FAILED_WRONG_CONTACT_KEY and return it
                val error = (result as? LBResult.Failure)?.throwable as? OSCryptoError
                error?.code != OSCryptoError.Code.BUBBLES_DECRYPTION_FAILED_WRONG_CONTACT_KEY
            } ?: LBResult.Failure(OSDomainError(OSDomainError.Code.NO_MATCHING_CONTACT))
    }
}
