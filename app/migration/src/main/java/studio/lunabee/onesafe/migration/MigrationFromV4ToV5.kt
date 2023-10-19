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
 * Created by Lunabee Studio / Date - 8/31/2023 - for the oneSafe6 SDK.
 * Last modified 31/08/2023 15:18
 */

package studio.lunabee.onesafe.migration

import android.content.Context
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.qualifiers.ApplicationContext
import studio.lunabee.onesafe.importexport.usecase.DeleteOldLocalBackupsUseCase
import studio.lunabee.onesafe.storage.dao.BackupDao
import studio.lunabee.onesafe.storage.model.RoomLocalBackup
import timber.log.Timber
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

/**
 * Get locals backup (auto backups) and insert them in database
 */
class MigrationFromV4ToV5 @Inject constructor(
    @ApplicationContext context: Context,
    private val backupDao: BackupDao,
    private val deleteOldLocalBackupsUseCase: DeleteOldLocalBackupsUseCase,
) {
    private val backupsDir: File = File(context.filesDir, backupDir)

    suspend operator fun invoke(): LBResult<Unit> {
        val backupFiles = backupsDir.listFiles { _, name ->
            name.startsWith(ArchiveFilePrefix) && name.endsWith(ExtensionOs6Backup)
        }
        return if (backupFiles?.isNotEmpty() == true) {
            val roomBackups = backupFiles.map { file ->
                val chunks = file.nameWithoutExtension.split(ArchiveFileSeparator)
                val date = if (chunks.size == 3) {
                    try {
                        val date = LocalDate.parse(chunks[1], ArchiveDateFormatter)
                        val time = LocalTime.parse(chunks[2], ArchiveTimeFormatter)
                        LocalDateTime.of(date, time).toInstant(ZoneOffset.UTC)
                    } catch (e: DateTimeParseException) {
                        Timber.e(e)
                        null
                    }
                } else {
                    Timber.e("Unexpected backup name (chunks size != 3)")
                    null
                } ?: Instant.now() // fallback to now to preserve backup

                Timber.i("Migrate backup ${file.name}")
                RoomLocalBackup(id = file.name, localFile = file, date = date)
            }
            backupDao.insertLocals(roomBackups)
            deleteOldLocalBackupsUseCase() // make sure we only have the expected backup count
            LBResult.Success(Unit)
        } else {
            LBResult.Success(Unit)
        }
    }

    companion object {
        const val backupDir: String = "backups"
        const val ExtensionOs6Backup: String = "os6lsb"
        const val ArchiveFilePrefix: String = "oneSafe"
        const val ArchiveFileSeparator: String = "-"
        val ArchiveTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HHmmss")
        val ArchiveDateFormatter: DateTimeFormatter = DateTimeFormatter.BASIC_ISO_DATE
    }
}
