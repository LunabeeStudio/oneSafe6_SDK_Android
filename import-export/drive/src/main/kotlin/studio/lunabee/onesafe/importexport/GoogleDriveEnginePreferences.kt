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
 * Last modified 10/10/23, 5:55 PM
 */

package studio.lunabee.onesafe.importexport

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.lunabee.onesafe.setOrRemove
import javax.inject.Inject

class GoogleDriveEnginePreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val selectedAccountKey = stringPreferencesKey(SelectedAccountKey)
    val selectedAccount: Flow<String?> = dataStore.data.map { it[selectedAccountKey] }
    suspend fun setSelectedAccount(account: String?): Unit = dataStore.setOrRemove(selectedAccountKey, account)

    private val folderIdKey = stringPreferencesKey(FolderIdKey)
    val folderId: Flow<String?> = dataStore.data.map { it[folderIdKey] }
    suspend fun setFolderId(id: String?): Unit = dataStore.setOrRemove(folderIdKey, id)

    private val folderUrlKey = stringPreferencesKey(FolderUrlKey)
    val folderUrl: Flow<String?> = dataStore.data.map { it[folderUrlKey] }
    suspend fun setFolderUrl(url: String?): Unit = dataStore.setOrRemove(folderUrlKey, url)

    companion object {
        private const val SelectedAccountKey: String = "c76834a4-44ed-4985-ab5e-262cd1993a88"
        private const val FolderIdKey: String = "fe7c959a-7f84-409e-a92e-d7d2406698d7"
        private const val FolderUrlKey: String = "a793df5d-56fc-438c-bf00-275499b30bf9"
    }
}
