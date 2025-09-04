/*
 * Copyright (c) 2024-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 8/29/2024 - for the oneSafe6 SDK.
 * Last modified 29/08/2024 09:23
 */

package studio.lunabee.messaging.domain.usecase

import kotlin.time.Clock
import studio.lunabee.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.bubbles.domain.repository.BubblesSafeRepository
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.doubleratchet.DoubleRatchetEngine
import studio.lunabee.doubleratchet.crypto.DoubleRatchetKeyRepository
import studio.lunabee.doubleratchet.model.AsymmetricKeyPair
import studio.lunabee.doubleratchet.model.DRSharedSecret
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.model.createRandomUUID
import studio.lunabee.messaging.domain.MessagingConstant
import studio.lunabee.messaging.domain.model.HandShakeData
import studio.lunabee.messaging.domain.model.SharedMessage
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.onesafe.error.BubblesDomainError

class ResetConversationUseCase @Inject constructor(
    private val doubleRatchetEngine: DoubleRatchetEngine,
    private val bubblesCryptoRepository: BubblesCryptoRepository,
    private val contactRepository: ContactRepository,
    private val safeRepository: BubblesSafeRepository,
    private val doubleRatchetKeyRepository: DoubleRatchetKeyRepository,
    private val insertHandShakeDataUseCase: InsertHandShakeDataUseCase,
    private val saveMessageUseCase: SaveMessageUseCase,
    private val clock: Clock,
    private val updateContactResetConversationDateUseCase: UpdateContactResetConversationDateUseCase,
) {

    /**
     * Reset double ratchet data from a conversation.
     * - Use `DoubleRatchetEngine.createInvitation` to reset from scratch
     * - Update the contact's reset conversation date to clock.now()
     * - Save the reset indication message.
     * - Insert the new hand shake data to allow reset message construction
     */
    suspend operator fun invoke(conversationId: DoubleRatchetUUID) {
        val keyPair: AsymmetricKeyPair = doubleRatchetKeyRepository.generateKeyPair()
        val contact = contactRepository.getContact(conversationId)
            ?: throw BubblesDomainError(BubblesDomainError.Code.NO_MATCHING_CONTACT)
        val sharedSalt = bubblesCryptoRepository.deriveUUIDToKey(
            contact.sharedConversationId,
            doubleRatchetKeyRepository.rootKeyByteSize,
        )
        doubleRatchetEngine.createInvitation(
            sharedSalt = DRSharedSecret(sharedSalt),
            newConversationId = conversationId,
        )
        val now = clock.now()
        updateContactResetConversationDateUseCase(conversationId, now)
        saveMessageUseCase(
            plainMessage = SharedMessage(
                content = MessagingConstant.ResetConversationMessageData,
                recipientId = conversationId,
                date = now,
            ),
            contactId = conversationId,
            channel = null,
            id = createRandomUUID(),
            safeItemId = safeRepository.currentSafeId(),
        )
        val handShakeData = HandShakeData(
            conversationLocalId = conversationId,
            conversationSharedId = contact.sharedConversationId,
            oneSafePrivateKey = keyPair.privateKey.value,
            oneSafePublicKey = keyPair.publicKey.value,
        )
        insertHandShakeDataUseCase(handShakeData)
    }
}
