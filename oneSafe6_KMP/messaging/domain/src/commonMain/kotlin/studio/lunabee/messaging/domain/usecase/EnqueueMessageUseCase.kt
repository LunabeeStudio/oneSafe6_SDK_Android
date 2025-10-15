/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 6/7/2023 - for the oneSafe6 SDK.
 * Last modified 6/7/23, 4:41 PM
 */

package studio.lunabee.messaging.domain.usecase

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.messaging.domain.repository.EnqueuedMessageRepository
import studio.lunabee.messaging.domain.repository.MessagingCryptoRepository
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.error.OSError

/**
 * Add an incoming message to the queue for future processing
 */
class EnqueueMessageUseCase @Inject constructor(
    private val cryptoRepository: MessagingCryptoRepository,
    private val queueMessageRepository: EnqueuedMessageRepository,
) {
    /**
     * @param encMessage Row message received
     * @param channel channel name
     */
    suspend operator fun invoke(encMessage: ByteArray, channel: String?): LBResult<Unit> = OSError.runCatching {
        val encChannel = channel?.let { cryptoRepository.queueEncrypt(EncryptEntry(channel)) }
        queueMessageRepository.save(encMessage, encChannel)
        // TODO could be improve by checking the stored channel in order to update it if it was null
    }
}
