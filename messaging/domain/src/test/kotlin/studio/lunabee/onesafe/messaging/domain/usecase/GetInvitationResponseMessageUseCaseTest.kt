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
 * Created by Lunabee Studio / Date - 6/4/2024 - for the oneSafe6 SDK.
 * Last modified 6/4/24, 10:32 AM
 */

package studio.lunabee.onesafe.messaging.domain.usecase

import com.lunabee.lbcore.model.LBResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import studio.lunabee.doubleratchet.model.DRMessageKey
import studio.lunabee.doubleratchet.model.DRPublicKey
import studio.lunabee.doubleratchet.model.MessageHeader
import studio.lunabee.doubleratchet.model.SendMessageData
import studio.lunabee.onesafe.messaging.domain.MessagingConstant
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.assertSuccess
import studio.lunabee.onesafe.test.testUUIDs
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class GetInvitationResponseMessageUseCaseTest {

    private val publicKey = DRPublicKey(byteArrayOf(0))
    private val messageNumber = 0
    private val sequenceNumber = 0
    private val messageHeader = MessageHeader(publicKey, messageNumber, sequenceNumber)
    private val messageKey = DRMessageKey(byteArrayOf(1))
    private val contactId = testUUIDs[0]
    private val sendMessageData = SendMessageData(messageHeader, messageKey)

    private val getSendMessageDataUseCase: GetSendMessageDataUseCase = mockk {
        coEvery { this@mockk(contactId) } returns LBResult.Success(sendMessageData)
    }

    private val sentAt = Instant.now(OSTestConfig.clock)

    private val encryptMessageUseCase: EncryptMessageUseCase = mockk {
        coEvery {
            this@mockk(
                plainMessage = MessagingConstant.FirstMessageData,
                contactId = contactId,
                sentAt = sentAt,
                sendMessageData = any(),
            )
        } returns LBResult.Success("encoded_message")
    }
    private val saveSentMessageUseCase: SaveSentMessageUseCase = mockk {
        coEvery { this@mockk(any(), any(), any(), any(), any()) } returns LBResult.Success(null)
    }

    private val getInvitationResponseMessageUseCase: GetInvitationResponseMessageUseCase = GetInvitationResponseMessageUseCase(
        getSendMessageDataUseCase = getSendMessageDataUseCase,
        encryptMessageUseCase = encryptMessageUseCase,
        saveSentMessageUseCase = saveSentMessageUseCase,
        clock = OSTestConfig.clock,
    )

    @Test
    fun generate_invitation_once_test(): TestResult = runTest {
        val invitationRes: LBResult<String> = getInvitationResponseMessageUseCase(contactId)
        val invitation = assertSuccess(invitationRes).successData
        assertEquals("encoded_message", invitation)
        coVerify(exactly = 1) { saveSentMessageUseCase(any(), any(), any(), any(), any()) }
    }

    @Test
    fun generate_invitation_twice_test(): TestResult = runTest {
        coEvery { getSendMessageDataUseCase(contactId) } returns LBResult.Success(
            SendMessageData(
                messageHeader = MessageHeader(
                    publicKey = publicKey,
                    messageNumber = 1,
                    sequenceNumber = sequenceNumber,
                ),
                messageKey = messageKey,
            ),
        )

        val invitationRes: LBResult<String> = getInvitationResponseMessageUseCase(contactId)
        val invitation = assertSuccess(invitationRes).successData
        assertEquals("encoded_message", invitation)
        coVerify(exactly = 0) { saveSentMessageUseCase(any(), any(), any(), any(), any()) }
    }
}
