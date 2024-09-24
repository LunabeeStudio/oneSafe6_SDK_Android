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
 * Last modified 5/30/24, 5:11 PM
 */

package studio.lunabee.messaging.domain

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.bubbles.domain.model.contactkey.ContactLocalKey
import studio.lunabee.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.model.createRandomUUID
import studio.lunabee.messaging.domain.model.MessageOrder
import studio.lunabee.messaging.domain.repository.MessageOrderRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MessageOrderCalculatorTest {
    private val contactId: DoubleRatchetUUID = createRandomUUID()
    private val contactLocalKey = ContactLocalKey(byteArrayOf())
    private val messageCount = 5

    private val messageOrders: List<MessageOrder> = List(messageCount) { idx ->
        MessageOrder(
            encSentAt = byteArrayOf(idx.toByte()),
            order = idx.toFloat(),
            id = testUUID[idx],
        )
    }.sortedByDescending { it.order }

    private val messageOrderRepository: MessageOrderRepository = mockk {
        coEvery { getMostRecent(contactId, any()) } returns messageOrders.first()
        coEvery { getLeastRecent(contactId, any()) } returns messageOrders.last()
        coEvery { count(contactId, any()) } returns messageCount
        coEvery { getAt(contactId, any(), any()) } answers {
            val position = secondArg() as Int
            messageOrders.getOrNull(position)
        }
    }
    private val bubblesCryptoRepository: BubblesCryptoRepository = mockk {
        coEvery { localDecrypt(contactLocalKey, any<DecryptEntry<Instant>>()) } answers {
            val sentAt = (secondArg() as DecryptEntry<Instant>).data.first().toLong()
            Instant.fromEpochSeconds(sentAt)
        }
    }

    private val messageOrderCalculator: MessageOrderCalculator = MessageOrderCalculator(
        messageOrderRepository,
        bubblesCryptoRepository,
        Clock.System,
    )

    @Test
    fun messageOrderCalculator_noMessage_test(): TestResult = runTest {
        coEvery { messageOrderRepository.getMostRecent(contactId, any()) } returns null

        val expected = 0f
        val result = messageOrderCalculator.invoke(Instant.DISTANT_PAST, contactId, contactLocalKey)

        assertIs<MessageOrderCalculator.OrderResult.Found>(result)
        assertEquals(expected, result.order)
        coVerify(exactly = 0) { bubblesCryptoRepository.localDecrypt(contactLocalKey, any<DecryptEntry<Instant>>()) }
        confirmVerified(bubblesCryptoRepository)
    }

    @Test
    fun messageOrderCalculator_isLast_test(): TestResult = runTest {
        val expected = (messageOrders.lastIndex + 1).toFloat()
        val result = messageOrderCalculator.invoke(Instant.DISTANT_FUTURE, contactId, contactLocalKey)

        assertIs<MessageOrderCalculator.OrderResult.Found>(result)
        assertEquals(expected, result.order)
        coVerify(exactly = 1) { bubblesCryptoRepository.localDecrypt(contactLocalKey, any<DecryptEntry<Instant>>()) }
        confirmVerified(bubblesCryptoRepository)
    }

    @Test
    fun messageOrderCalculator_isFirst_test(): TestResult = runTest {
        val expected = -1f
        val result = messageOrderCalculator.invoke(Instant.fromEpochSeconds(-1L), contactId, contactLocalKey)

        assertIs<MessageOrderCalculator.OrderResult.Found>(result)
        assertEquals(expected, result.order)
    }

    @Test
    fun messageOrderCalculator_duplicate_test(): TestResult = runTest {
        messageOrders.forEachIndexed { idx, msgOrder ->
            val sentAtSec = msgOrder.encSentAt.first().toLong()
            val sentAt = Instant.fromEpochSeconds(sentAtSec)
            val result = messageOrderCalculator.invoke(sentAt, contactId, contactLocalKey)
            val expected = when (idx) {
                0 -> sentAtSec + 1f
                messageOrders.lastIndex -> sentAtSec - 1f
                else -> sentAtSec + 0.5f
            }

            val message = "sentAtSec=$sentAtSec"
            assertIs<MessageOrderCalculator.OrderResult.Duplicated>(result, message)
            assertEquals(expected, result.candidateOrder, message)
            assertEquals(sentAtSec.toFloat(), result.duplicatedOrder, message)
        }
    }

    @Test
    fun messageOrderCalculator_insertEverywhere_test(): TestResult = runTest {
        repeat(2) { offsetSign ->
            repeat(messageCount) { position ->
                val nanos: Long = if (offsetSign == 0) 1 else -1
                val messageSentAt = Instant.fromEpochSeconds(position.toLong(), nanos)
                val expected = if (position == messageCount - 1 && nanos == 1L) {
                    position.toFloat() + 1f // last message case
                } else if (position == 0 && nanos == -1L) {
                    -1f // first message case
                } else {
                    position.toFloat() + (0.5f * nanos)
                }
                val result = messageOrderCalculator.invoke(messageSentAt, contactId, contactLocalKey)

                val message = "offsetSign=$nanos, position=$position, messageCount=$messageCount"
                assertIs<MessageOrderCalculator.OrderResult.Found>(
                    value = result,
                    message = message,
                )
                assertEquals(
                    expected = expected,
                    actual = result.order,
                    message = message,
                )
            }
        }
    }

    /**
     * Assert that the new most or least recent get an integer order (1.3 -> 2 and not 2.3)
     */
    @Test
    fun messageOrderCalculator_afterBeforeRounding_test(): TestResult = runTest {
        val currentMessageOrder = MessageOrder(testUUID[0], byteArrayOf(10), 1.3f)

        coEvery { messageOrderRepository.getMostRecent(contactId, any()) } returns currentMessageOrder
        coEvery { messageOrderRepository.getLeastRecent(contactId, any()) } returns currentMessageOrder
        coEvery { messageOrderRepository.count(contactId, any()) } returns 1
        coEvery { messageOrderRepository.getAt(contactId, any(), any()) } answers {
            val position = secondArg() as Int
            if (position == 0) currentMessageOrder else null
        }

        val actualBefore = messageOrderCalculator.invoke(Instant.fromEpochSeconds(5L), contactId, contactLocalKey)
        assertIs<MessageOrderCalculator.OrderResult.Found>(actualBefore)
        assertEquals(1f, actualBefore.order)

        val actualAfter = messageOrderCalculator.invoke(Instant.fromEpochSeconds(15L), contactId, contactLocalKey)
        assertIs<MessageOrderCalculator.OrderResult.Found>(actualAfter)
        assertEquals(2f, actualAfter.order)
    }
}
