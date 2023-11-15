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
 * Last modified 10/11/23, 2:38 PM
 */

package studio.lunabee.importexport.repository.datasource

import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.importexport.model.LocalBackup

interface LocalBackupLocalDataSource {
    /**
     * Create backup entry in database and move the file to the internal backup dir
     */
    suspend fun addBackup(localBackup: LocalBackup): LocalBackup

    /**
     * Retrieve all local backups and check file existence
     */
    suspend fun getBackups(): List<LBResult<LocalBackup>>

    /**
     * Retrieve all local backups which does not have remote id
     */
    suspend fun getBackupsToUpload(): List<LBResult<LocalBackup>>

    /**
     * Same as [getBackups] with flow
     *
     * @see getBackups
     */
    fun getBackupsFlow(): Flow<List<LBResult<LocalBackup>>>

    /**
     * Remove backup entry in database and remove the file from the internal backup dir
     */
    suspend fun delete(oldBackups: List<LocalBackup>)

    /**
     * Delete all local backup entries and remove files from the internal backup dir
     */
    suspend fun deleteAll()
}
