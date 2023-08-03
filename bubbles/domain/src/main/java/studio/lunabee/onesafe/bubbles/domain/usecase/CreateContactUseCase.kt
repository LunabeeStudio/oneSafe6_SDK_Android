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

package studio.lunabee.onesafe.bubbles.domain.usecase

import studio.lunabee.onesafe.bubbles.domain.model.Contact
import studio.lunabee.onesafe.bubbles.domain.model.ContactLocalKey
import studio.lunabee.onesafe.bubbles.domain.model.ContactSharedKey
import studio.lunabee.onesafe.bubbles.domain.model.PlainContact
import studio.lunabee.onesafe.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.onesafe.bubbles.domain.repository.ContactRepository
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import java.time.Instant
import javax.inject.Inject
import kotlin.io.encoding.ExperimentalEncodingApi

class CreateContactUseCase @Inject constructor(
    private val contactRepository: ContactRepository,
    private val bubblesCryptoRepository: BubblesCryptoRepository,
) {
    @OptIn(ExperimentalEncodingApi::class)
    suspend operator fun invoke(plainContact: PlainContact) {
        val localKey: ContactLocalKey = bubblesCryptoRepository.generateLocalKeyForContact()
        val encSharedKey = plainContact.sharedKey?.let {
            ContactSharedKey(bubblesCryptoRepository.localEncrypt(localKey, EncryptEntry(it)))
        }
        val contact = Contact(
            id = plainContact.id,
            encName = bubblesCryptoRepository.localEncrypt(localKey, EncryptEntry(plainContact.name)),
            encSharedKey = encSharedKey,
            updatedAt = Instant.now(),
            sharedConversationId = plainContact.sharedConversationId,
            encIsUsingDeeplink = bubblesCryptoRepository.localEncrypt(localKey, EncryptEntry(plainContact.isUsingDeepLink)),
        )
        contactRepository.save(contact, localKey)
    }
}
