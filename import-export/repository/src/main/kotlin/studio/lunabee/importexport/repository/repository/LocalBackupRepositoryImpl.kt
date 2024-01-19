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
 * Created by Lunabee Studio / Date - 10/11/2023 - for the oneSafe6 SDK.
 * Last modified 10/9/23, 6:29 PM
 */

package studio.lunabee.importexport.repository.repository

import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.flow.Flow
import studio.lunabee.importexport.repository.datasource.LocalBackupLocalDataSource
import studio.lunabee.onesafe.importexport.model.LocalBackup
import studio.lunabee.onesafe.importexport.repository.LocalBackupRepository
import java.io.File
import javax.inject.Inject

class LocalBackupRepositoryImpl @Inject constructor(
    private val dataSource: LocalBackupLocalDataSource,
) : LocalBackupRepository {
    override suspend fun addBackup(localBackup: LocalBackup) {
        dataSource.addBackup(localBackup)
    }

    override suspend fun getBackups(): List<LBResult<LocalBackup>> {
        return dataSource.getBackups()
    }

    override suspend fun getBackupsExcludeRemote(): List<LBResult<LocalBackup>> {
        return dataSource.getBackupsToUpload()
    }

    override fun getBackupsFlow(): Flow<List<LBResult<LocalBackup>>> {
        return dataSource.getBackupsFlow()
    }

    override suspend fun delete(backups: List<LocalBackup>) {
        dataSource.delete(backups)
    }

    override suspend fun deleteAll() {
        dataSource.deleteAll()
    }

    override suspend fun getFile(backupId: String): File? =
        dataSource.getFile(backupId)

    override fun hasBackupFlow(): Flow<Boolean> =
        dataSource.hasBackup()
}
