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
 * Last modified 10/11/23, 9:56 AM
 */

package studio.lunabee.importexport.repository.datasource

import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.importexport.model.CloudBackup
import studio.lunabee.onesafe.importexport.model.LocalBackup
import java.io.File

interface CloudBackupEngine {
    fun fetchBackupList(): Flow<LBFlowResult<List<CloudBackup>>>
    fun uploadBackup(localBackup: LocalBackup, description: String): Flow<LBFlowResult<CloudBackup>>
    fun downloadBackup(cloudBackup: CloudBackup, target: File): Flow<LBFlowResult<LocalBackup>>
    fun deleteBackup(cloudBackup: CloudBackup): Flow<LBFlowResult<Unit>>
    fun setupAccount(accountName: String?)
    fun getCurrentDriveAccount(): String?
    suspend fun isAuthorized(): LBResult<Boolean>
    suspend fun authorize(isAuthorized: Boolean): LBResult<Unit>
}
