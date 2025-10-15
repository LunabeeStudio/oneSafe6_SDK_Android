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

package studio.lunabee.onesafe.storage.datasource

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbextensions.mapValues
import kotlinx.coroutines.flow.Flow
import studio.lunabee.importexport.datasource.LocalBackupLocalDataSource
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.qualifier.InternalDir
import studio.lunabee.onesafe.error.OSImportExportError
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.importexport.model.LocalBackup
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.storage.MainDatabase
import studio.lunabee.onesafe.storage.dao.BackupDao
import studio.lunabee.onesafe.storage.model.RoomLocalBackup
import studio.lunabee.onesafe.storage.utils.TransactionProvider
import java.io.File
import java.io.IOException
import javax.inject.Inject

class LocalBackupLocalDataSourceImpl @Inject constructor(
    @InternalDir(InternalDir.Type.Backups) backupsDir: File,
    private val backupDao: BackupDao,
    private val transactionProvider: TransactionProvider<MainDatabase>,
) : LocalBackupLocalDataSource {

    private val backupsDir: File = backupsDir
        get() {
            if (!field.exists()) field.mkdirs()
            return field
        }

    override suspend fun addBackup(localBackup: LocalBackup): LocalBackup = try {
        val finalBackup = localBackup.copy(file = File(backupsDir, localBackup.file.name))
        transactionProvider.runAsTransaction {
            localBackup.file.copyTo(finalBackup.file)
            backupDao.insert(RoomLocalBackup.fromBackup(finalBackup))
        }
        finalBackup
    } catch (e: IOException) {
        throw OSStorageError.Code.UNKNOWN_FILE_ERROR.get(cause = e)
    }

    override suspend fun getBackups(safeId: SafeId): List<LBResult<LocalBackup>> =
        backupDao.getAllLocal(safeId).map(::transformBackup)

    override suspend fun getBackupsToUpload(safeId: SafeId): List<LBResult<LocalBackup>> =
        backupDao.getAllLocalWithoutRemote(safeId).map(::transformBackup)

    override fun getBackupsFlow(safeId: SafeId): Flow<List<LBResult<LocalBackup>>> =
        backupDao.getAllLocalAsFlow(safeId).mapValues(::transformBackup)

    private fun transformBackup(roomBackup: RoomLocalBackup): LBResult<LocalBackup> {
        val backup = roomBackup.toBackup()
        return if (backup.file.exists()) {
            LBResult.Success(backup)
        } else {
            LBResult.Failure(OSStorageError.Code.MISSING_BACKUP_FILE.get(), backup)
        }
    }

    override suspend fun delete(oldBackups: List<LocalBackup>) {
        transactionProvider.runAsTransaction {
            oldBackups.forEach { localBackup ->
                if (localBackup.file.delete() || !localBackup.file.exists()) {
                    backupDao.deleteLocalBackup(localBackup.id)
                } else if (localBackup.file.exists()) {
                    throw OSImportExportError(OSImportExportError.Code.BACKUP_FILE_DELETE_FAILED)
                }
            }
        }
    }

    override suspend fun deleteAll(safeId: SafeId) {
        transactionProvider.runAsTransaction {
            val backups = backupDao.getAllLocal(safeId)
            backups.forEach { localBackup ->
                if (localBackup.localFile.delete()) {
                    backupDao.deleteLocalBackup(localBackup.id)
                } else if (localBackup.localFile.exists()) {
                    throw OSImportExportError(OSImportExportError.Code.BACKUP_FILE_DELETE_FAILED)
                }
            }
        }
    }

    override suspend fun getFile(backupId: String): File? =
        backupDao.getFile(backupId)?.takeIf { it.exists() }

    override fun hasBackup(safeId: SafeId): Flow<Boolean> =
        backupDao.hasLocalBackup(safeId)
}
