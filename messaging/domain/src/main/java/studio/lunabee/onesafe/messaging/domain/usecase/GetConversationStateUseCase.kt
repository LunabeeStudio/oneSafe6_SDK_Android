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
 * Created by Lunabee Studio / Date - 7/17/2023 - for the oneSafe6 SDK.
 * Last modified 17/07/2023 16:34
 */

package studio.lunabee.onesafe.messaging.domain.usecase

import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import studio.lunabee.doubleratchet.model.Conversation
import studio.lunabee.onesafe.messaging.domain.model.ConversationState
import studio.lunabee.onesafe.messaging.domain.model.HandShakeData
import studio.lunabee.onesafe.messaging.domain.repository.ConversationRepository
import studio.lunabee.onesafe.messaging.domain.repository.HandShakeDataRepository
import java.util.UUID
import javax.inject.Inject

private val log = LBLogger.get<GetConversationStateUseCase>()

class GetConversationStateUseCase @Inject constructor(
    private val handShakeDataRepository: HandShakeDataRepository,
    private val conversationRepository: ConversationRepository,
) {
    suspend operator fun invoke(contactId: UUID): ConversationState {
        val handShakeData: HandShakeData? = handShakeDataRepository.getById(contactId)
        val conversation: Conversation? = conversationRepository.getConversation(contactId)
        val nextMessageNumber = conversation?.nextMessageNumber
        val hasSendingKey = conversation?.sendingChainKey != null
        return when {
            // Conversation does not exist
            conversation == null -> ConversationState.Error
            // Conversation does not have a sending key
            !hasSendingKey -> ConversationState.WaitingForReply
            // Conversation has a sending key but still have handshake data
            hasSendingKey && handShakeData != null -> ConversationState.WaitingForFirstMessage
            // Conversation is setup but you never sent a message
            nextMessageNumber == 0 -> ConversationState.FullySetup
            // Conversation has sent at least one message
            nextMessageNumber!! > 0 -> ConversationState.Running
            else -> {
                log.e("Unexpected conversation error state")
                ConversationState.Error
            }
        }
    }
}
