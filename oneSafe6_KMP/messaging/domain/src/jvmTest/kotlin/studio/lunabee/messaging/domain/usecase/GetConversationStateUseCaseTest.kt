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

package studio.lunabee.messaging.domain.usecase

import com.lunabee.lbcore.model.LBResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import studio.lunabee.bubbles.domain.model.contact.Contact
import studio.lunabee.bubbles.domain.model.contactkey.ContactSharedKey
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.doubleratchet.model.Conversation
import studio.lunabee.doubleratchet.model.DRChainKey
import studio.lunabee.doubleratchet.model.createRandomUUID
import studio.lunabee.messaging.domain.bubblesAssertFailure
import studio.lunabee.messaging.domain.bubblesAssertSuccess
import studio.lunabee.messaging.domain.model.ConversationState
import studio.lunabee.messaging.domain.repository.ConversationRepository
import studio.lunabee.messaging.domain.testUUID
import studio.lunabee.onesafe.error.BubblesMessagingError
import kotlin.test.Test
import kotlin.test.assertEquals

class GetConversationStateUseCaseTest {

    private val conversationRepository: ConversationRepository = mockk {
        coEvery { getConversation(testUUID.first()) } returns null
    }

    private val getHandShakeDataUseCase: GetHandShakeDataUseCase = mockk {
        coEvery { this@mockk(testUUID.first()) } returns LBResult.Success(null)
    }

    private val contactRepository: ContactRepository = mockk {
        coEvery { getContact(testUUID.first()) } returns null
    }

    private val getConversationStateUseCase: GetConversationStateUseCase = GetConversationStateUseCase(
        conversationRepository = conversationRepository,
        getHandShakeDataUseCase = getHandShakeDataUseCase,
        contactRepository = contactRepository,
    )

    /**
     * Conversation does not exist
     */
    @Test
    fun no_conversation_error_test(): TestResult = runTest {
        val actual = getConversationStateUseCase(testUUID.first())
        val error = bubblesAssertFailure(actual).throwable
        assertEquals(BubblesMessagingError(BubblesMessagingError.Code.CONVERSATION_NOT_FOUND), error)
    }

    /**
     * Conversation has received message
     */
    @Test
    fun conversation_has_received_message_test(): TestResult = runTest {
        coEvery { conversationRepository.getConversation(testUUID.first()) } returns Conversation(
            id = testUUID.first(),
            personalKeyPair = mockk(),
            messageNumber = 1,
            sendingChainKey = DRChainKey(byteArrayOf()),
        )
        val actualRes = getConversationStateUseCase(testUUID.first())
        val actual = bubblesAssertSuccess(actualRes).successData
        assertEquals(ConversationState.Running, actual)
    }

    /**
     * Conversation does not have received message
     */
    @Test
    fun conversation_setup_but_no_message_test(): TestResult = runTest {
        coEvery { conversationRepository.getConversation(testUUID.first()) } returns Conversation(
            id = testUUID.first(),
            personalKeyPair = mockk(),
            messageNumber = 0,
            sendingChainKey = DRChainKey(byteArrayOf()),
        )
        val actualRes = getConversationStateUseCase(testUUID.first())
        val actual = bubblesAssertSuccess(actualRes).successData
        assertEquals(ConversationState.FullySetup, actual)
    }

    /**
     * Conversation has a sending key but did not received message yet
     */
    @Test
    fun conversation_has_done_handshake_test(): TestResult = runTest {
        coEvery { conversationRepository.getConversation(testUUID.first()) } returns Conversation(
            id = testUUID.first(),
            personalKeyPair = mockk(),
            receivedLastMessageNumber = 0,
            sendingChainKey = DRChainKey(byteArrayOf()),
        )
        coEvery { getHandShakeDataUseCase(testUUID.first()) } returns LBResult.Success(mockk())

        val actualRes = getConversationStateUseCase(testUUID.first())
        val actual = bubblesAssertSuccess(actualRes).successData
        assertEquals(ConversationState.WaitingForFirstMessage, actual)
    }

    /**
     * Conversation does not have a sending key
     */
    @Test
    fun conversation_wait_reply_test(): TestResult = runTest {
        coEvery { conversationRepository.getConversation(testUUID.first()) } returns Conversation(
            id = testUUID.first(),
            personalKeyPair = mockk(),
        )

        val actualRes = getConversationStateUseCase(testUUID.first())
        val actual = bubblesAssertSuccess(actualRes).successData
        assertEquals(ConversationState.WaitingForReply, actual)
    }

    /**
     * Conversation does not have sending key but have bubbles crypto
     */
    @Test
    fun conversation_reset_test(): TestResult = runTest {
        coEvery { conversationRepository.getConversation(testUUID.first()) } returns Conversation(
            id = testUUID.first(),
            personalKeyPair = mockk(),
            sendingChainKey = null,
        )
        coEvery { contactRepository.getContact(testUUID.first()) } returns Contact(
            id = testUUID.first(),
            encName = byteArrayOf(),
            encSharedKey = ContactSharedKey(byteArrayOf()),
            updatedAt = Clock.System.now(),
            encSharingMode = byteArrayOf(),
            sharedConversationId = createRandomUUID(),
            consultedAt = Clock.System.now(),
            safeId = createRandomUUID(),
            encResetConversationDate = byteArrayOf(),
        )
        val actualRes = getConversationStateUseCase(testUUID.first())
        val actual = bubblesAssertSuccess(actualRes).successData
        assertEquals(ConversationState.Reset, actual)
    }
}
