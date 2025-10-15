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
 * Created by Lunabee Studio / Date - 9/5/2024 - for the oneSafe6 SDK.
 * Last modified 05/09/2024 09:29
 */

package studio.lunabee.messaging.domain.usecase

import studio.lunabee.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import kotlin.time.Instant

class UpdateContactResetConversationDateUseCase @Inject constructor(
    private val contactRepository: ContactRepository,
    private val contactKeyRepository: ContactKeyRepository,
    private val bubblesCryptoRepository: BubblesCryptoRepository,
) {

    suspend operator fun invoke(contactId: DoubleRatchetUUID, instant: Instant) {
        val contactKey = contactKeyRepository.getContactLocalKey(contactId)
        val encResetConversationDate = bubblesCryptoRepository.localEncrypt(contactKey, EncryptEntry(instant))
        contactRepository.updateContactResetConversationDate(
            id = contactId,
            encResetConversationDate = encResetConversationDate,
        )
    }
}
