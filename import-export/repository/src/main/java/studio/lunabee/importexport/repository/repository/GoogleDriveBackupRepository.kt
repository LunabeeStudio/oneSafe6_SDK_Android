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
 * Created by Lunabee Studio / Date - 10/10/2023 - for the oneSafe6 SDK.
 * Last modified 10/10/23, 9:33 AM
 */

package studio.lunabee.importexport.repository.repository

import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBFlowResult.Companion.mapResult
import com.lunabee.lbcore.model.LBFlowResult.Companion.unit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import studio.lunabee.importexport.repository.datasource.CloudBackupEngine
import studio.lunabee.importexport.repository.datasource.CloudBackupLocalDataSource
import studio.lunabee.onesafe.combine
import studio.lunabee.onesafe.error.OSImportExportError
import studio.lunabee.onesafe.importexport.model.CloudBackup
import studio.lunabee.onesafe.importexport.model.LocalBackup
import studio.lunabee.onesafe.importexport.repository.CloudBackupRepository
import java.io.InputStream
import javax.inject.Inject

// TODO <AutoBackup> cache refreshBackupList to avoid multiple call too close

class GoogleDriveBackupRepository @Inject constructor(
    private val driveEngine: CloudBackupEngine,
    private val localCloudBackupDatasource: CloudBackupLocalDataSource,
) : CloudBackupRepository {
    override fun refreshBackupList(): Flow<LBFlowResult<List<CloudBackup>>> =
        driveEngine.fetchBackupList().mapResult { cloudBackups ->
            localCloudBackupDatasource.refreshCloudBackups(cloudBackups)
            cloudBackups
        }

    override fun uploadBackup(
        backup: LocalBackup,
    ): Flow<LBFlowResult<CloudBackup>> =
        driveEngine.uploadBackup(backup).mapResult { cloudBackup ->
            localCloudBackupDatasource.saveCloudBackup(cloudBackup)
        }

    override fun uploadBackup(backups: List<LocalBackup>): Flow<LBFlowResult<List<CloudBackup?>>> =
        backups.map { backup ->
            uploadBackup(backup)
        }.combine()

    override fun deleteBackup(backup: CloudBackup): Flow<LBFlowResult<Unit>> {
        return driveEngine.deleteBackup(backup).mapResult {
            localCloudBackupDatasource.deleteCloudBackup(backup.id)
        }
    }

    override fun deleteBackup(backups: List<CloudBackup>): Flow<LBFlowResult<Unit>> =
        backups.map(::deleteBackup).combine().unit()

    override suspend fun getBackups(): List<CloudBackup> = localCloudBackupDatasource.getCloudBackups()

    override fun getBackupsFlow(): Flow<List<CloudBackup>> {
        return localCloudBackupDatasource.getCloudBackupsFlow()
    }

    override fun getInputStream(backupId: String): Flow<LBFlowResult<InputStream>> = flow {
        localCloudBackupDatasource.getRemoteId(backupId)?.let { remoteId ->
            emitAll(driveEngine.getInputStream(remoteId))
        } ?: emit(LBFlowResult.Failure(OSImportExportError(OSImportExportError.Code.BACKUP_ID_NOT_FOUND_IN_DB)))
    }

    override suspend fun getLatestBackup(): CloudBackup? =
        localCloudBackupDatasource.getLatestBackup()

    override fun getLatestBackupFlow(): Flow<CloudBackup?> =
        localCloudBackupDatasource.getLatestBackupFlow()

    override suspend fun clearBackupsLocally() {
        localCloudBackupDatasource.deleteAll()
    }
}
