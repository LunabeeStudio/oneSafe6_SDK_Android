/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 8/7/2024 - for the oneSafe6 SDK.
 * Last modified 07/08/2024 09:54
 */

package studio.lunabee.onesafe.messaging.usecase

import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.MessagingConstant
import studio.lunabee.messaging.domain.usecase.EncryptMessageUseCase
import studio.lunabee.messaging.domain.usecase.GetSendMessageDataUseCase
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Clock

class CreateMessageWithItemSharedUseCase @Inject constructor(
    private val createBubblesShareItemFileUseCase: CreateBubblesShareItemFileUseCase,
    private val getSendMessageDataUseCase: GetSendMessageDataUseCase,
    private val encryptMessageUseCase: EncryptMessageUseCase,
    private val clock: Clock,
    private val createBubblesMessageArchiveUseCase: CreateBubblesMessageArchiveUseCase,
) {

    operator fun invoke(
        itemId: UUID,
        includeChildren: Boolean,
        contactId: DoubleRatchetUUID,
    ): Flow<LBFlowResult<File>> = flow {
        // Generate the double ratchet data
        val sendMessageData = getSendMessageDataUseCase(contactId)
        when (sendMessageData) {
            is LBResult.Failure -> emit(LBFlowResult.Failure(sendMessageData.throwable))
            is LBResult.Success -> {
                // Create the share item file encrypted with the doubleRatchet key
                createBubblesShareItemFileUseCase
                    .invoke(
                        itemId = itemId,
                        messageKey = sendMessageData.successData.messageKey,
                        includeChildren = includeChildren,
                    ).collect { safeItemFileResult ->
                        when (safeItemFileResult) {
                            is LBFlowResult.Loading -> emit(LBFlowResult.Loading())
                            is LBFlowResult.Failure -> emit(safeItemFileResult)
                            is LBFlowResult.Success -> {
                                // Generate the message data with ShareItem constant string
                                val messageResult = encryptMessageUseCase.invoke(
                                    plainMessage = MessagingConstant.SafeItemMessageData,
                                    contactId = contactId,
                                    sentAt = clock.now(),
                                    sendMessageData = sendMessageData.successData,
                                )
                                when (messageResult) {
                                    is LBResult.Failure -> emit(LBFlowResult.Failure(messageResult.throwable))
                                    is LBResult.Success -> {
                                        // Generate the final file
                                        createBubblesMessageArchiveUseCase(
                                            messageData = messageResult.successData,
                                            attachmentFile = safeItemFileResult.successData,
                                        ).collect(::emit)
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }
}
