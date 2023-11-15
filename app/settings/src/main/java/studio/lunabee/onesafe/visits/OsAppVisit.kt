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
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.lunabee.onesafe.blockingReadDatastore
import javax.inject.Inject

class OsAppVisit @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    private val hasVisitedLoginKey = booleanPreferencesKey(AppVisitConstants.hasVisitedLoginKey)
    val hasVisitedLogin: Boolean by blockingReadDatastore(
        dataStore,
        hasVisitedLoginKey,
        AppVisitConstants.hasVisitedLoginDefault,
    )

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

    private val hasDoneOnBoardingBubblesKey = booleanPreferencesKey(AppVisitConstants.hasDoneOnBoardingBubbles)

    val hasDoneOnBoardingBubblesFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[hasDoneOnBoardingBubblesKey] ?: AppVisitConstants.hasDoneOnBoardingBubblesDefault
    }

    val hasDoneOnBoardingBubbles: Boolean by blockingReadDatastore(
        dataStore = dataStore,
        key = hasDoneOnBoardingBubblesKey,
        defaultValue = AppVisitConstants.hasDoneOnBoardingBubblesDefault,
    )

    suspend fun storeHasDoneOnBoardingBubbles() {
        dataStore.edit { preferences ->
            preferences[hasDoneOnBoardingBubblesKey] = true
        }
    }

    private val hasDoneTutorialOpenOskKey = booleanPreferencesKey(AppVisitConstants.hasDoneTutorialOpenOsk)
    val hasDoneTutorialOpenOskFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[hasDoneTutorialOpenOskKey] ?: AppVisitConstants.hasDoneTutorialOpenOskDefault
    }

    suspend fun storeHasDoneTutorialOpenOsk(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[hasDoneTutorialOpenOskKey] = value
        }
    }

    private val hasDoneTutorialLockOskKey = booleanPreferencesKey(AppVisitConstants.hasDoneTutorialLockOsk)
    val hasDoneTutorialLockOskFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[hasDoneTutorialLockOskKey] ?: AppVisitConstants.hasDoneTutorialLockOskDefault
    }

    suspend fun storeHasDoneTutorialLockOsk(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[hasDoneTutorialLockOskKey] = value
        }
    }
}
