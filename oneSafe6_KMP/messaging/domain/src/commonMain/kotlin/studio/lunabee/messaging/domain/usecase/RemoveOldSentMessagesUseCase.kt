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

package studio.lunabee.messaging.domain.usecase

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.model.SentMessage
import studio.lunabee.messaging.domain.repository.MessagingSettingsRepository
import studio.lunabee.messaging.domain.repository.SentMessageRepository
import studio.lunabee.onesafe.error.BubblesCryptoError
import studio.lunabee.onesafe.error.OSError
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val logger = LBLogger.get<RemoveOldSentMessagesUseCase>()

class RemoveOldSentMessagesUseCase @Inject constructor(
    private val sentMessageRepository: SentMessageRepository,
    private val localContactLocalDecryptUseCase: ContactLocalDecryptUseCase,
    private val messagingSettingsRepository: MessagingSettingsRepository,
    private val clock: Clock,
) {

    suspend operator fun invoke(safeId: DoubleRatchetUUID): LBResult<Unit> = OSError.runCatching(logger) {
        var oldestSentMessage: SentMessage? = sentMessageRepository.getOldestSentMessage(safeId)
        val sentMessageTimeToLive: Duration = messagingSettingsRepository.bubblesResendMessageDelayInMillis(safeId).milliseconds
        while (oldestSentMessage != null) {
            val createdAtRes: LBResult<Instant> = localContactLocalDecryptUseCase(
                data = oldestSentMessage.encCreatedAt,
                contactId = oldestSentMessage.contactId,
                clazz = Instant::class,
            )
            oldestSentMessage = when (createdAtRes) {
                is LBResult.Failure -> {
                    if ((createdAtRes.throwable as? BubblesCryptoError)?.code == BubblesCryptoError.Code.BUBBLES_MASTER_KEY_NOT_LOADED) {
                        return@runCatching
                    } else {
                        delete(oldestSentMessage, safeId)
                    }
                }
                is LBResult.Success -> {
                    val messageAge = clock.now() - createdAtRes.successData
                    if (messageAge > sentMessageTimeToLive) {
                        delete(oldestSentMessage, safeId)
                    } else {
                        null
                    }
                }
            }
        }
    }

    private suspend fun delete(actualSentMessage: SentMessage, safeId: DoubleRatchetUUID): SentMessage? {
        sentMessageRepository.deleteSentMessage(actualSentMessage.id)
        return sentMessageRepository.getOldestSentMessage(safeId)
    }
}
