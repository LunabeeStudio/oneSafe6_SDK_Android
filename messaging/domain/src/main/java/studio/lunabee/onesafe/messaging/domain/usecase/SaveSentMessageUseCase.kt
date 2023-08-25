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

package studio.lunabee.onesafe.messaging.domain.usecase

import studio.lunabee.onesafe.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.onesafe.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.messaging.domain.model.SentMessage
import studio.lunabee.onesafe.messaging.domain.repository.SentMessageRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class SaveSentMessageUseCase @Inject constructor(
    private val bubblesCryptoRepository: BubblesCryptoRepository,
    private val sentMessageRepository: SentMessageRepository,
    private val contactKeyRepository: ContactKeyRepository,
) {

    suspend operator fun invoke(
        id: UUID,
        messageString: String,
        contactId: UUID,
        createdAt: Instant,
        order: Float,
    ) {
        val key = contactKeyRepository.getContactLocalKey(contactId)
        val encContent = bubblesCryptoRepository.localEncrypt(key, EncryptEntry(messageString))
        sentMessageRepository.saveSentMessage(
            SentMessage(
                id = id,
                encContent = encContent,
                encCreatedAt = bubblesCryptoRepository.localEncrypt(key, EncryptEntry(createdAt)),
                contactId = contactId,
                order = order,
            ),
        )
    }
}
