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
 * Created by Lunabee Studio / Date - 6/26/2023 - for the oneSafe6 SDK.
 * Last modified 6/26/23, 9:27 AM
 */

package studio.lunabee.onesafe.messaging.domain.usecase

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
import studio.lunabee.onesafe.domain.common.MessageIdProvider
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.usecase.authentication.IsCryptoDataReadyInMemoryUseCase
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.messaging.domain.model.EnqueuedMessage
import studio.lunabee.onesafe.messaging.domain.repository.EnqueuedMessageRepository
import studio.lunabee.onesafe.messaging.domain.repository.MessagingCryptoRepository
import javax.inject.Inject

private val log = LBLogger.get<ProcessMessageQueueUseCase>()

class ProcessMessageQueueUseCase @Inject constructor(
    private val enqueuedMessageRepository: EnqueuedMessageRepository,
    private val decryptIncomingMessageUseCase: DecryptIncomingMessageUseCase,
    private val isCryptoDataReadyInMemoryUseCase: IsCryptoDataReadyInMemoryUseCase,
    private val saveMessageUseCase: SaveMessageUseCase,
    private val cryptoRepository: MessagingCryptoRepository,
    private val messageIdProvider: MessageIdProvider,
) {
    /**
     * Wait until crypto is ready and dequeue all messages to process them. Skip the flush the observer is running.
     */
    suspend fun flush() {
        isCryptoDataReadyInMemoryUseCase.flow().collectLatest { cryptoReady ->
            if (cryptoReady) {
                if (mutex.tryLock()) {
                    try {
                        enqueuedMessageRepository.getAll().forEach { enqueuedMessage ->
                            processMessage(enqueuedMessage)
                            yield()
                        }
                    } finally {
                        mutex.unlock()
                    }
                }
            }
        }
    }

    /**
     * Wait until crypto is ready, observe and dequeue all messages to process them
     */
    suspend fun observe() {
        isCryptoDataReadyInMemoryUseCase.flow()
            .collectLatest { cryptoLoaded ->
                if (cryptoLoaded) {
                    mutex.withLock {
                        enqueuedMessageRepository
                            .getOldestAsFlow()
                            .filterNotNull()
                            .collectLatest { enqueuedMessage ->
                                processMessage(enqueuedMessage)
                            }
                    }
                }
            }
    }

    private suspend fun processMessage(enqueuedMessage: EnqueuedMessage) {
        val result = decryptIncomingMessageUseCase(enqueuedMessage.encIncomingMessage)
        when (result) {
            is LBResult.Failure -> {
                // Unexpected case as we observe the crypto state. Do not delete the enqueued message.
                if ((result.throwable as? OSCryptoError)?.code == OSCryptoError.Code.BUBBLES_MASTER_KEY_NOT_LOADED) {
                    result.throwable?.let(log::e)
                    return
                }
            }
            is LBResult.Success -> {
                val plainMessage = result.successData.osPlainMessage
                val channel = getChannel(enqueuedMessage)
                plainMessage?.let {
                    val saveResult = saveMessageUseCase(
                        plainMessage = plainMessage,
                        contactId = result.successData.contactId,
                        channel = channel,
                        id = messageIdProvider(),
                    )
                    when (saveResult) {
                        is LBResult.Failure -> saveResult.throwable?.let(log::e)
                        is LBResult.Success -> {
                            /* no-op */
                        }
                    }
                }
            }
        }
        // Always delete processed enqueued message
        enqueuedMessageRepository.delete(enqueuedMessage.id)
    }

    private suspend fun getChannel(enqueuedMessage: EnqueuedMessage): String? = runCatching {
        // Do not block the message decryption if we cannot decrypt the channel
        enqueuedMessage.encChannel?.let { encChannel ->
            cryptoRepository.queueDecrypt(DecryptEntry(encChannel, String::class))
        }
    }.onFailure {
        log.e(it)
    }.getOrNull()

    companion object {
        // Make sure observe and flush cannot run concurrently
        private val mutex = Mutex()
    }
}
