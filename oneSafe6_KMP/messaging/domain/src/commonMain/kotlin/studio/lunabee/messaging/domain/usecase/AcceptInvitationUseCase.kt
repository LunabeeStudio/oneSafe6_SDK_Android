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

package studio.lunabee.messaging.domain.usecase

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import studio.lunabee.bubbles.domain.model.MessageSharingMode
import studio.lunabee.bubbles.domain.model.contact.PlainContact
import studio.lunabee.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.bubbles.domain.usecase.CreateContactUseCase
import studio.lunabee.doubleratchet.DoubleRatchetEngine
import studio.lunabee.doubleratchet.crypto.DoubleRatchetKeyRepository
import studio.lunabee.doubleratchet.model.DRPublicKey
import studio.lunabee.doubleratchet.model.DRSharedSecret
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.model.createRandomUUID
import studio.lunabee.messaging.domain.model.HandShakeData
import studio.lunabee.messaging.domain.model.proto.ProtoInvitationMessage
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.onesafe.error.BubblesDomainError

class AcceptInvitationUseCase @Inject constructor(
    private val doubleRatchetEngine: DoubleRatchetEngine,
    private val doubleRatchetKeyRepository: DoubleRatchetKeyRepository,
    private val createContactUseCase: CreateContactUseCase,
    private val bubblesCryptoRepository: BubblesCryptoRepository,
    private val insertHandShakeDataUseCase: InsertHandShakeDataUseCase,
) {

    /**
     * Create a conversation for double ratchet message exchange
     * Create a bubbles contact with id corresponding to the conversation
     * @return the id of the contact created
     */
    @OptIn(ExperimentalSerializationApi::class)
    suspend operator fun invoke(contactName: String, sharingMode: MessageSharingMode, invitationMessage: ByteArray): DoubleRatchetUUID {
        val invitationMessageProto = try {
            ProtoBuf.decodeFromByteArray<ProtoInvitationMessage>(invitationMessage)
        } catch (e: Exception) {
            throw BubblesDomainError(BubblesDomainError.Code.NOT_AN_INVITATION_MESSAGE)
        } catch (e: IllegalArgumentException) {
            throw BubblesDomainError(BubblesDomainError.Code.NOT_AN_INVITATION_MESSAGE)
        }
        val contactId = createRandomUUID()
        val keyPair = doubleRatchetKeyRepository.generateKeyPair()
        val sharedSecretKey = doubleRatchetKeyRepository.createDiffieHellmanSharedSecret(
            DRPublicKey(invitationMessageProto.oneSafePublicKey),
            keyPair.privateKey,
        )
        createContactUseCase(
            PlainContact(
                id = contactId,
                name = contactName,
                sharedKey = sharedSecretKey.value,
                sharedConversationId = DoubleRatchetUUID.fromString(invitationMessageProto.conversationId),
                sharingMode = sharingMode,
            ),
        )
        val sharedSalt = bubblesCryptoRepository.deriveUUIDToKey(
            DoubleRatchetUUID.fromString(invitationMessageProto.conversationId),
            doubleRatchetKeyRepository.rootKeyByteSize,
        )
        doubleRatchetEngine.createNewConversationFromInvitation(
            sharedSalt = DRSharedSecret(sharedSalt),
            contactPublicKey = DRPublicKey(invitationMessageProto.doubleRatchetPublicKey),
            newConversationId = contactId,
        )

        val handShakeData = HandShakeData(
            conversationLocalId = contactId,
            oneSafePublicKey = keyPair.publicKey.value,
            oneSafePrivateKey = null,
            conversationSharedId = DoubleRatchetUUID.fromString(invitationMessageProto.conversationId),
        )
        insertHandShakeDataUseCase(handShakeData)
        return contactId
    }
}
