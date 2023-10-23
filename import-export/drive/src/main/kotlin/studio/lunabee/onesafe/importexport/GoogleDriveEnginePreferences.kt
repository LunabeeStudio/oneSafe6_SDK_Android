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
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class GoogleDriveEnginePreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val selectedAccountKey = stringPreferencesKey(ImportExportDriveConstant.SelectedAccountKey)

    var selectedAccount: String?
        get() = runBlocking {
            dataStore.data.map { preferences -> preferences[selectedAccountKey] }.firstOrNull()
        }
        set(value) {
            runBlocking {
                dataStore.edit { settings ->
                    if (value != null) {
                        settings[selectedAccountKey] = value
                    } else {
                        settings.remove(selectedAccountKey)
                    }
                }
            }
        }

    private val isDriveApiAuthorizedKey = booleanPreferencesKey(ImportExportDriveConstant.IsDriveApiAuthorizedKey)

    var isDriveApiAuthorized: Boolean
        get() = runBlocking {
            dataStore.data.map { preferences -> preferences[isDriveApiAuthorizedKey] }.firstOrNull() ?: false
        }
        set(value) {
            runBlocking {
                dataStore.edit { settings ->
                    settings[isDriveApiAuthorizedKey] = value
                }
            }
        }
}
