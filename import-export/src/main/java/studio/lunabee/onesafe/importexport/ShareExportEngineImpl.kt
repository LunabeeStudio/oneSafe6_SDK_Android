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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.onesafe.domain.common.BackupInfoProvider
import studio.lunabee.onesafe.domain.model.importexport.OSArchiveKind
import studio.lunabee.onesafe.domain.qualifier.DateFormatterType
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.error.OSImportExportError
import studio.lunabee.onesafe.importexport.engine.ExportEngine
import studio.lunabee.onesafe.importexport.engine.ShareExportEngine
import studio.lunabee.onesafe.importexport.model.ExportData
import studio.lunabee.onesafe.importexport.model.ExportInfo
import studio.lunabee.onesafe.importexport.repository.ImportExportCryptoRepository
import java.io.File
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Implementation of [ExportEngine] (and [ShareExportEngine]) dedicated to sharing
 */
class ShareExportEngineImpl @Inject constructor(
    @FileDispatcher fileDispatcher: CoroutineDispatcher,
    private val importExportCryptoRepository: ImportExportCryptoRepository,
    @DateFormatterType(type = DateFormatterType.Type.IsoInstant) dateTimeFormatter: DateTimeFormatter,
    private val backupInfoProvider: BackupInfoProvider,
) : AbstractExportEngine(
    fileDispatcher = fileDispatcher,
    dateTimeFormatter = dateTimeFormatter,
),
    ShareExportEngine {
    override val exportKey: ByteArray
        get() = exportInfo?.archiveMasterKey ?: throw OSImportExportError(OSImportExportError.Code.ENGINE_NOT_PREPARED)

    private var exportInfo: ExportInfo? = null

    override fun buildExportInfo(password: CharArray): Flow<LBFlowResult<Unit>> {
        return flow<LBFlowResult<Unit>> {
            val keySaltPair = importExportCryptoRepository.createMasterKeyAndSalt(password)
            exportInfo = ExportInfo(
                archiveMasterKey = keySaltPair.first,
                fromPlatformVersion = backupInfoProvider(),
                exportSalt = keySaltPair.second,
            )
            emit(LBFlowResult.Success(Unit))
        }.catch { e ->
            emit(LBFlowResult.Failure(e))
        }
    }

    override fun createExportArchiveContent(
        dataHolderFolder: File,
        data: ExportData,
        archiveKind: OSArchiveKind,
    ): Flow<LBFlowResult<Unit>> {
        val exportInfo = exportInfo
            ?: return flowOf(LBFlowResult.Failure(OSImportExportError(OSImportExportError.Code.ENGINE_NOT_PREPARED)))
        return createExportArchiveContent(
            dataHolderFolder = dataHolderFolder,
            data = data,
            archiveKind = archiveKind,
            exportInfo = exportInfo,
        ).catch {
            emit(LBFlowResult.Failure(OSImportExportError(OSImportExportError.Code.UNEXPECTED_ERROR, cause = it)))
        }
    }
}
