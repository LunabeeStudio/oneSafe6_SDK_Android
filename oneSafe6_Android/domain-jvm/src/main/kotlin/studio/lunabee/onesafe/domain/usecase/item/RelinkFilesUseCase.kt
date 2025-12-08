/*
 * Copyright (c) 2025 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 11/20/2025 - for the oneSafe6 SDK.
 * Last modified 11/20/25, 4:07 PM
 */

package studio.lunabee.onesafe.domain.usecase.item

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.domain.Constant
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.qualifier.InternalDir
import studio.lunabee.onesafe.domain.repository.FileRepository
import studio.lunabee.onesafe.domain.repository.IconRepository
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.error.OSError
import java.io.File
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

private val logger = LBLogger.get<RelinkFilesUseCase>()

/**
 * Scan all fields of current safe to get all files referenced by the current safe and re-add them the to file table. Fix potential
 * desynchronization between file storage and file table. Also re-insert icon references if any.
 */
class RelinkFilesUseCase @Inject constructor(
    private val fileRepository: FileRepository,
    private val itemRepository: SafeItemRepository,
    private val fieldRepository: SafeItemFieldRepository,
    private val safeRepository: SafeRepository,
    private val cryptoRepository: MainCryptoRepository,
    private val keyRepository: SafeItemKeyRepository,
    private val iconRepository: IconRepository,
    @param:InternalDir(InternalDir.Type.Logs) private val logDir: File,
    private val clock: Clock,
) {
    private val logFile: File = File(logDir, LogFile)

    suspend operator fun invoke(): LBResult<File> = OSError.runCatching(
        logger = logger,
        mapErr = {
            appendLog("❌ Relink failed", it)
            it
        },
    ) {
        logFile.delete()
        logFile.parentFile?.mkdirs()

        var missingRefCount = 0
        appendLog("Relink files started")
        safeRepository.currentSafeIdOrNull()?.let { safeId ->
            val referencedFiles = fileRepository.getFiles(safeId).mapTo(hashSetOf()) { file -> file.name }
            val items = itemRepository.getAllSafeItems(safeId)
            appendLog("Found ${items.size} items")
            val globalRelinkedFiles = mutableSetOf<File>()
            items.forEach { item ->
                runCatching {
                    val key = keyRepository.getSafeItemKey(item.id)
                    val fields = fieldRepository.getSafeItemFields(item.id)
                    val kindEncEntries = fields.map { field ->
                        field.encKind?.let { DecryptEntry(it, SafeItemFieldKind::class) }
                    }

                    if (fields.isEmpty()) {
                        appendLog("No fields for item ${item.id}")
                    } else {
                        appendLog("Found ${fields.size} fields for item ${item.id}")

                        @Suppress("UNCHECKED_CAST")
                        val kinds = cryptoRepository.decrypt(key, kindEncEntries) as List<SafeItemFieldKind>
                        val encFieldMap = fields
                            .zip(kinds)
                            .associate { (field, kind) ->
                                val entry = if (kind.isKindFile()) {
                                    field.encValue?.let { DecryptEntry(it, String::class) }
                                } else {
                                    null
                                }
                                field.id to entry
                            }
                            .filter { (_, entry) -> entry != null }

                        appendLog("${encFieldMap.size}/${fields.size} fields are files")

                        if (encFieldMap.isNotEmpty()) {
                            @Suppress("UNCHECKED_CAST")
                            val plainValues = cryptoRepository.decrypt(key, encFieldMap.values) as List<String>
                            val plainFieldMap = encFieldMap.keys.zip(plainValues)
                            val relinkFiles = plainFieldMap.mapNotNull { (fieldId, fileValue) ->
                                val fileId = fileValue.substringBefore(Constant.FileTypeExtSeparator)
                                if (!referencedFiles.contains(fileId)) {
                                    appendLog("✅ Relink file $fileId of field $fieldId")
                                    fileId
                                } else {
                                    null
                                }
                            }
                            globalRelinkedFiles += runCatching { fileRepository.saveFilesRef(safeId, relinkFiles) }
                                .onFailure {
                                    appendLog("❌ Safe files ref failed", it)
                                }
                                .getOrNull()
                                .orEmpty()
                            missingRefCount += relinkFiles.size
                        }

                        item.iconId?.let { iconId ->
                            runCatching { iconRepository.saveIconRef(safeId, iconId) } // also re-link icon just in case
                                .onFailure {
                                    appendLog("❌ Save icon ref failed", it)
                                }
                                .onSuccess {
                                    appendLog("✅ Relink icon $iconId")
                                }
                        }
                    }
                }.onFailure {
                    appendLog("❌ Relink algo for item ${item.id} failed", it)
                }
            }

            if (missingRefCount > 0) {
                logger.e("Relinked $missingRefCount files")
                appendLog("✅ Relinked $missingRefCount files")
            }

            // Unexpected
            val postRefFiles = fileRepository.getFiles(safeId)
            if (!postRefFiles.containsAll(globalRelinkedFiles)) {
                val missingFiles = globalRelinkedFiles.count { it !in postRefFiles }
                appendLog("❌ Final check fail, some relinked files are still missing ($missingFiles)")
            }

            appendLog("Relink files done")
        } ?: appendLog("❌ Relink files no safe loaded")

        logFile
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

    companion object {
        const val LogFile: String = "last_relink_files.log"
    }
}
