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
 * Created by Lunabee Studio / Date - 10/9/2023 - for the oneSafe6 SDK.
 * Last modified 10/9/23, 6:37 PM
 */

package studio.lunabee.onesafe.importexport.repository

import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.importexport.model.CloudBackup
import studio.lunabee.onesafe.importexport.model.CloudInfo
import studio.lunabee.onesafe.importexport.model.LocalBackup
import java.io.InputStream

interface CloudBackupRepository {
    fun uploadBackup(backup: LocalBackup): Flow<LBFlowResult<CloudBackup>>
    fun uploadBackup(backups: List<LocalBackup>): Flow<LBFlowResult<List<CloudBackup?>>>
    fun refreshBackupList(): Flow<LBFlowResult<List<CloudBackup>>>
    fun deleteBackup(backup: CloudBackup): Flow<LBFlowResult<Unit>>
    fun deleteBackup(backups: List<CloudBackup>): Flow<LBFlowResult<Unit>>
    suspend fun getBackups(): List<CloudBackup>
    fun getBackupsFlow(): Flow<List<CloudBackup>>
    fun getInputStream(backupId: String): Flow<LBFlowResult<InputStream>>
    suspend fun getLatestBackup(): CloudBackup?
    fun getLatestBackupFlow(): Flow<CloudBackup?>
    suspend fun clearBackupsLocally()
    fun getCloudInfo(): Flow<CloudInfo>
    fun setupAccount(accountName: String): Flow<LBFlowResult<Unit>>
}
