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
 * Created by Lunabee Studio / Date - 10/2/2023 - for the oneSafe6 SDK.
 * Last modified 10/2/23, 8:54 AM
 */

package studio.lunabee.onesafe.importexport.usecase

import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.lunabee.onesafe.domain.repository.BackupRepository
import studio.lunabee.onesafe.importexport.model.Backup
import studio.lunabee.onesafe.importexport.model.ImportExportConstant
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException
import javax.inject.Inject

private val log = LBLogger.get<GetBackupsUseCase>()

/**
 * Retrieve all internal backups ordered by date (latest first)
 */
class GetBackupsUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
) {
    operator fun invoke(): List<Backup> {
        return backupRepository.getBackups().toBackups()
    }

    fun flow(): Flow<List<Backup>> = backupRepository.getBackupsFlow().map {
        it.toBackups()
    }

    private fun List<File>.toBackups() = filter { backupFile ->
        backupFile.isFile && backupFile.extension == ImportExportConstant.ExtensionOs6Backup
    }.mapNotNull { backupFile ->
        val chunks = backupFile.nameWithoutExtension.split(ImportExportConstant.ArchiveFileSeparator)
        if (chunks.size == 3) {
            try {
                val date = LocalDate.parse(chunks[1], ImportExportConstant.ArchiveDateFormatter)
                val time = LocalTime.parse(chunks[2], ImportExportConstant.ArchiveTimeFormatter)
                Backup(
                    date = LocalDateTime.of(date, time),
                    file = backupFile,
                )
            } catch (e: DateTimeParseException) {
                log.e(e)
                null
            }
        } else {
            log.e("Unexpected backup name (chunks size != 3)")
            null
        }
    }.sortedDescending()
}
