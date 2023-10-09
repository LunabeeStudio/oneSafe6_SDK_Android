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
 * Created by Lunabee Studio / Date - 9/29/2023 - for the oneSafe6 SDK.
 * Last modified 9/29/23, 2:14 PM
 */

package studio.lunabee.onesafe.importexport.usecase

import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.onesafe.domain.model.importexport.OSArchiveKind
import studio.lunabee.onesafe.domain.repository.FileRepository
import studio.lunabee.onesafe.domain.repository.IconRepository
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.importexport.engine.BackupExportEngine
import studio.lunabee.onesafe.importexport.model.ExportData
import studio.lunabee.onesafe.importexport.model.ImportExportConstant
import java.io.File
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ExportBackupUseCase @Inject constructor(
    private val archiveZipUseCase: ArchiveZipUseCase,
    private val safeItemRepository: SafeItemRepository,
    private val safeItemKeyRepository: SafeItemKeyRepository,
    private val safeItemFieldRepository: SafeItemFieldRepository,
    private val iconRepository: IconRepository,
    private val fileRepository: FileRepository,
    private val clock: Clock,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        exportEngine: BackupExportEngine,
        archiveExtractedDirectory: File,
    ): Flow<LBFlowResult<File>> {
        return flow {
            val safeItemsWithKeys = safeItemRepository.getAllSafeItems().associateWith { safeItem ->
                safeItemKeyRepository.getSafeItemKey(safeItem.id)
            }
            val data = ExportData(
                safeItemsWithKeys = safeItemsWithKeys,
                safeItemFields = safeItemFieldRepository.getAllSafeItemFields(),
                icons = iconRepository.getIcons(),
                files = fileRepository.getFiles(),
            )

            emitAll(
                exportEngine.createExportArchiveContent(
                    dataHolderFolder = archiveExtractedDirectory,
                    data = data,
                    archiveKind = OSArchiveKind.Backup,
                ).flatMapLatest { result ->
                    when (result) {
                        is LBFlowResult.Failure -> flowOf(LBFlowResult.Failure(throwable = result.throwable))
                        is LBFlowResult.Loading -> flowOf(LBFlowResult.Loading(progress = result.progress))
                        // Zip if creation is success. Will return the progress of zip.
                        is LBFlowResult.Success -> archiveZipUseCase(
                            folderToZip = archiveExtractedDirectory,
                            outputZipFile = File(archiveExtractedDirectory, buildArchiveName(clock)),
                        )
                    }
                },
            )
        }
    }

    companion object {
        /**
         * Return final archive name (i.e oneSafe-20221217-134622.os6lsb)
         */
        private fun buildArchiveName(clock: Clock): String {
            return listOf(
                ImportExportConstant.ArchiveFilePrefix,
                DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.now(clock)), // i.e 20230113
                ImportExportConstant.ArchiveTimeFormatter.format(LocalTime.now(clock)), // i.e 134602
            ).joinToString(ImportExportConstant.ArchiveFileSeparator) + ".${ImportExportConstant.ExtensionOs6Backup}"
        }
    }
}
