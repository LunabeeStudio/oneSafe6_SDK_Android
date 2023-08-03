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
 * Last modified 6/26/23, 8:02 AM
 */

package studio.lunabee.onesafe.messaging.domain.usecase

import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.flow.first
import studio.lunabee.onesafe.domain.usecase.authentication.IsCryptoDataReadyInMemoryUseCase
import java.util.UUID
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

// TODO fix double call
class HandleIncomingMessageUseCase @Inject constructor(
    private val isCryptoDataReadyInMemoryUseCase: IsCryptoDataReadyInMemoryUseCase,
    private val decryptIncomingMessageUseCase: DecryptIncomingMessageUseCase,
    private val enqueueMessageUseCase: EnqueueMessageUseCase,
    private val saveMessageUseCase: SaveMessageUseCase,
) {
    @OptIn(ExperimentalEncodingApi::class)
    suspend operator fun invoke(message: String, channel: String?): LBResult<IncomingMessageState> {
        val cipherData = try {
            Base64.decode(message)
        } catch (e: IllegalArgumentException) {
            // ignore base64 errors
            return LBResult.Success(IncomingMessageState.NotBase64)
        }

        return if (isCryptoDataReadyInMemoryUseCase().first()) {
            val decryptResult = decryptIncomingMessageUseCase(cipherData)
            when (decryptResult) {
                is LBResult.Success -> {
                    val plainMessage = decryptResult.successData.second
                    val contactId = decryptResult.successData.first.id
                    plainMessage?.let {
                        val saveResult = saveMessageUseCase(
                            plainMessage = plainMessage.content,
                            sentAt = plainMessage.sentAt,
                            contactId = contactId,
                            recipientId = plainMessage.recipientId,
                            channel = channel,
                        )
                        when (saveResult) {
                            is LBResult.Failure -> LBResult.Failure(saveResult.throwable)
                            is LBResult.Success -> LBResult.Success(IncomingMessageState.Processed(contactId))
                        }
                    } ?: LBResult.Success(IncomingMessageState.Processed(contactId))
                }
                is LBResult.Failure -> LBResult.Failure(decryptResult.throwable)
            }
        } else {
            val enqueueResult = enqueueMessageUseCase(cipherData, channel)
            when (enqueueResult) {
                is LBResult.Failure -> LBResult.Failure(enqueueResult.throwable)
                is LBResult.Success -> LBResult.Success(IncomingMessageState.Enqueued)
            }
        }
    }
}

sealed interface IncomingMessageState {
    object NotBase64 : IncomingMessageState
    object Enqueued : IncomingMessageState
    class Processed(val contactId: UUID) : IncomingMessageState
}
