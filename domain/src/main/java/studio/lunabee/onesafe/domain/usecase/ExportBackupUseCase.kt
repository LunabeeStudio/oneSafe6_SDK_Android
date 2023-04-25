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

package studio.lunabee.onesafe.domain.usecase

import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.onesafe.domain.usecase.archive.ArchiveZipUseCase
import studio.lunabee.onesafe.domain.engine.ExportEngine
import studio.lunabee.onesafe.domain.model.importexport.OSArchiveKind
import studio.lunabee.onesafe.domain.repository.IconRepository
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ExportBackupUseCase @Inject constructor(
    private val exportEngine: ExportEngine,
    private val archiveZipUseCase: ArchiveZipUseCase,
    private val safeItemRepository: SafeItemRepository,
    private val safeItemKeyRepository: SafeItemKeyRepository,
    private val safeItemFieldRepository: SafeItemFieldRepository,
    private val iconRepository: IconRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        archiveExtractedDirectory: File,
    ): Flow<LBFlowResult<File>> {
        return flow {
            val safeItemsWithKeys = safeItemRepository.getAllSafeItems().associateWith { safeItem ->
                safeItemKeyRepository.getSafeItemKey(safeItem.id)
            }
            val fields = safeItemFieldRepository.getAllSafeItemFields()
            val icons = iconRepository.getIcons()

            emitAll(
                exportEngine.createExportArchiveContent(
                    dataHolderFolder = archiveExtractedDirectory,
                    safeItemsWithKeys = safeItemsWithKeys,
                    safeItemFields = fields,
                    icons = icons,
                    archiveKind = OSArchiveKind.Backup,
                ).flatMapLatest { result ->
                    when (result) {
                        is LBFlowResult.Failure -> flowOf(LBFlowResult.Failure(throwable = result.throwable))
                        is LBFlowResult.Loading -> flowOf(LBFlowResult.Loading(progress = result.progress))
                        // Zip if creation is success. Will return the progress of zip.
                        is LBFlowResult.Success -> archiveZipUseCase(
                            folderToZip = archiveExtractedDirectory,
                            outputZipFile = File(archiveExtractedDirectory, buildArchiveName()),
                        )
                    }
                },
            )
        }
    }

    companion object {
        private const val ArchiveFilePrefix: String = "oneSafe"
        private const val ArchiveFileExtension: String = ".os6lsb"
        private const val ArchiveFileSeparator: String = "-"
        private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HHmmss")

        /**
         * Return final archive name (i.e oneSafe-20221212-134622.os6lsb)
         */
        private fun buildArchiveName(): String {
            return listOf(
                ArchiveFilePrefix,
                DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.now()), // i.e 20230113
                TimeFormatter.format(LocalTime.now()), // i.e 134602
            ).joinToString(ArchiveFileSeparator) + ArchiveFileExtension
        }
    }
}
