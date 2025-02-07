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

package studio.lunabee.importexport.datasource

import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.importexport.model.CloudBackup
import studio.lunabee.onesafe.importexport.model.CloudInfo
import studio.lunabee.onesafe.importexport.model.LocalBackup
import java.io.InputStream
import java.net.URI

interface CloudBackupEngine {
    fun fetchBackupList(safeId: SafeId): Flow<LBFlowResult<List<CloudBackup>>>
    fun uploadBackup(localBackup: LocalBackup): Flow<LBFlowResult<CloudBackup>>
    fun getInputStream(remoteId: String, safeId: SafeId): Flow<LBFlowResult<InputStream>>
    fun deleteBackup(cloudBackup: CloudBackup): Flow<LBFlowResult<Unit>>
    fun setupAccount(accountName: String, safeId: SafeId): Flow<LBFlowResult<Unit>>
    fun getCloudInfoFlow(safeId: SafeId): Flow<CloudInfo>

    /**
     * Retrieve the first cloud folder URI available (to be use before login)
     */
    suspend fun getFirstCloudFolderAvailable(): URI?
}
