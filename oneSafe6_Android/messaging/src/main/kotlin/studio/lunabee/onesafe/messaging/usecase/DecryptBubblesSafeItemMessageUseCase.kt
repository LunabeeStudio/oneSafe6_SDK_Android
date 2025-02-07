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
 * Last modified 07/08/2024 14:49
 */

package studio.lunabee.onesafe.messaging.usecase

import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.flow.first
import studio.lunabee.doubleratchet.model.DRMessageKey
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.model.createRandomUUID
import studio.lunabee.messaging.domain.MessagingConstant
import studio.lunabee.messaging.domain.model.DecryptResult
import studio.lunabee.messaging.domain.usecase.ManageIncomingMessageUseCase
import studio.lunabee.messaging.domain.usecase.ManagingIncomingMessageResultData
import studio.lunabee.messaging.domain.usecase.SaveMessageUseCase
import studio.lunabee.onesafe.domain.model.importexport.ImportMode
import studio.lunabee.onesafe.domain.qualifier.ArchiveCacheDir
import studio.lunabee.onesafe.domain.utils.mkdirs
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.getOrThrow
import studio.lunabee.onesafe.importexport.usecase.ArchiveUnzipUseCase
import studio.lunabee.onesafe.importexport.usecase.ImportAuthUseCase
import studio.lunabee.onesafe.importexport.usecase.ImportSaveDataUseCase
import studio.lunabee.onesafe.jvm.toCharArray
import java.io.File
import java.util.UUID
import javax.inject.Inject

class DecryptBubblesSafeItemMessageUseCase @Inject constructor(
    private val manageIncomingMessageUseCase: ManageIncomingMessageUseCase,
    private val importAuthUseCase: ImportAuthUseCase,
    private val saveMessageUseCase: SaveMessageUseCase,
    private val importSaveDataUseCase: ImportSaveDataUseCase,
    private val unzipUseCase: ArchiveUnzipUseCase,
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.Import) private val importArchiveDir: File,
) {
    suspend operator fun invoke(folder: File): LBResult<DecryptResult> {
        val messageFile = File(folder, MessagingConstant.MessageFileName)
        val data = messageFile.readBytes()
        val result: LBResult<ManagingIncomingMessageResultData> = manageIncomingMessageUseCase(data, null)
        return when (result) {
            is LBResult.Failure -> LBResult.Failure(result.throwable)
            is LBResult.Success -> {
                when (val messageResultData = result.successData) {
                    ManagingIncomingMessageResultData.Invitation -> error("should not happen")
                    is ManagingIncomingMessageResultData.Message -> {
                        LBResult.Success(messageResultData.decryptResult)
                    }
                    is ManagingIncomingMessageResultData.SafeItem -> {
                        val messageKey = (messageResultData.decryptResult as? DecryptResult.NewMessage)?.messageKey
                        if (messageKey == null) {
                            LBResult.Success(messageResultData.decryptResult)
                        } else {
                            importArchiveDir.mkdirs(override = true)
                            val itemFile = File(folder, MessagingConstant.AttachmentFileName)
                            when (val importResult = importSafeFromFile(itemFile, messageKey)) {
                                is LBResult.Failure -> {
                                    LBResult.Failure(importResult.throwable)
                                }
                                is LBResult.Success -> {
                                    val saveId = importResult.successData?.let(::DoubleRatchetUUID)
                                    saveMessageUseCase(
                                        plainMessage = messageResultData.sharedMessage,
                                        contactId = messageResultData.decryptResult.contactId,
                                        channel = null,
                                        id = createRandomUUID(),
                                        safeItemId = saveId,
                                    )
                                    LBResult.Success(messageResultData.decryptResult)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun importSafeFromFile(
        attachmentFile: File,
        messageKey: DRMessageKey,
    ): LBResult<UUID?> {
        return OSError.runCatching {
            unzipUseCase.invoke(attachmentFile.inputStream(), importArchiveDir)
                .first { it !is LBFlowResult.Loading }
                .asResult()
                .getOrThrow()
            importAuthUseCase.invoke(messageKey.value.toCharArray())
                .first { it !is LBFlowResult.Loading }
                .asResult()
                .getOrThrow()
            importSaveDataUseCase.invoke(importArchiveDir, ImportMode.Append)
                .first { it !is LBFlowResult.Loading }
                .asResult().getOrThrow()
        }
    }
}
