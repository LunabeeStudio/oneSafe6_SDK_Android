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
 * Created by Lunabee Studio / Date - 8/18/2023 - for the oneSafe6 SDK.
 * Last modified 18/08/2023 11:07
 */

package studio.lunabee.messaging.domain.usecase

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.bubbles.domain.repository.BubblesSafeRepository
import studio.lunabee.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.model.createRandomUUID
import studio.lunabee.messaging.domain.model.SentMessage
import studio.lunabee.messaging.domain.model.SharedMessage
import studio.lunabee.messaging.domain.repository.SentMessageRepository
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.error.OSError
import kotlin.time.Instant

/**
 * Save a new sent message in permanent storage and also as a [SentMessage] use to re-send a message
 */
class SaveSentMessageUseCase @Inject constructor(
    private val bubblesCryptoRepository: BubblesCryptoRepository,
    private val sentMessageRepository: SentMessageRepository,
    private val contactKeyRepository: ContactKeyRepository,
    private val saveMessageUseCase: SaveMessageUseCase,
    private val bubblesSafeRepository: BubblesSafeRepository,
) {
    /**
     * @param plainMessage Message to be encrypted and store in message storage
     * @param messageString Encoded sent message string to be encrypted and store in sent message
     * @param contactId the associated contact identifier
     * @param createdAt creation date of the message
     * @param channel channel on which the message has been send if known
     *
     * @return The [SentMessage] saved or null if re-send feature is disabled
     */
    @Suppress("LongParameterList")
    suspend operator fun invoke(
        plainMessage: SharedMessage,
        messageString: ByteArray,
        contactId: DoubleRatchetUUID,
        createdAt: Instant,
        channel: String?,
    ): LBResult<SentMessage?> {
        val messageId = createRandomUUID()
        val orderRes = saveMessageUseCase(
            plainMessage = plainMessage,
            contactId = contactId,
            channel = channel,
            id = messageId,
            safeItemId = null,
        )
        return when (orderRes) {
            is LBResult.Failure -> LBResult.Failure(orderRes.throwable)
            is LBResult.Success -> OSError.runCatching {
                val key = contactKeyRepository.getContactLocalKey(contactId)
                val encContent = bubblesCryptoRepository.localEncrypt(key, EncryptEntry(messageString))
                val sentMessage = SentMessage(
                    id = messageId,
                    encContent = encContent,
                    encCreatedAt = bubblesCryptoRepository.localEncrypt(key, EncryptEntry(createdAt)),
                    contactId = contactId,
                    order = orderRes.successData,
                    safeId = bubblesSafeRepository.currentSafeId(),
                )
                sentMessageRepository.saveSentMessage(sentMessage)
                sentMessage
            }
        }
    }
}
