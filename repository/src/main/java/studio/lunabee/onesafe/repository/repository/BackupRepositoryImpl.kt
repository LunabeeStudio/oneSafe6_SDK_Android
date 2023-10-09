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
 * Created by Lunabee Studio / Date - 9/28/2023 - for the oneSafe6 SDK.
 * Last modified 9/28/23, 6:50 PM
 */

package studio.lunabee.onesafe.repository.repository

import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.repository.BackupRepository
import studio.lunabee.onesafe.repository.datasource.BackupLocalDataSource
import java.io.File
import javax.inject.Inject

class BackupRepositoryImpl @Inject constructor(
    private val dataSource: BackupLocalDataSource,
) : BackupRepository {
    override fun addBackup(backupFile: File) {
        dataSource.addBackup(backupFile)
    }

    override fun getBackups(): List<File> {
        return dataSource.getBackups()
    }

    override fun getBackupsFlow(): Flow<List<File>> {
        return dataSource.getBackupsFlow()
    }
}
