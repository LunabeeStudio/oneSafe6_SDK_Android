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
 * Created by Lunabee Studio / Date - 4/12/2023 - for the oneSafe6 SDK.
 * Last modified 4/12/23, 2:57 PM
 */

package studio.lunabee.onesafe.visits

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.Flow
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.Preferences
import javax.inject.Inject

class OsAppVisit @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    private val hasVisitedLoginKey = booleanPreferencesKey(AppVisitConstants.hasVisitedLoginKey)
    val hasVisitedLogin: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[hasVisitedLoginKey] ?: AppVisitConstants.hasVisitedLoginDefault
    }

    suspend fun storeHasVisitedLogin() {
        dataStore.edit { preferences ->
            preferences[hasVisitedLoginKey] = true
        }
    }

    private val hasFinishOneSafeKOnBoardingKey = booleanPreferencesKey(AppVisitConstants.hasFinishOneSafeKOnBoarding)

    val hasFinishOneSafeKOnBoarding: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[hasFinishOneSafeKOnBoardingKey] ?: AppVisitConstants.hasFinishOneSafeKOnBoardingDefault
    }

    suspend fun storeHasFinishOneSafeKOnBoarding() {
        dataStore.edit { preferences ->
            preferences[hasFinishOneSafeKOnBoardingKey] = true
        }
    }
}
