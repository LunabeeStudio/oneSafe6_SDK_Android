/*
 * Copyright (c) 2023-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 6/3/2024 - for the oneSafe6 SDK.
 * Last modified 6/3/24, 11:53 PM
 */

package studio.lunabee.onesafe.messaging.domain.usecase

import com.lunabee.lbcore.model.LBResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import studio.lunabee.doubleratchet.model.Conversation
import studio.lunabee.doubleratchet.model.DRChainKey
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.error.OSMessagingError
import studio.lunabee.onesafe.messaging.domain.model.ConversationState
import studio.lunabee.onesafe.messaging.domain.repository.ConversationRepository
import studio.lunabee.onesafe.test.assertFailure
import studio.lunabee.onesafe.test.assertSuccess
import studio.lunabee.onesafe.test.testUUIDs
import kotlin.test.Test
import kotlin.test.assertEquals

class GetConversationStateUseCaseTest {

    private val conversationRepository: ConversationRepository = mockk {
        coEvery { getConversation(testUUIDs.first()) } returns null
    }

    private val getHandShakeDataUseCase: GetHandShakeDataUseCase = mockk {
        coEvery { this@mockk(testUUIDs.first()) } returns LBResult.Success(null)
    }

    private val getConversationStateUseCase: GetConversationStateUseCase = GetConversationStateUseCase(
        conversationRepository = conversationRepository,
        getHandShakeDataUseCase = getHandShakeDataUseCase,
    )

    /**
     * Conversation does not exist
     */
    @Test
    fun no_conversation_error_test(): TestResult = runTest {
        val actual = getConversationStateUseCase(testUUIDs.first())
        val error = assertFailure(actual).throwable
        assertEquals(OSMessagingError(OSMessagingError.Code.CONVERSATION_NOT_FOUND), error)
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
        val actualRes = getConversationStateUseCase(testUUIDs.first())
        val actual = assertSuccess(actualRes).successData
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
        val actualRes = getConversationStateUseCase(testUUIDs.first())
        val actual = assertSuccess(actualRes).successData
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
        coEvery { getHandShakeDataUseCase(testUUIDs.first()) } returns LBResult.Success(mockk())

        val actualRes = getConversationStateUseCase(testUUIDs.first())
        val actual = assertSuccess(actualRes).successData
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

        val actualRes = getConversationStateUseCase(testUUIDs.first())
        val actual = assertSuccess(actualRes).successData
        assertEquals(ConversationState.WaitingForReply, actual)
    }
}
