/*
 * Copyright (c) 2024-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 7/17/2024 - for the oneSafe6 SDK.
 * Last modified 17/07/2024 11:49
 */

package studio.lunabee.messaging.domain.usecase

import com.lunabee.lbcore.model.LBResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import studio.lunabee.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.model.createRandomUUID
import studio.lunabee.messaging.domain.MessagingConstant
import studio.lunabee.messaging.domain.bubblesAssertFailure
import studio.lunabee.messaging.domain.bubblesAssertSuccess
import studio.lunabee.messaging.domain.model.MessageDirection
import studio.lunabee.messaging.domain.model.PlainMessageData
import studio.lunabee.messaging.domain.model.SafeMessage
import studio.lunabee.messaging.domain.testUUID
import studio.lunabee.onesafe.error.BubblesCryptoError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant

class DecryptSafeMessageUseCaseTest {
    private val contactId: DoubleRatchetUUID = testUUID[1]
    private val message = SafeMessage(
        id = testUUID[0],
        fromContactId = contactId,
        encSentAt = byteArrayOf(0),
        encContent = byteArrayOf(1),
        direction = MessageDirection.RECEIVED,
        encChannel = byteArrayOf(2),
        isRead = false,
        encSafeItemId = null,
    )
    val safeItemId = createRandomUUID()
    private val itemMessage = SafeMessage(
        id = testUUID[0],
        fromContactId = contactId,
        encSentAt = byteArrayOf(0),
        encContent = byteArrayOf(3),
        direction = MessageDirection.RECEIVED,
        encChannel = byteArrayOf(2),
        isRead = false,
        encSafeItemId = byteArrayOf(4),
    )

    private val contactLocalDecryptUseCase: ContactLocalDecryptUseCase = mockk {
        coEvery {
            this@mockk(byteArrayOf(0), contactId, Instant::class)
        } returns LBResult.Success<Instant>(Instant.fromEpochSeconds(0))
        coEvery { this@mockk(byteArrayOf(1), contactId, String::class) } returns LBResult.Success("content")
        coEvery { this@mockk(byteArrayOf(2), contactId, String::class) } returns LBResult.Success<String>("channel")
        coEvery {
            this@mockk(byteArrayOf(3), contactId, String::class)
        } returns LBResult.Success<String>(MessagingConstant.SafeItemMessageData)
        coEvery {
            this@mockk(byteArrayOf(4), contactId, DoubleRatchetUUID::class)
        } returns LBResult.Success<DoubleRatchetUUID>(safeItemId)
    }

    private val decryptSafeMessageUseCase: DecryptSafeMessageUseCase = DecryptSafeMessageUseCase(
        contactLocalDecryptUseCase,
    )

    @Test
    fun message_default_test(): TestResult = runTest {
        val message = decryptSafeMessageUseCase.message(message)
        assertIs<PlainMessageData.Default>(message)
        val actualContent = bubblesAssertSuccess(message.content).successData
        assertEquals("content", actualContent)
        val actualChannel = bubblesAssertSuccess(message.channel).successData
        assertEquals("channel", actualChannel)
        val actualSentAt = bubblesAssertSuccess(message.sentAt).successData
        assertEquals(Instant.fromEpochSeconds(0), actualSentAt)
        assertFalse(message.hasCorruptedData)
    }

    @Test
    fun message_default_corrupted_test(): TestResult = runTest {
        val expectedError = BubblesCryptoError(BubblesCryptoError.Code.BUBBLES_CONTACT_KEY_DECRYPTION_FAIL)
        coEvery { contactLocalDecryptUseCase(byteArrayOf(1), contactId, String::class) } returns LBResult.Failure(expectedError)

        val message = decryptSafeMessageUseCase.message(message)
        assertIs<PlainMessageData.Default>(message)
        val actualError = bubblesAssertFailure(message.content).throwable
        assertEquals(expectedError, actualError)
        val actualChannel = bubblesAssertSuccess(message.channel).successData
        assertEquals("channel", actualChannel)
        val actualSentAt = bubblesAssertSuccess(message.sentAt).successData
        assertEquals(Instant.fromEpochSeconds(0), actualSentAt)
        assertTrue(message.hasCorruptedData)
    }

    @Test
    fun message_invitation_test(): TestResult = runTest {
        coEvery {
            contactLocalDecryptUseCase(byteArrayOf(1), contactId, String::class)
        } returns LBResult.Success(MessagingConstant.FirstMessageData)

        val message = decryptSafeMessageUseCase.message(message)
        assertIs<PlainMessageData.AcceptedInvitation>(message)
        val actualChannel = bubblesAssertSuccess(message.channel).successData
        assertEquals("channel", actualChannel)
        val actualSentAt = bubblesAssertSuccess(message.sentAt).successData
        assertEquals(Instant.fromEpochSeconds(0), actualSentAt)
        assertFalse(message.hasCorruptedData)
    }

    @Test
    fun `PlainMessageData type test`(): TestResult = runTest {
        val message = decryptSafeMessageUseCase.message(itemMessage)
        assertIs<PlainMessageData.SafeItem>(message)
        assertEquals(safeItemId, message.itemId)
    }
}
