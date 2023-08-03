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
 * Created by Lunabee Studio / Date - 6/29/2023 - for the oneSafe6 SDK.
 * Last modified 29/06/2023 16:31
 */

package studio.lunabee.onesafe.messaging.domain.usecase

import studio.lunabee.doubleratchet.DoubleRatchetEngine
import studio.lunabee.doubleratchet.crypto.DoubleRatchetKeyRepository
import studio.lunabee.doubleratchet.model.AsymmetricKeyPair
import studio.lunabee.doubleratchet.model.DRSharedSecret
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.bubbles.domain.model.PlainContact
import studio.lunabee.onesafe.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.onesafe.bubbles.domain.usecase.CreateContactUseCase
import studio.lunabee.onesafe.domain.common.ItemIdProvider

// TODO move logic to KMM lib
import studio.lunabee.onesafe.messaging.domain.model.HandShakeData
import studio.lunabee.onesafe.messaging.domain.repository.HandShakeDataRepository
import java.util.UUID
import javax.inject.Inject
import kotlin.io.encoding.ExperimentalEncodingApi

class CreateInvitationUseCase @Inject constructor(
    private val doubleRatchetEngine: DoubleRatchetEngine,
    private val doubleRatchetKeyRepository: DoubleRatchetKeyRepository,
    private val createContactUseCase: CreateContactUseCase,
    private val randomIdProvider: ItemIdProvider,
    private val handShakeDataRepository: HandShakeDataRepository,
    private val bubblesCryptoRepository: BubblesCryptoRepository,
) {

    /**
     * Create a conversation for double ratchet message exchange
     * Create and save handShakeData required to establish the symmetric encryption
     * Create a bubble contact with id corresponding to the conversation
     *
     * @return the message string to send
     */
    @OptIn(ExperimentalEncodingApi::class)
    suspend operator fun invoke(contactName: String, isUsingDeepLink: Boolean): UUID {
        val keyPair: AsymmetricKeyPair = doubleRatchetKeyRepository.generateKeyPair()
        val contactId = randomIdProvider.invoke()
        val sharedConversationId = randomIdProvider.invoke()
        createContactUseCase(
            PlainContact(
                id = contactId,
                name = contactName,
                // Not created yet
                sharedKey = null,
                sharedConversationId = sharedConversationId,
                isUsingDeepLink = isUsingDeepLink,
            ),
        )
        val sharedSalt = bubblesCryptoRepository.deriveUUIDToKey(
            sharedConversationId,
            doubleRatchetKeyRepository.rootKeyByteSize,
        )
        doubleRatchetEngine.createInvitation(
            sharedSalt = DRSharedSecret(sharedSalt),
            newConversationId = DoubleRatchetUUID(contactId),
        )
        val handShakeData = HandShakeData(
            conversationLocalId = contactId,
            conversationSharedId = sharedConversationId,
            oneSafePrivateKey = keyPair.privateKey.value,
            oneSafePublicKey = keyPair.publicKey.value,
        )
        handShakeDataRepository.insert(handShakeData)
        return contactId
    }
}
