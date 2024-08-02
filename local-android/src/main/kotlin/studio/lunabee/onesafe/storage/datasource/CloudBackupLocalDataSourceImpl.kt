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
 * Created by Lunabee Studio / Date - 10/11/2023 - for the oneSafe6 SDK.
 * Last modified 10/11/23, 10:08 AM
 */

package studio.lunabee.onesafe.storage.datasource

import com.lunabee.lbextensions.mapValues
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.lunabee.importexport.datasource.CloudBackupLocalDataSource
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.importexport.model.CloudBackup
import studio.lunabee.onesafe.storage.dao.BackupDao
import studio.lunabee.onesafe.storage.model.RoomCloudBackup
import javax.inject.Inject

class CloudBackupLocalDataSourceImpl @Inject constructor(
    private val dao: BackupDao,
) : CloudBackupLocalDataSource {
    override suspend fun saveCloudBackup(backup: CloudBackup): CloudBackup = backup.also {
        dao.insert(RoomCloudBackup.fromBackup(backup))
    }

    override suspend fun refreshCloudBackups(backups: List<CloudBackup>) {
        dao.refreshCloudBackups(backups)
    }

    override suspend fun getCloudBackups(safeId: SafeId): List<CloudBackup> =
        dao.getAllCloud(safeId).map(RoomCloudBackup::toBackup)

    override fun getCloudBackupsFlow(safeId: SafeId): Flow<List<CloudBackup>> =
        dao.getCloudBackupsFlow(safeId).mapValues(RoomCloudBackup::toBackup)

    override suspend fun deleteCloudBackup(id: String) {
        dao.deleteCloudBackup(id)
    }

    override suspend fun getRemoteId(backupId: String): String? =
        dao.getRemoteId(backupId)

    override suspend fun getLatestBackup(safeId: SafeId): CloudBackup? =
        dao.getLatestCloudBackup(safeId)?.let(RoomCloudBackup::toBackup)

    override fun getLatestBackupFlow(safeId: SafeId): Flow<CloudBackup?> =
        dao.getLatestCloudBackupFlow(safeId).map { it?.toBackup() }

    override suspend fun deleteAll(safeId: SafeId) {
        dao.deleteAllCloudBackup(safeId)
    }
}
