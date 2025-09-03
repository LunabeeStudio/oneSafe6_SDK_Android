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

package studio.lunabee.messaging.domain

import com.lunabee.lblogger.LBLogger
import kotlin.time.Clock
import kotlin.time.Instant
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.bubbles.domain.model.contactkey.ContactLocalKey
import studio.lunabee.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.model.MessageOrder
import studio.lunabee.messaging.domain.repository.MessageOrderRepository
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.error.BubblesCryptoError
import kotlin.math.ceil
import kotlin.math.floor

private val logger = LBLogger.get<MessageOrderCalculator>()

/**
 * Compute the order of a message from it sentAt date
 *   • If the new message is the new most recent, increment current most recent and round backward
 *   • If the new message is the new least recent, decrement current least recent and round toward
 *   • Else put the message between two others
 *
 * Handle potential corrupted messages by ignoring them
 */
class MessageOrderCalculator @Inject constructor(
    private val messageOrderRepository: MessageOrderRepository,
    private val bubblesCryptoRepository: BubblesCryptoRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        messageSentAt: Instant,
        contactId: DoubleRatchetUUID,
        key: ContactLocalKey,
    ): OrderResult {
        val excludedMessages: MutableList<MessageOrder> = mutableListOf()

        var lastMessageOrder: MessageOrder? = null
        var lastSentAt: Instant? = null
        var orderFirstMsg: Float? = null

        while (lastSentAt == null && orderFirstMsg == null) {
            lastMessageOrder = messageOrderRepository.getMostRecent(contactId, excludedMessages.map { it.id })
            if (lastMessageOrder == null) {
                orderFirstMsg = 0f
            } else {
                lastSentAt = try {
                    bubblesCryptoRepository.localDecrypt(key, DecryptEntry(lastMessageOrder.encSentAt, Instant::class))
                } catch (e: BubblesCryptoError) {
                    logger.e(e.message, e)
                    excludedMessages += lastMessageOrder // exclude failing and continue
                    null
                }
            }
        }

        return if (orderFirstMsg != null) {
            // If order found is equal to an excluded message, increment it to make sure we keep the order unique
            if (excludedMessages.any { it.order == orderFirstMsg }) {
                orderFirstMsg += 0.01f
            }
            OrderResult.Found(orderFirstMsg)
        } else {
            checkNotNull(lastMessageOrder)
            if (messageSentAt > (lastSentAt ?: clock.now())) {
                // If excluded messages is not empty, set the order to max excluded message +1
                val order = if (excludedMessages.isNotEmpty()) {
                    floor(excludedMessages.maxOf { it.order } + 1)
                } else {
                    floor(lastMessageOrder.order + 1f)
                }
                OrderResult.Found(order)
            } else {
                val firstMessageOrder = messageOrderRepository.getLeastRecent(contactId, excludedMessages.map { it.id })!!.order
                binarySearch(messageSentAt, contactId, key, lastMessageOrder.order to firstMessageOrder, excludedMessages)
            }
        }
    }

    private suspend fun binarySearch(
        newMessageSentAt: Instant,
        contactId: DoubleRatchetUUID,
        key: ContactLocalKey,
        orderRange: Pair<Float, Float>,
        excludedMessages: MutableList<MessageOrder>,
    ): OrderResult {
        val count = messageOrderRepository.count(contactId, excludedMessages.map { it.id }) - 1
        var start = 0
        var end = count

        // Init with possible new max/min order
        var next = floor(orderRange.first + 1f)
        var previous = ceil(orderRange.second - 1f)

        while (start <= end) {
            val mid = (start + (end - start) / 2f).toInt()
            val midMessage = messageOrderRepository.getAt(contactId, mid, excludedMessages.map { it.id })!!
            val midSentAt = try {
                bubblesCryptoRepository.localDecrypt(key, DecryptEntry(midMessage.encSentAt, Instant::class))
            } catch (e: BubblesCryptoError) {
                logger.e(e.message, e)
                excludedMessages += midMessage // exclude failing
                continue
            }
            when {
                midSentAt == newMessageSentAt -> {
                    return when (mid) {
                        0 -> OrderResult.Duplicated(floor(midMessage.order + 1f), midMessage.order)
                        count -> OrderResult.Duplicated(ceil(midMessage.order - 1f), midMessage.order)
                        else -> {
                            val nextMessageOrder = messageOrderRepository.getAt(contactId, mid - 1, excludedMessages.map { it.id })!!.order
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

        // If order found is equal to an excluded message, increment it to make sure we keep the order unique
        if (excludedMessages.any { it.order == order }) {
            order += 0.01f
        }

        return OrderResult.Found(order)
    }

    sealed interface OrderResult {
        /**
         * New order has been found for the message
         */
        data class Found(val order: Float) : OrderResult

        /**
         * Message has the same sentAt as the message with order [duplicatedOrder]. New order candidate for the message is [candidateOrder]
         */
        class Duplicated(val candidateOrder: Float, val duplicatedOrder: Float) : OrderResult
    }
}
