/*
 * Copyright (c) 2023-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 7/9/2024 - for the oneSafe6 SDK.
 * Last modified 7/8/24, 9:05 PM
 */

package studio.lunabee.onesafe.migration.migration

import android.content.Context
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import dagger.hilt.android.qualifiers.ApplicationContext
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.importexport.usecase.DeleteOldLocalBackupsUseCase
import studio.lunabee.onesafe.migration.MigrationSafeData0
import studio.lunabee.onesafe.migration.utils.MigrationGetSafeIdBeforeV14UseCase
import studio.lunabee.onesafe.storage.dao.BackupDao
import studio.lunabee.onesafe.storage.model.RoomLocalBackup
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

private val logger = LBLogger.get<MigrationFromV4ToV5>()

/**
 * Get locals backup (auto backups) and insert them in database
 */
class MigrationFromV4ToV5 @Inject constructor(
    @ApplicationContext context: Context,
    private val backupDao: BackupDao,
    private val deleteOldLocalBackupsUseCase: DeleteOldLocalBackupsUseCase,
    private val clock: Clock,
    private val safeRepository: SafeRepository,
    private val migrationGetSafeIdBeforeV14UseCase: MigrationGetSafeIdBeforeV14UseCase,
) : AppMigration0(4, 5) {
    private val backupsDir: File = File(context.filesDir, backupDir)

    override suspend fun migrate(migrationSafeData: MigrationSafeData0): LBResult<Unit> {
        val safeId = migrationGetSafeIdBeforeV14UseCase()
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
                        logger.e(e)
                        null
                    }
                } else {
                    logger.e("Unexpected backup name (chunks size != 3)")
                    null
                } ?: Instant.now(clock) // fallback to now to preserve backup

                logger.i("Migrate backup ${file.name}")
                RoomLocalBackup(
                    id = file.name,
                    localFile = file,
                    date = date,
                    safeId = safeRepository.currentSafeId(),
                )
            }
            backupDao.insertLocals(roomBackups)
            deleteOldLocalBackupsUseCase(safeId) // make sure we only have the expected backup count
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
