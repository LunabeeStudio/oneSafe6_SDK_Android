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
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:24 AM
 */

package studio.lunabee.onesafe.importexport

import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import studio.lunabee.onesafe.domain.model.crypto.SubKeyType
import studio.lunabee.onesafe.domain.model.importexport.OSArchiveKind
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.qualifier.DateFormatterType
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.error.OSImportExportError
import studio.lunabee.onesafe.importexport.engine.BackupExportEngine
import studio.lunabee.onesafe.importexport.engine.ExportEngine
import studio.lunabee.onesafe.importexport.model.ExportData
import studio.lunabee.onesafe.importexport.model.ExportInfo
import studio.lunabee.onesafe.importexport.usecase.CreateBackupInfoUseCase
import java.io.File
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Implementation of [ExportEngine] (and [BackupExportEngine]) for backup exports
 */
class BackupExportEngineImpl @Inject constructor(
    @FileDispatcher fileDispatcher: CoroutineDispatcher,
    @DateFormatterType(type = DateFormatterType.Type.IsoInstant) dateTimeFormatter: DateTimeFormatter,
    private val backupInfoProvider: CreateBackupInfoUseCase,
    private val safeRepository: SafeRepository,
) : AbstractExportEngine(
    fileDispatcher = fileDispatcher,
    dateTimeFormatter = dateTimeFormatter,
),
    BackupExportEngine {
    override fun createExportArchiveContent(
        dataHolderFolder: File,
        data: ExportData,
        archiveKind: OSArchiveKind,
        safeId: SafeId,
    ): Flow<LBFlowResult<Unit>> {
        return flow {
            val exportInfoResult = OSError.runCatching {
                ExportInfo(
                    archiveMasterKey = null,
                    fromPlatformVersion = backupInfoProvider(),
                    exportSalt = safeRepository.getSalt(safeId),
                    encBubblesMasterKey = safeRepository.getCurrentSubKey(SubKeyType.Bubbles),
                )
            }
            when (exportInfoResult) {
                is LBResult.Failure -> emit(LBFlowResult.Failure(exportInfoResult.throwable))
                is LBResult.Success -> {
                    val exportArchiveContentFlow = super.createExportArchiveContent(
                        dataHolderFolder = dataHolderFolder,
                        data = data,
                        archiveKind = archiveKind,
                        exportInfo = exportInfoResult.successData,
                    )
                    emitAll(exportArchiveContentFlow)
                }
            }
        }.catch {
            emit(LBFlowResult.Failure(OSImportExportError(OSImportExportError.Code.UNEXPECTED_ERROR, cause = it)))
        }
    }
}
