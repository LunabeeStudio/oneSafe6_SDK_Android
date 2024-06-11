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
 * Created by Lunabee Studio / Date - 6/3/2024 - for the oneSafe6 SDK.
 * Last modified 6/3/24, 11:59 PM
 */

package studio.lunabee.onesafe.messaging.domain.usecase

import com.lunabee.lbcore.model.LBResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import studio.lunabee.onesafe.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSError.Companion.get
import studio.lunabee.onesafe.messaging.domain.MessagingConstant
import studio.lunabee.onesafe.messaging.domain.model.MessageDirection
import studio.lunabee.onesafe.messaging.domain.model.PlainMessageData
import studio.lunabee.onesafe.messaging.domain.model.SafeMessage
import studio.lunabee.onesafe.test.assertFailure
import studio.lunabee.onesafe.test.assertSuccess
import studio.lunabee.onesafe.test.testUUIDs
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DecryptSafeMessageUseCaseTest {
    private val contactId: UUID = testUUIDs[1]
    private val message = SafeMessage(
        id = testUUIDs[0],
        fromContactId = contactId,
        encSentAt = byteArrayOf(0),
        encContent = byteArrayOf(1),
        direction = MessageDirection.RECEIVED,
        encChannel = byteArrayOf(2),
        isRead = false,
    )

    private val contactLocalDecryptUseCase: ContactLocalDecryptUseCase = mockk {
        coEvery { this@mockk(byteArrayOf(0), contactId, Instant::class) } returns LBResult.Success(Instant.EPOCH)
        coEvery { this@mockk(byteArrayOf(1), contactId, String::class) } returns LBResult.Success("content")
        coEvery { this@mockk(byteArrayOf(2), contactId, String::class) } returns LBResult.Success("channel")
    }

    private val decryptSafeMessageUseCase: DecryptSafeMessageUseCase = DecryptSafeMessageUseCase(
        contactLocalDecryptUseCase,
    )

    @Test
    fun message_default_test(): TestResult = runTest {
        val message = decryptSafeMessageUseCase.message(message)
        assertIs<PlainMessageData.Default>(message)
        val actualContent = assertSuccess(message.content).successData
        assertEquals("content", actualContent)
        val actualChannel = assertSuccess(message.channel).successData
        assertEquals("channel", actualChannel)
        val actualSentAt = assertSuccess(message.sentAt).successData
        assertEquals(Instant.EPOCH, actualSentAt)
        assertFalse(message.hasCorruptedData)
    }

    @Test
    fun message_default_corrupted_test(): TestResult = runTest {
        val expectedError = OSCryptoError.Code.BUBBLES_CONTACT_KEY_DECRYPTION_FAIL.get()
        coEvery { contactLocalDecryptUseCase(byteArrayOf(1), contactId, String::class) } returns LBResult.Failure(expectedError)

        val message = decryptSafeMessageUseCase.message(message)
        assertIs<PlainMessageData.Default>(message)
        val actualError = assertFailure(message.content).throwable
        assertEquals(expectedError, actualError)
        val actualChannel = assertSuccess(message.channel).successData
        assertEquals("channel", actualChannel)
        val actualSentAt = assertSuccess(message.sentAt).successData
        assertEquals(Instant.EPOCH, actualSentAt)
        assertTrue(message.hasCorruptedData)
    }

    @Test
    fun message_invitation_test(): TestResult = runTest {
        coEvery {
            contactLocalDecryptUseCase(byteArrayOf(1), contactId, String::class)
        } returns LBResult.Success(MessagingConstant.FirstMessageData)

        val message = decryptSafeMessageUseCase.message(message)
        assertIs<PlainMessageData.AcceptedInvitation>(message)
        val actualChannel = assertSuccess(message.channel).successData
        assertEquals("channel", actualChannel)
        val actualSentAt = assertSuccess(message.sentAt).successData
        assertEquals(Instant.EPOCH, actualSentAt)
        assertFalse(message.hasCorruptedData)
    }
}
