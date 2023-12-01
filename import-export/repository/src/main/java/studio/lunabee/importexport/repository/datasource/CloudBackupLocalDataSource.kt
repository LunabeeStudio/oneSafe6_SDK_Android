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
 * Last modified 10/11/23, 9:37 AM
 */

package studio.lunabee.importexport.repository.datasource

import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.importexport.model.CloudBackup

interface CloudBackupLocalDataSource {
    suspend fun saveCloudBackup(backup: CloudBackup): CloudBackup
    suspend fun refreshCloudBackups(backups: List<CloudBackup>)
    suspend fun getCloudBackups(): List<CloudBackup>
    fun getCloudBackupsFlow(): Flow<List<CloudBackup>>
    suspend fun deleteCloudBackup(id: String)
    suspend fun getRemoteId(backupId: String): String?
    suspend fun getLatestBackup(): CloudBackup?
    fun getLatestBackupFlow(): Flow<CloudBackup?>
    suspend fun deleteAll()
}
