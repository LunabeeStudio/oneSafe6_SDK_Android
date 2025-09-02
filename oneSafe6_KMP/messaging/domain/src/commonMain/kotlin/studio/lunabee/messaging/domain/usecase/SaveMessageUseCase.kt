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
 * Created by Lunabee Studio / Date - 6/20/2023 - for the oneSafe6 SDK.
 * Last modified 6/20/23, 1:22 PM
 */

package studio.lunabee.messaging.domain.usecase

import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import studio.lunabee.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.bubbles.domain.repository.BubblesSafeRepository
import studio.lunabee.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.bubbles.domain.usecase.GetContactUseCase
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.MessageOrderCalculator
import studio.lunabee.messaging.domain.model.MessageDirection
import studio.lunabee.messaging.domain.model.SafeMessage
import studio.lunabee.messaging.domain.model.SharedMessage
import studio.lunabee.messaging.domain.repository.MessageRepository
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.error.BubblesMessagingError
import studio.lunabee.onesafe.error.OSError

/**
 * Encrypt & store a plain message associated with a contact
 */
class SaveMessageUseCase @Inject constructor(
    private val bubblesCryptoRepository: BubblesCryptoRepository,
    private val messageRepository: MessageRepository,
    private val contactKeyRepository: ContactKeyRepository,
    private val getContactUseCase: GetContactUseCase,
    private val contactRepository: ContactRepository,
    private val bubblesSafeRepository: BubblesSafeRepository,
    private val messageOrderCalculator: MessageOrderCalculator,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        plainMessage: SharedMessage,
        contactId: DoubleRatchetUUID,
        channel: String?,
        id: DoubleRatchetUUID,
        safeItemId: DoubleRatchetUUID?,
    ): LBResult<Float> = OSError.runCatching {
        mutex.withLock {
            val recipient = getContactUseCase(plainMessage.recipientId, bubblesSafeRepository.currentSafeId())
            val key = contactKeyRepository.getContactLocalKey(contactId)
            val encContent = bubblesCryptoRepository.localEncrypt(key, EncryptEntry(plainMessage.content))
            val encSentAt = bubblesCryptoRepository.localEncrypt(key, EncryptEntry(plainMessage.date))
            val encChannel = channel?.let { bubblesCryptoRepository.localEncrypt(key, EncryptEntry(channel)) }
            val encSafeItemId = safeItemId?.let { bubblesCryptoRepository.localEncrypt(key, EncryptEntry(it)) }

            // Test if we know the recipientId (= we are the sender)
            val direction = if (recipient != null) {
                MessageDirection.SENT
            } else {
                MessageDirection.RECEIVED
            }

            val message = SafeMessage(
                id = id,
                fromContactId = contactId,
                encSentAt = encSentAt,
                encContent = encContent,
                direction = direction,
                encChannel = encChannel,
                isRead = direction == MessageDirection.SENT,
                encSafeItemId = encSafeItemId,
            )

            val orderResult: MessageOrderCalculator.OrderResult = messageOrderCalculator.invoke(plainMessage.date, contactId, key)

            return@withLock when (orderResult) {
                is MessageOrderCalculator.OrderResult.Found -> {
                    messageRepository.save(message, orderResult.order)
                    contactRepository.updateUpdatedAt(contactId, clock.now())
                    orderResult.order
                }
                is MessageOrderCalculator.OrderResult.Duplicated -> {
                    // If we have 2 messages with the same sentAt value, make sure this is a duplicate and return a failure (or save it)
                    val dupMessage = messageRepository.getByContactByOrder(contactId, orderResult.duplicatedOrder)
                    val dupContent = bubblesCryptoRepository.localDecrypt(key, DecryptEntry(dupMessage.encContent, String::class))

                    if (dupContent == plainMessage.content) {
                        throw BubblesMessagingError(BubblesMessagingError.Code.DUPLICATED_MESSAGE)
                    } else {
                        messageRepository.save(message, orderResult.candidateOrder)
                    }
                    orderResult.candidateOrder
                }
            }
        }
    }

    companion object {
        private val mutex = Mutex()
    }
}
