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
 * Created by Lunabee Studio / Date - 9/20/2023 - for the oneSafe6 SDK.
 * Last modified 9/20/23, 11:27 AM
 */

package studio.lunabee.onesafe.messaging.domain

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import studio.lunabee.doubleratchet.model.Conversation
import studio.lunabee.doubleratchet.model.DRChainKey
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.messaging.domain.model.ConversationState
import studio.lunabee.onesafe.messaging.domain.repository.ConversationRepository
import studio.lunabee.onesafe.messaging.domain.repository.HandShakeDataRepository
import studio.lunabee.onesafe.messaging.domain.usecase.GetConversationStateUseCase
import studio.lunabee.onesafe.test.testUUIDs
import kotlin.test.assertEquals

class GetConversationStateUseCaseTest {

    private val handShakeDataRepository: HandShakeDataRepository = mockk {
        coEvery { getById(testUUIDs.first()) } returns null
    }

    private val conversationRepository: ConversationRepository = mockk {
        coEvery { getConversation(testUUIDs.first()) } returns null
    }

    private val getConversationStateUseCase: GetConversationStateUseCase = GetConversationStateUseCase(
        handShakeDataRepository = handShakeDataRepository,
        conversationRepository = conversationRepository,
    )

    /**
     * Conversation does not exist
     */
    @Test
    fun no_conversation_error_test(): TestResult = runTest {
        val actual = getConversationStateUseCase(testUUIDs.first())
        assertEquals(ConversationState.Error, actual)
    }

    /**
     * Conversation has received message
     */
    @Test
    fun conversation_has_received_message_test(): TestResult = runTest {
        coEvery { conversationRepository.getConversation(testUUIDs.first()) } returns Conversation(
            id = DoubleRatchetUUID(testUUIDs.first()),
            personalKeyPair = mockk(),
            messageNumber = 1,
            sendingChainKey = DRChainKey(byteArrayOf()),
        )
        val actual = getConversationStateUseCase(testUUIDs.first())
        assertEquals(ConversationState.Running, actual)
    }

    /**
     * Conversation does not have received message
     */
    @Test
    fun conversation_setup_but_no_message_test(): TestResult = runTest {
        coEvery { conversationRepository.getConversation(testUUIDs.first()) } returns Conversation(
            id = DoubleRatchetUUID(testUUIDs.first()),
            personalKeyPair = mockk(),
            messageNumber = 0,
            sendingChainKey = DRChainKey(byteArrayOf()),
        )
        val actual = getConversationStateUseCase(testUUIDs.first())
        assertEquals(ConversationState.FullySetup, actual)
    }

    /**
     * Conversation has a sending key but did not received message yet
     */
    @Test
    fun conversation_has_done_handshake_test(): TestResult = runTest {
        coEvery { conversationRepository.getConversation(testUUIDs.first()) } returns Conversation(
            id = DoubleRatchetUUID(testUUIDs.first()),
            personalKeyPair = mockk(),
            receivedLastMessageNumber = 0,
            sendingChainKey = DRChainKey(byteArrayOf()),
        )
        coEvery { handShakeDataRepository.getById(testUUIDs.first()) } returns mockk()

        val actual = getConversationStateUseCase(testUUIDs.first())
        assertEquals(ConversationState.WaitingForFirstMessage, actual)
    }

    /**
     * Conversation does not have a sending key
     */
    @Test
    fun conversation_wait_reply_test(): TestResult = runTest {
        coEvery { conversationRepository.getConversation(testUUIDs.first()) } returns Conversation(
            id = DoubleRatchetUUID(testUUIDs.first()),
            personalKeyPair = mockk(),
        )

        val actual = getConversationStateUseCase(testUUIDs.first())
        assertEquals(ConversationState.WaitingForReply, actual)
    }
}
