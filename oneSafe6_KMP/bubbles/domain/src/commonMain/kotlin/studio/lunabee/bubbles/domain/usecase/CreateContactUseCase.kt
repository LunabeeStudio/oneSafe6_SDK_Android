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
 * Created by Lunabee Studio / Date - 5/22/2023 - for the oneSafe6 SDK.
 * Last modified 5/22/23, 3:12 PM
 */

package studio.lunabee.bubbles.domain.usecase

import kotlin.time.Clock
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.bubbles.domain.model.contact.Contact
import studio.lunabee.bubbles.domain.model.contact.PlainContact
import studio.lunabee.bubbles.domain.model.contactkey.ContactLocalKey
import studio.lunabee.bubbles.domain.model.contactkey.ContactSharedKey
import studio.lunabee.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.bubbles.domain.repository.BubblesSafeRepository
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry

class CreateContactUseCase @Inject constructor(
    private val contactRepository: ContactRepository,
    private val bubblesCryptoRepository: BubblesCryptoRepository,
    private val clock: Clock,
    private val bubblesSafeRepository: BubblesSafeRepository,
) {
    suspend operator fun invoke(plainContact: PlainContact) {
        val localKey: ContactLocalKey = bubblesCryptoRepository.generateLocalKeyForContact()
        val encSharedKey = plainContact.sharedKey?.let {
            ContactSharedKey(bubblesCryptoRepository.localEncrypt(localKey, EncryptEntry(it)))
        }
        val contact = Contact(
            id = plainContact.id,
            encName = bubblesCryptoRepository.localEncrypt(localKey, EncryptEntry(plainContact.name)),
            encSharedKey = encSharedKey,
            updatedAt = clock.now(),
            sharedConversationId = plainContact.sharedConversationId,
            encSharingMode = bubblesCryptoRepository.localEncrypt(localKey, EncryptEntry(plainContact.sharingMode)),
            consultedAt = clock.now(),
            safeId = bubblesSafeRepository.currentSafeId(),
            encResetConversationDate = null,
        )
        contactRepository.save(contact, localKey)
    }
}
