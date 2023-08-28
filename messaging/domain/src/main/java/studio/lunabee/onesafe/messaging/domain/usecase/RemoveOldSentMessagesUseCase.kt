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

import studio.lunabee.onesafe.bubbles.domain.BubblesConstant
import studio.lunabee.onesafe.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.onesafe.messaging.domain.model.SentMessage
import studio.lunabee.onesafe.messaging.domain.repository.SentMessageRepository
import java.time.Instant
import javax.inject.Inject

class RemoveOldSentMessagesUseCase @Inject constructor(
    private val sentMessageRepository: SentMessageRepository,
    private val localContactLocalDecryptUseCase: ContactLocalDecryptUseCase,
) {

    suspend operator fun invoke() {
        var actualSentMessage: SentMessage? = sentMessageRepository.getOldestSentMessage()
        while (actualSentMessage != null) {
            val createdAt: Instant? = localContactLocalDecryptUseCase(
                data = actualSentMessage.encCreatedAt,
                actualSentMessage.contactId,
                Instant::class,
            ).data
            if (createdAt != null) {
                val messageAge = Instant.now().minusMillis(createdAt.toEpochMilli())
                if (messageAge.toEpochMilli() > BubblesConstant.SentMessageTimeToLive.inWholeMilliseconds) {
                    sentMessageRepository.deleteSentMessage(actualSentMessage.id)
                    actualSentMessage = sentMessageRepository.getOldestSentMessage()
                } else {
                    break
                }
            }
        }
    }
}
