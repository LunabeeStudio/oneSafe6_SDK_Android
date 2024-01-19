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
 * Created by Lunabee Studio / Date - 7/13/2023 - for the oneSafe6 SDK.
 * Last modified 13/07/2023 13:37
 */

package studio.lunabee.onesafe.messaging.domain.usecase

import com.google.protobuf.kotlin.toByteString
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.storage.DoubleRatchetLocalDatasource
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.messagecompanion.invitationMessage
import studio.lunabee.onesafe.messaging.domain.repository.HandShakeDataRepository
import java.util.UUID
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class GetInvitationMessageUseCase @Inject constructor(
    private val handShakeDataRepository: HandShakeDataRepository,
    private val doubleRatchetLocalDatasource: DoubleRatchetLocalDatasource,
) {

    @OptIn(ExperimentalEncodingApi::class)
    suspend operator fun invoke(contactId: UUID): String {
        val handShakeData = handShakeDataRepository.getById(conversationLocalId = contactId)
            ?: throw OSDomainError(OSDomainError.Code.HAND_SHAKE_DATA_NOT_FOUND)
        val conversation = doubleRatchetLocalDatasource.getConversation(DoubleRatchetUUID(contactId))
            ?: throw OSDomainError(OSDomainError.Code.NO_MATCHING_CONTACT)
        val invitationProto = invitationMessage {
            doubleRatchetPublicKey = conversation.personalKeyPair.publicKey.value.toByteString()
            oneSafePublicKey = handShakeData.oneSafePublicKey?.toByteString() ?: byteArrayOf().toByteString()
            conversationId = handShakeData.conversationSharedId.toString()
            recipientId = handShakeData.conversationLocalId.toString()
        }
        return Base64.encode(invitationProto.toByteArray())
    }
}
