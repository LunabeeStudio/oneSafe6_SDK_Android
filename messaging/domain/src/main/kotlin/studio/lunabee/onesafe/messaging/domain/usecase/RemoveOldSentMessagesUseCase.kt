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
 * Created by Lunabee Studio / Date - 8/21/2023 - for the oneSafe6 SDK.
 * Last modified 21/08/2023 10:06
 */

package studio.lunabee.onesafe.messaging.domain.usecase

import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.flow.first
import studio.lunabee.onesafe.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.onesafe.domain.repository.SecurityOptionRepository
import studio.lunabee.onesafe.messaging.domain.model.SentMessage
import studio.lunabee.onesafe.messaging.domain.repository.SentMessageRepository
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

class RemoveOldSentMessagesUseCase @Inject constructor(
    private val sentMessageRepository: SentMessageRepository,
    private val localContactLocalDecryptUseCase: ContactLocalDecryptUseCase,
    private val securityOptionRepository: SecurityOptionRepository,
    private val clock: Clock,
) {

    suspend operator fun invoke() {
        var oldestSentMessage: SentMessage? = sentMessageRepository.getOldestSentMessage()
        val sentMessageTimeToLive: Duration = securityOptionRepository.bubblesResendMessageDelayFlow.first()
        while (oldestSentMessage != null) {
            val createdAtRes: LBResult<Instant> = localContactLocalDecryptUseCase(
                data = oldestSentMessage.encCreatedAt,
                contactId = oldestSentMessage.contactId,
                clazz = Instant::class,
            )
            oldestSentMessage = when (createdAtRes) {
                is LBResult.Failure -> delete(oldestSentMessage)
                is LBResult.Success -> {
                    val messageAge = java.time.Duration.between(createdAtRes.successData, Instant.now(clock)).toKotlinDuration()
                    if (messageAge > sentMessageTimeToLive) {
                        delete(oldestSentMessage)
                    } else {
                        null
                    }
                }
            }
        }
    }

    private suspend fun delete(actualSentMessage: SentMessage): SentMessage? {
        sentMessageRepository.deleteSentMessage(actualSentMessage.id)
        return sentMessageRepository.getOldestSentMessage()
    }
}
