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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import studio.lunabee.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.domain.model.importexport.OSArchiveKind
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.repository.FileRepository
import studio.lunabee.onesafe.domain.repository.IconRepository
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.importexport.engine.BackupExportEngine
import studio.lunabee.onesafe.importexport.model.ExportData
import studio.lunabee.onesafe.importexport.model.ExportItem
import studio.lunabee.onesafe.importexport.model.ImportExportConstant
import studio.lunabee.onesafe.importexport.model.LocalBackup
import studio.lunabee.onesafe.importexport.repository.ImportExportBubblesRepository
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.random.Random
import kotlin.random.nextInt

class ExportBackupUseCase @Inject constructor(
    private val archiveZipUseCase: ArchiveZipUseCase,
    private val safeItemRepository: SafeItemRepository,
    private val safeItemKeyRepository: SafeItemKeyRepository,
    private val safeItemFieldRepository: SafeItemFieldRepository,
    private val iconRepository: IconRepository,
    private val fileRepository: FileRepository,
    private val contactRepository: ContactRepository,
    private val contactKeyRepository: ContactKeyRepository,
    private val importExportBubblesRepository: ImportExportBubblesRepository,
    private val dismissPreventionWarningCtaUseCase: DismissPreventionWarningCtaUseCase,
    private val clock: Clock,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        exportEngine: BackupExportEngine,
        archiveExtractedDirectory: File,
        safeId: SafeId,
    ): Flow<LBFlowResult<LocalBackup>> {
        return flow {
            val safeItemsWithKeys = safeItemRepository.getAllSafeItems(safeId).associate { safeItem ->
                ExportItem(safeItem) to safeItemKeyRepository.getSafeItemKey(safeItem.id)
            }
            val contactsWithKeys = contactRepository.getAllContactsFlow(DoubleRatchetUUID(safeId.id)).first().associateWith { contact ->
                contactKeyRepository.getContactLocalKey(contact.id)
            }
            val data = ExportData(
                safeItemsWithKeys = safeItemsWithKeys,
                safeItemFields = safeItemFieldRepository.getAllSafeItemFields(safeId),
                icons = iconRepository.getIcons(safeId),
                files = fileRepository.getFiles(safeId),
                bubblesContactsWithKey = contactsWithKeys,
                bubblesMessages = importExportBubblesRepository.getAllByContactList(contactsWithKeys.keys.map { it.id }),
                bubblesConversation = importExportBubblesRepository.getEncConversations(contactsWithKeys.keys.map { it.id }),
            )

            val now = Instant.now(clock)
            emitAll(
                exportEngine.createExportArchiveContent(
                    dataHolderFolder = archiveExtractedDirectory,
                    data = data,
                    archiveKind = OSArchiveKind.Backup,
                    safeId = safeId,
                ).flatMapLatest { result ->
                    when (result) {
                        is LBFlowResult.Failure -> flowOf(LBFlowResult.Failure(throwable = result.throwable))
                        is LBFlowResult.Loading -> flowOf(LBFlowResult.Loading(progress = result.progress))
                        // Zip if creation is success. Will return the progress of zip.
                        is LBFlowResult.Success -> archiveZipUseCase(
                            folderToZip = archiveExtractedDirectory,
                            outputZipFile = File(archiveExtractedDirectory, buildArchiveName(now)),
                        ).map { zipResult ->
                            when (zipResult) {
                                is LBFlowResult.Failure -> LBFlowResult.Failure(throwable = zipResult.throwable)
                                is LBFlowResult.Loading -> LBFlowResult.Loading(progress = zipResult.progress)
                                is LBFlowResult.Success -> {
                                    val localBackup = LocalBackup(now, zipResult.successData, safeId)
                                    // Easy way to consider an export as a backup to remove display of prevention warning.
                                    dismissPreventionWarningCtaUseCase()
                                    LBFlowResult.Success(localBackup)
                                }
                            }
                        }
                    }
                },
            )
        }
    }

    companion object {
        /**
         * Return final archive name (i.e oneSafe-20221217-134622-234324.os6lsb)
         */
        private fun buildArchiveName(now: Instant): String {
            val localDateTime = LocalDateTime.ofInstant(now, ZoneOffset.UTC)
            return listOf(
                ImportExportConstant.ArchiveFilePrefix,
                DateTimeFormatter.BASIC_ISO_DATE.format(localDateTime), // i.e 20230113
                ImportExportConstant.ArchiveTimeFormatter.format(localDateTime), // i.e 134602
                Random.nextInt(100_000..999_999), // i.e random string to avoid backup override with multi-safe
            ).joinToString(ImportExportConstant.ArchiveFileSeparator) + ".${ImportExportConstant.ExtensionOs6Backup}"
        }
    }
}
