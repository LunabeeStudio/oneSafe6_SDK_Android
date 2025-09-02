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

package studio.lunabee.messaging.domain.usecase

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.error.BubblesMessagingError
import studio.lunabee.doubleratchet.model.Conversation
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.model.ConversationState
import studio.lunabee.messaging.domain.repository.ConversationRepository

class GetConversationStateUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val getHandShakeDataUseCase: GetHandShakeDataUseCase,
    private val contactRepository: ContactRepository,
) {
    suspend operator fun invoke(id: DoubleRatchetUUID): LBResult<ConversationState> = OSError.runCatching {
        val conversation: Conversation? = conversationRepository.getConversation(id)
        val contact = contactRepository.getContact(id)
        when {
            // Conversation does not exist
            conversation == null -> throw BubblesMessagingError(BubblesMessagingError.Code.CONVERSATION_NOT_FOUND)
            // Conversation has bubbles key but double ratchet crypto is not initialized
            conversation.sendingChainKey == null && contact?.encSharedKey != null -> ConversationState.Reset
            // Conversation does not have a sending key
            conversation.sendingChainKey == null -> ConversationState.WaitingForReply
            // Conversation has a sending key but still have handshake data
            getHandShakeDataUseCase(id)
                .getOrThrow("Unable to get handshake data") != null -> ConversationState.WaitingForFirstMessage
            // Conversation is setup but you never sent a message
            conversation.nextMessageNumber == 0 -> ConversationState.FullySetup
            // Conversation has sent at least one message
            conversation.nextMessageNumber > 0 -> ConversationState.Running
            else -> throw BubblesMessagingError(BubblesMessagingError.Code.UNKNOWN_ERROR)
        }
    }
}
