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
import kotlinx.coroutines.flow.Flow
import studio.lunabee.importexport.repository.datasource.CloudBackupEngine
import studio.lunabee.importexport.repository.datasource.CloudBackupLocalDataSource
import studio.lunabee.onesafe.importexport.model.CloudBackup
import studio.lunabee.onesafe.importexport.model.LocalBackup
import studio.lunabee.onesafe.importexport.repository.CloudBackupRepository
import java.io.File
import javax.inject.Inject

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
        description: String,
    ): Flow<LBFlowResult<CloudBackup>> =
        driveEngine.uploadBackup(backup, description).mapResult { cloudBackup ->
            localCloudBackupDatasource.saveCloudBackup(cloudBackup)
        }

    override fun downloadBackup(backup: CloudBackup, file: File): Flow<LBFlowResult<LocalBackup>> =
        driveEngine.downloadBackup(backup, file)

    override fun deleteBackup(backup: CloudBackup): Flow<LBFlowResult<Unit>> {
        return driveEngine.deleteBackup(backup).mapResult {
            localCloudBackupDatasource.deleteCloudBackup(backup.id)
        }
    }
}
