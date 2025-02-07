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
 * Created by Lunabee Studio / Date - 6/24/2024 - for the oneSafe6 SDK.
 * Last modified 6/20/24, 11:51 AM
 */

package studio.lunabee.importexport.repository

import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.flow.Flow
import studio.lunabee.importexport.datasource.LocalBackupCacheDataSource
import studio.lunabee.importexport.datasource.LocalBackupLocalDataSource
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.importexport.model.LocalBackup
import studio.lunabee.onesafe.importexport.repository.LocalBackupRepository
import java.io.File
import java.io.InputStream
import java.time.Instant
import javax.inject.Inject

class LocalBackupRepositoryImpl @Inject constructor(
    private val dataSource: LocalBackupLocalDataSource,
    private val cacheDataSource: LocalBackupCacheDataSource,
) : LocalBackupRepository {
    override suspend fun addBackup(localBackup: LocalBackup) {
        dataSource.addBackup(localBackup)
    }

    override suspend fun getBackups(safeId: SafeId): List<LBResult<LocalBackup>> {
        return dataSource.getBackups(safeId)
    }

    override suspend fun getBackupsExcludeRemote(safeId: SafeId): List<LBResult<LocalBackup>> {
        return dataSource.getBackupsToUpload(safeId)
    }

    override fun getBackupsFlow(safeId: SafeId): Flow<List<LBResult<LocalBackup>>> {
        return dataSource.getBackupsFlow(safeId)
    }

    override suspend fun delete(backups: List<LocalBackup>, safeId: SafeId) {
        dataSource.delete(backups)
    }

    override suspend fun deleteAll(safeId: SafeId) {
        dataSource.deleteAll(safeId)
    }

    override suspend fun getFile(backupId: String): File? =
        dataSource.getFile(backupId)

    override fun hasBackupFlow(safeId: SafeId): Flow<Boolean> =
        dataSource.hasBackup(safeId)

    override suspend fun cacheBackup(inputStream: InputStream, date: Instant): File {
        return cacheDataSource.addBackup(inputStream, date)
    }

    override suspend fun clearCachedBackup(localBackup: LocalBackup) {
        cacheDataSource.removeBackup(localBackup)
    }
}
