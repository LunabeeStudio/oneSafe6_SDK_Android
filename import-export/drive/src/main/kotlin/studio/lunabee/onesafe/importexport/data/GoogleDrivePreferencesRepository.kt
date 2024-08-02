/*
 * Copyright (c) 2023-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 6/24/2024 - for the oneSafe6 SDK.
 * Last modified 6/24/24, 9:06 AM
 */

package studio.lunabee.onesafe.importexport.data

import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safe.SafeId
import javax.inject.Inject

class GoogleDrivePreferencesRepository @Inject constructor(
    private val datasource: GoogleDriveEnginePreferencesDatasource,
) {
    fun selectedAccountFlow(safeId: SafeId): Flow<String?> = datasource.selectedAccountFlow(safeId)
    fun folderIdFlow(safeId: SafeId): Flow<String?> = datasource.folderIdFlow(safeId)
    fun folderUrlFlow(safeId: SafeId): Flow<String?> = datasource.folderUrlFlow(safeId)

    suspend fun selectedAccount(safeId: SafeId): String? = datasource.selectedDriveAccount(safeId)
    suspend fun folderId(safeId: SafeId): String? = datasource.folderId(safeId)
    suspend fun folderUrl(safeId: SafeId): String? = datasource.folderUrl(safeId)

    suspend fun setSelectedAccount(safeId: SafeId, account: String?): Unit = datasource.setDriveSelectedAccount(safeId, account)
    suspend fun setFolderId(safeId: SafeId, id: String?): Unit = datasource.setDriveFolderId(safeId, id)
    suspend fun setFolderUrl(safeId: SafeId, url: String?): Unit = datasource.setDriveFolderUrl(safeId, url)
}
