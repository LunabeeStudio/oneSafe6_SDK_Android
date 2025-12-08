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
 */

package studio.lunabee.onesafe.importexport.usecase

import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lblogger.LBLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import studio.lunabee.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.domain.Constant
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.model.importexport.OSArchiveKind
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.qualifier.InternalDir
import studio.lunabee.onesafe.domain.repository.FileRepository
import studio.lunabee.onesafe.domain.repository.IconRepository
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.repository.SafeSettingsRepository
import studio.lunabee.onesafe.domain.usecase.ZipFolderUseCase
import studio.lunabee.onesafe.domain.usecase.item.RelinkFilesUseCase
import studio.lunabee.onesafe.domain.utils.CrossSafeData
import studio.lunabee.onesafe.error.OSImportExportError
import studio.lunabee.onesafe.importexport.engine.BackupExportEngine
import studio.lunabee.onesafe.importexport.model.ExportData
import studio.lunabee.onesafe.importexport.model.ExportItem
import studio.lunabee.onesafe.importexport.model.ExportSuccessResult
import studio.lunabee.onesafe.importexport.model.ImportExportConstant
import studio.lunabee.onesafe.importexport.model.LocalBackup
import studio.lunabee.onesafe.importexport.repository.ImportExportBubblesRepository
import java.io.File
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.random.Random
import kotlin.random.nextInt

private val logger = LBLogger.get<ExportBackupUseCase>()

// TODO <backup> handle SuccessResult in CloudAutoBackupUseCase/Worker and LocalAutoBackupUseCase/Worker
class ExportBackupUseCase @Inject constructor(
    private val zipFolderUseCase: ZipFolderUseCase,
    private val safeItemRepository: SafeItemRepository,
    private val safeItemKeyRepository: SafeItemKeyRepository,
    private val safeItemFieldRepository: SafeItemFieldRepository,
    private val iconRepository: IconRepository,
    private val fileRepository: FileRepository,
    private val contactRepository: ContactRepository,
    private val contactKeyRepository: ContactKeyRepository,
    private val importExportBubblesRepository: ImportExportBubblesRepository,
    private val settingsRepository: SafeSettingsRepository,
    private val clock: Clock,
    private val safeRepository: SafeRepository,
    private val cryptoRepository: MainCryptoRepository,
    private val relinkFilesUseCase: RelinkFilesUseCase,
    @param:InternalDir(InternalDir.Type.Logs) private val logDir: File,
) {
    private val logFile: File = File(logDir, ImportExportConstant.BackupLogFilename)

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        exportEngine: BackupExportEngine,
        archiveExtractedDirectory: File,
        safeId: SafeId,
    ): Flow<LBFlowResult<SuccessResult>> = flow {
        logDir.mkdirs()
        logFile.delete()

        val startInstant = Instant.now(clock)
        appendLog("Backup started")
        emit(LBFlowResult.Loading(progress = 0f))

        try {
            val safeItemsWithKeys = safeItemRepository.getAllSafeItems(safeId).associate { safeItem ->
                ExportItem(safeItem) to safeItemKeyRepository.getSafeItemKey(safeItem.id)
            }
            appendLog("Collected ${safeItemsWithKeys.size} items for backup")

            val contactsWithKeys = contactRepository
                .getAllContactsFlow(DoubleRatchetUUID(safeId.id))
                .first()
                .associateWith { contact ->
                    contactKeyRepository.getContactLocalKey(contact.id)
                }
            appendLog("Collected ${contactsWithKeys.size} contacts for backup")

            val sanityCheckErrors = runSanityCheck(safeId)

            val icons = iconRepository.getIcons(safeId)
            appendLog("Collected ${icons.size} icons for backup")

            val files = fileRepository.getFiles(safeId)
            appendLog("Collected ${files.size} files for backup")

            val data = ExportData(
                safeItemsWithKeys = safeItemsWithKeys,
                safeItemFields = safeItemFieldRepository.getAllSafeItemFields(safeId),
                icons = icons,
                files = files,
                bubblesContactsWithKey = contactsWithKeys,
                bubblesMessages = importExportBubblesRepository.getAllByContactList(contactsWithKeys.keys.map { it.id }),
                bubblesConversation = importExportBubblesRepository
                    .getEncConversations(contactsWithKeys.keys.map { it.id }),
            )

            val now = Instant.now(clock)
            val createBackupContentFlow = exportEngine.createExportArchiveContent(
                dataHolderFolder = archiveExtractedDirectory,
                data = data,
                archiveKind = OSArchiveKind.Backup,
                safeId = safeId,
            )

            val zipFlow = createBackupContentFlow.transform { backupResult ->
                when (backupResult) {
                    is LBFlowResult.Failure -> {
                        appendLog("Export content creation failed", backupResult.throwable)
                        emit(LBFlowResult.Failure(throwable = backupResult.throwable))
                    }
                    is LBFlowResult.Loading -> emit(LBFlowResult.Loading(progress = backupResult.progress))
                    is LBFlowResult.Success -> {
                        emitAll(zipBackup(backupResult, archiveExtractedDirectory, now, safeId))
                    }
                }
            }

            val fullFlow = zipFlow.transform { zipResult ->
                when (zipResult) {
                    is LBFlowResult.Failure -> emit(zipResult)
                    is LBFlowResult.Loading -> emit(zipResult)
                    is LBFlowResult.Success -> {
                        if (sanityCheckErrors.isNotEmpty()) {
                            val successData = SuccessResult.WithErrors(
                                zipResult.successData.localBackup,
                                sanityCheckErrors,
                            )
                            emit(LBFlowResult.Success(successData))
                        } else {
                            emit(zipResult)
                        }
                    }
                }
            }

            emitAll(fullFlow)

            val elapsed = Duration.between(startInstant, Instant.now(clock))
            appendLog("Backup done (elapsed=$elapsed)")
        } catch (t: Throwable) {
            val elapsed = Duration.between(startInstant, Instant.now(clock))
            appendLog("Backup failed (elapsed=$elapsed)", t)
            throw t
        }
    }

    private suspend fun runSanityCheck(
        safeId: SafeId,
    ): List<OSImportExportError.Code> = runCatching {
        safeRepository.currentSafeIdOrNull()?.let {
            val icons = iconRepository.getIcons(safeId)
            val files = fileRepository.getFiles(safeId)

            val errors = backupSanityChek(safeId = it, icons = icons, files = files)
            if (errors.contains(OSImportExportError.Code.BACKUP_SANITY_CHECK_RECOVERABLE_ERROR_FOUND)) {
                appendLog("Recoverable error detected, try to relink before backup")
                relinkFilesUseCase().data?.readLines()?.forEach {
                    appendLog("\t[RELINK] $it")
                }

                backupSanityChek(
                    safeId = it,
                    icons = iconRepository.getIcons(safeId),
                    files = fileRepository.getFiles(safeId),
                ) // re-run sanity check to get remaining errors
            } else {
                errors
            }
        } ?: emptyList()
    }.onFailure { e ->
        logger.e("Fail to run backup sanity check", e)
        appendLog("backup sanity check failed", e)
    }.getOrElse { emptyList() }

    private fun zipBackup(
        backupResult: LBFlowResult.Success<ExportSuccessResult>,
        archiveExtractedDirectory: File,
        now: Instant,
        safeId: SafeId,
    ): Flow<LBFlowResult<SuccessResult>> {
        val backupErrors = (backupResult.successData as? ExportSuccessResult.Failure)?.errors.orEmpty()
        val zipFLow = zipFolderUseCase(
            inputFolder = archiveExtractedDirectory,
            outputZipFile = File(archiveExtractedDirectory, buildArchiveName(now)),
            deleteFiles = true,
        ).map { zipResult ->
            when (zipResult) {
                is LBFlowResult.Failure -> {
                    appendLog("Zipping archive failed", zipResult.throwable)
                    LBFlowResult.Failure(throwable = zipResult.throwable)
                }
                is LBFlowResult.Loading -> LBFlowResult.Loading(progress = zipResult.progress)
                is LBFlowResult.Success -> {
                    val localBackup = LocalBackup(now, zipResult.successData, safeId)
                    settingsRepository.setLastExportDate(Instant.now(clock), safeId = safeId)
                    appendLog("Zipping succeed: zip=${zipResult.successData.absolutePath}")
                    val backupResult = if (backupErrors.isEmpty()) {
                        SuccessResult.WithoutError(localBackup)
                    } else {
                        SuccessResult.WithErrors(localBackup, backupErrors.map { it.code })
                    }
                    LBFlowResult.Success(backupResult)
                }
            }
        }
        return zipFLow
    }

    private fun appendLog(message: String, throwable: Throwable? = null) {
        runCatching {
            logFile.appendText("${Instant.now(clock)}: $message\n")
            throwable?.let {
                logger.e(message, throwable)
                logFile.appendText(it.stackTraceToString() + "\n")
            } ?: logger.v(message)
        }
    }

    // TODO factorize with BuildDiagnosticUseCase (create model to store the report?)
    private suspend fun backupSanityChek(safeId: SafeId, icons: Set<File>, files: Set<File>): List<OSImportExportError.Code> {
        val retErrors = mutableListOf<OSImportExportError.Code>()

        val filesFromFileTable = files.mapTo(hashSetOf()) { file -> file.name }
        val iconsFromFileTable = icons.mapTo(hashSetOf()) { icon -> icon.name }
        val filesFromField = hashSetOf<String>()
        val iconsFromItem = hashSetOf<String>()

        @OptIn(CrossSafeData::class)
        val storageFiles = fileRepository.getAllFiles().mapTo(hashSetOf()) { file -> file.name }

        @OptIn(CrossSafeData::class)
        val storageIcons = iconRepository.getAllIcons().mapTo(hashSetOf()) { file -> file.name }

        val items = safeItemRepository.getAllSafeItems(safeId)
        items.forEach { item ->
            val key = safeItemKeyRepository.getSafeItemKey(item.id)
            val fields = safeItemFieldRepository.getSafeItemFields(item.id)
            val kindEncEntries = fields.map { field ->
                field.encKind?.let { DecryptEntry(it, SafeItemFieldKind::class) }
            }

            @Suppress("UNCHECKED_CAST")
            val kinds = cryptoRepository.decrypt(key, kindEncEntries) as List<SafeItemFieldKind>
            val valueEncEntries = fields
                .zip(kinds)
                .mapNotNull { (field, kind) ->
                    if (kind.isKindFile()) {
                        field.encValue?.let { DecryptEntry(it, String::class) }
                    } else {
                        null
                    }
                }

            if (valueEncEntries.isNotEmpty()) {
                @Suppress("UNCHECKED_CAST")
                val values = cryptoRepository.decrypt(key, valueEncEntries) as List<String>
                values.forEach { fileValue ->
                    val fileId = fileValue.substringBefore(Constant.FileTypeExtSeparator)
                    filesFromField += fileId
                }
            }

            item.iconId?.let { iconId ->
                iconsFromItem += iconId.toString()
            }
        }

        if (!filesFromFileTable.containsAll(filesFromField)) {
            val missing = (filesFromField - filesFromFileTable)
            val msg = "${missing.size} files are referenced in fields but not in file table ❌"
            logger.e(msg)
            appendLog(msg)
            retErrors += List(missing.size) { OSImportExportError.Code.BACKUP_SANITY_CHECK_RECOVERABLE_ERROR_FOUND }
        }

        if (!filesFromField.containsAll(filesFromFileTable)) {
            val extra = (filesFromFileTable - filesFromField)
            val msg = "${extra.size} files are referenced in file table but not in fields ❌"
            logger.e(msg)
            appendLog(msg)
        }

        if (!iconsFromFileTable.containsAll(iconsFromItem)) {
            val missingIcons = (iconsFromItem - iconsFromFileTable)
            val msg = "${missingIcons.size} icons are referenced in item but not in file table ❌"
            logger.e(msg)
            appendLog(msg)
            retErrors += List(missingIcons.size) { OSImportExportError.Code.BACKUP_SANITY_CHECK_RECOVERABLE_ERROR_FOUND }
        }

        if (!iconsFromItem.containsAll(iconsFromFileTable)) {
            val extraIcons = (iconsFromFileTable - iconsFromItem)
            val msg = "${extraIcons.size} icons are referenced in file table but not in item ❌"
            logger.e(msg)
            appendLog(msg)
        }

        if (!storageFiles.containsAll(filesFromField)) {
            val missingFiles = filesFromField.count { it !in storageFiles }
            val msg = "$missingFiles files are missing in storage ❌"
            logger.e(msg)
            appendLog(msg)
            retErrors += List(missingFiles) { OSImportExportError.Code.BACKUP_SANITY_CHECK_ERROR_FOUND }
        }

        if (!storageIcons.containsAll(iconsFromItem)) {
            val missingIcons = iconsFromItem.count { it !in storageIcons }
            val msg = "$missingIcons icons are missing in storage ❌"
            appendLog(msg)
            logger.e(msg)
            retErrors += List(missingIcons) { OSImportExportError.Code.BACKUP_SANITY_CHECK_ERROR_FOUND }
        }

        return retErrors
    }

    sealed interface SuccessResult {
        val localBackup: LocalBackup

        data class WithoutError(override val localBackup: LocalBackup) : SuccessResult

        data class WithErrors(
            override val localBackup: LocalBackup,
            val errorCodes: List<OSImportExportError.Code>,
        ) : SuccessResult
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
