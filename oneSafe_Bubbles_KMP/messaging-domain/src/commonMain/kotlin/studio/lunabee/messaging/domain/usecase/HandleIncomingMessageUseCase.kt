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

package studio.lunabee.messaging.domain.usecase

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.bubbles.domain.di.Inject
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.model.createRandomUUID

// TODO fix double call
@Suppress("NestedBlockDepth")
class HandleIncomingMessageUseCase @Inject constructor(
    private val decryptIncomingMessageUseCase: DecryptIncomingMessageUseCase,
    private val enqueueMessageUseCase: EnqueueMessageUseCase,
    private val saveMessageUseCase: SaveMessageUseCase,
) {
    suspend operator fun invoke(message: ByteArray, channel: String?, isSafeReady: Boolean): LBResult<IncomingMessageState> {
        return if (isSafeReady) {
            when (val decryptResult = decryptIncomingMessageUseCase(message)) {
                is LBResult.Success -> {
                    val plainMessage = decryptResult.successData.osPlainMessage
                    val contactId = decryptResult.successData.contactId
                    plainMessage?.let {
                        val saveResult = saveMessageUseCase(
                            plainMessage = plainMessage,
                            contactId = contactId,
                            channel = channel,
                            id = createRandomUUID(),
                            safeItemId = null,
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
            when (val enqueueResult = enqueueMessageUseCase(message, channel)) {
                is LBResult.Failure -> LBResult.Failure(enqueueResult.throwable)
                is LBResult.Success -> LBResult.Success(IncomingMessageState.Enqueued)
            }
        }
    }
}

sealed interface IncomingMessageState {
    data object Enqueued : IncomingMessageState
    class Processed(val contactId: DoubleRatchetUUID) : IncomingMessageState
}
