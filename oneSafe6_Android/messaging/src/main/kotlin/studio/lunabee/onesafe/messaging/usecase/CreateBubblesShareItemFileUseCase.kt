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
 * Last modified 07/08/2024 09:24
 */

package studio.lunabee.onesafe.messaging.usecase

import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import studio.lunabee.doubleratchet.model.DRMessageKey
import studio.lunabee.onesafe.domain.qualifier.ArchiveCacheDir
import studio.lunabee.onesafe.domain.utils.mkdirs
import studio.lunabee.onesafe.error.OSImportExportError
import studio.lunabee.onesafe.importexport.engine.ShareExportEngine
import studio.lunabee.onesafe.importexport.usecase.ExportShareUseCase
import studio.lunabee.onesafe.jvm.toCharArray
import java.io.File
import java.util.UUID
import javax.inject.Inject

class CreateBubblesShareItemFileUseCase @Inject constructor(
    private val exportShareUseCase: ExportShareUseCase,
    private val exportEngine: ShareExportEngine,
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.Message) private val archiveDir: File,
) {
    operator fun invoke(
        itemId: UUID,
        messageKey: DRMessageKey,
        includeChildren: Boolean,
    ): Flow<LBFlowResult<File>> = flow {
        archiveDir.mkdirs(override = true)
        val fileName = UUID.randomUUID().toString()
        val archiveFile = File(archiveDir, fileName)
        exportEngine
            .buildExportInfo(
                password = messageKey.value.toCharArray(),
            ).collect { prepareSharingResult ->
                when (prepareSharingResult) {
                    is LBFlowResult.Success -> {
                        exportShareUseCase(
                            exportEngine = exportEngine,
                            itemToShare = itemId,
                            includeChildren = includeChildren,
                            archiveExtractedDirectory = archiveFile,
                        ).collect { result ->
                            when (result) {
                                is LBFlowResult.Success -> {
                                    val file = result.data
                                    if (file != null) {
                                        emit(LBFlowResult.Success(file))
                                    } else {
                                        emit(
                                            LBFlowResult
                                                .Failure(OSImportExportError(OSImportExportError.Code.EXPORT_DATA_FAILURE)),
                                        )
                                    }
                                }
                                is LBFlowResult.Failure -> emit(LBFlowResult.Failure(result.throwable))
                                is LBFlowResult.Loading -> emit(LBFlowResult.Loading())
                            }
                        }
                    }
                    is LBFlowResult.Failure -> emit(LBFlowResult.Failure(prepareSharingResult.throwable))
                    is LBFlowResult.Loading -> emit(LBFlowResult.Loading())
                }
            }
    }
}
