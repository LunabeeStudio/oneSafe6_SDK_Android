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
 * Created by Lunabee Studio / Date - 8/29/2024 - for the oneSafe6 SDK.
 * Last modified 29/08/2024 15:52
 */

package studio.lunabee.messaging.domain.usecase

import com.lunabee.lbcore.model.LBResult
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import studio.lunabee.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.storage.DoubleRatchetLocalDatasource
import studio.lunabee.messaging.domain.model.proto.ProtoResetInvitationMessage
import studio.lunabee.messaging.domain.model.proto.ProtoTimestamp
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.onesafe.error.BubblesDomainError
import studio.lunabee.onesafe.error.BubblesMessagingError
import studio.lunabee.onesafe.error.OSError
import kotlin.time.Instant

class GetResetMessageUseCase @Inject constructor(
    private val doubleRatchetLocalDatasource: DoubleRatchetLocalDatasource,
    private val getHandShakeDataUseCase: GetHandShakeDataUseCase,
    private val contactRepository: ContactRepository,
    private val contactKeyRepository: ContactKeyRepository,
    private val bubblesCryptoRepository: BubblesCryptoRepository,
    private val contactLocalDecryptUseCase: ContactLocalDecryptUseCase,
) {

    /**
     * Used AFTER a conversation reset
     * use the existing bubbles crypto to encrypt a `ProtoResetInvitationMessage` containing the new doubleRatchet data
     */
    @OptIn(ExperimentalSerializationApi::class)
    suspend operator fun invoke(contactId: DoubleRatchetUUID): LBResult<ByteArray> = OSError.runCatching {
        val contact = contactRepository.getContact(contactId)
        val sharedKey = contactRepository.getSharedKey(contactId)
            ?: throw BubblesMessagingError(BubblesMessagingError.Code.CONTACT_NOT_FOUND)
        val localKey = contactKeyRepository.getContactLocalKey(contactId)
        val handShakeData = getHandShakeDataUseCase(conversationLocalId = contactId).getOrThrow()
            ?: throw BubblesMessagingError(BubblesMessagingError.Code.HANDSHAKE_DATA_NOT_FOUND)
        val conversation = doubleRatchetLocalDatasource.getConversation(contactId)
            ?: throw BubblesDomainError(BubblesDomainError.Code.NO_MATCHING_CONTACT)
        val contactResetDate = contact?.encResetConversationDate?.let {
            contactLocalDecryptUseCase.invoke(it, contactId, Instant::class).data
        } ?: Instant.DISTANT_PAST
        val resetMessageProto = ProtoResetInvitationMessage(
            doubleRatchetPublicKey = conversation.personalKeyPair.publicKey.value,
            conversationId = handShakeData.conversationSharedId.uuidString(),
            recipientId = handShakeData.conversationLocalId.uuidString(),
            conversationResetDate = ProtoTimestamp.fromInstant(contactResetDate),
        )
        val messageByteArray = ProtoBuf.encodeToByteArray(resetMessageProto)
        bubblesCryptoRepository.sharedEncrypt(messageByteArray, localKey, sharedKey)
    }
}
