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
 * Created by Lunabee Studio / Date - 6/30/2023 - for the oneSafe6 SDK.
 * Last modified 30/06/2023 11:46
 */

package studio.lunabee.onesafe.messaging.domain.usecase

import studio.lunabee.doubleratchet.DoubleRatchetEngine
import studio.lunabee.doubleratchet.crypto.DoubleRatchetKeyRepository
import studio.lunabee.doubleratchet.model.DRPublicKey
import studio.lunabee.doubleratchet.model.DRSharedSecret
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.bubbles.domain.model.PlainContact
import studio.lunabee.onesafe.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.onesafe.bubbles.domain.usecase.CreateContactUseCase
import studio.lunabee.onesafe.domain.common.ItemIdProvider
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.messagecompanion.OSMessage
import studio.lunabee.onesafe.messaging.domain.model.HandShakeData
import studio.lunabee.onesafe.messaging.domain.repository.HandShakeDataRepository
import java.util.UUID
import javax.inject.Inject

// TODO move logic to KMM lib
class AcceptInvitationUseCase @Inject constructor(
    private val doubleRatchetEngine: DoubleRatchetEngine,
    private val doubleRatchetKeyRepository: DoubleRatchetKeyRepository,
    private val createContactUseCase: CreateContactUseCase,
    private val handShakeDataRepository: HandShakeDataRepository,
    private val randomIdProvider: ItemIdProvider,
    private val bubblesCryptoRepository: BubblesCryptoRepository,
) {

    /**
     * Create a conversation for double ratchet message exchange
     * Create a buble contact with id corresponding to the conversation
     * @return the id of the contact created
     */
    suspend operator fun invoke(contactName: String, isUsingDeeplink: Boolean, invitationMessage: ByteArray): UUID {
        val invitationMessageProto = try {
            OSMessage.InvitationMessage.parseFrom(invitationMessage)
        } catch (e: Exception) {
            throw OSDomainError(OSDomainError.Code.NOT_AN_INVITATION_MESSAGE)
        }
        val contactId = randomIdProvider.invoke()
        val keyPair = doubleRatchetKeyRepository.generateKeyPair()
        val sharedSecretKey = doubleRatchetKeyRepository.createDiffieHellmanSharedSecret(
            DRPublicKey(invitationMessageProto.oneSafePublicKey.toByteArray()),
            keyPair.privateKey,
        )
        createContactUseCase(
            PlainContact(
                id = contactId,
                name = contactName,
                sharedKey = sharedSecretKey.value,
                sharedConversationId = UUID.fromString(invitationMessageProto.conversationId),
                isUsingDeepLink = isUsingDeeplink,
            ),
        )
        val sharedSalt = bubblesCryptoRepository.deriveUUIDToKey(
            UUID.fromString(invitationMessageProto.conversationId),
            doubleRatchetKeyRepository.rootKeyByteSize,
        )
        doubleRatchetEngine.createNewConversationFromInvitation(
            sharedSalt = DRSharedSecret(sharedSalt),
            contactPublicKey = DRPublicKey(invitationMessageProto.doubleRatchetPublicKey.toByteArray()),
            newConversationId = DoubleRatchetUUID(contactId),
        )

        val handShakeData = HandShakeData(
            conversationLocalId = contactId,
            oneSafePublicKey = keyPair.publicKey.value,
            oneSafePrivateKey = null,
            conversationSharedId = UUID.fromString(invitationMessageProto.conversationId),
        )

        handShakeDataRepository.insert(handShakeData)
        return contactId
    }
}
