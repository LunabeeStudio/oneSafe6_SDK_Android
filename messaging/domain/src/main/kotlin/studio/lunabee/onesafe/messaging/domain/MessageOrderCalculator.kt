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
 * Created by Lunabee Studio / Date - 6/21/2023 - for the oneSafe6 SDK.
 * Last modified 6/21/23, 8:39 AM
 */

package studio.lunabee.onesafe.messaging.domain

import studio.lunabee.onesafe.bubbles.domain.model.ContactLocalKey
import studio.lunabee.onesafe.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.messaging.domain.repository.MessageOrderRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Compute the order of a message from it sentAt date
 *   • If the new message is the new most recent, increment current most recent and round backward
 *   • If the new message is the new least recent, decrement current least recent and round toward
 *   • Else put the message between two others
 */
class MessageOrderCalculator @Inject constructor(
    private val messageOrderRepository: MessageOrderRepository,
    private val bubblesCryptoRepository: BubblesCryptoRepository,
) {
    suspend operator fun invoke(messageSentAt: Instant, contactId: UUID, key: ContactLocalKey): OrderResult {
        val lastMessageOrder = messageOrderRepository.getMostRecent(contactId)
        return if (lastMessageOrder == null) {
            OrderResult.Found(0f)
        } else {
            val lastSentAt = bubblesCryptoRepository.localDecrypt(key, DecryptEntry(lastMessageOrder.encSentAt, Instant::class))
            if (messageSentAt > lastSentAt) {
                OrderResult.Found(floor(lastMessageOrder.order + 1f))
            } else {
                val firstMessageOrder = messageOrderRepository.getLeastRecent(contactId)!!.order
                binarySearch(messageSentAt, contactId, key, lastMessageOrder.order to firstMessageOrder)
            }
        }
    }

    private suspend fun binarySearch(
        newMessageSentAt: Instant,
        contactId: UUID,
        key: ContactLocalKey,
        orderRange: Pair<Float, Float>,
    ): OrderResult {
        val count = messageOrderRepository.count(contactId) - 1
        var start = 0
        var end = count

        // Init with possible new max/min order
        var next = floor(orderRange.first + 1f)
        var previous = ceil(orderRange.second - 1f)

        while (start <= end) {
            val mid = (start + (end - start) / 2f).toInt()
            val midMessage = messageOrderRepository.getAt(contactId, mid)!!
            val midSentAt = bubblesCryptoRepository.localDecrypt(key, DecryptEntry(midMessage.encSentAt, Instant::class))
            when {
                midSentAt == newMessageSentAt -> {
                    return when (mid) {
                        0 -> OrderResult.Duplicated(floor(midMessage.order + 1f), midMessage.order)
                        count -> OrderResult.Duplicated(ceil(midMessage.order - 1f), midMessage.order)
                        else -> {
                            val nextMessageOrder = messageOrderRepository.getAt(contactId, mid - 1)!!.order
                            OrderResult.Duplicated((nextMessageOrder + midMessage.order) / 2f, midMessage.order)
                        }
                    }
                }
                newMessageSentAt < midSentAt -> {
                    start = mid + 1
                    next = midMessage.order
                }
                newMessageSentAt > midSentAt -> {
                    end = mid - 1
                    previous = midMessage.order
                }
            }
        }

        var order = (next + previous) / 2f

        // Handle edge case where the new message is the before the first
        if (next == orderRange.second) {
            order = ceil(orderRange.second - 1f)
        }

        return OrderResult.Found(order)
    }

    sealed interface OrderResult {
        /**
         * New order has been found for the message
         */
        @JvmInline
        value class Found(val order: Float) : OrderResult

        /**
         * Message has the same sentAt as the message with order [duplicatedOrder]. New order candidate for the message is [candidateOrder]
         */
        class Duplicated(val candidateOrder: Float, val duplicatedOrder: Float) : OrderResult
    }
}
