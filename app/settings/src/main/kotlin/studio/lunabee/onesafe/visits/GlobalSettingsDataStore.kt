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
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.jvm.getAsFlow
import studio.lunabee.onesafe.jvm.store
import studio.lunabee.onesafe.repository.datasource.GlobalSettingsLocalDataSource
import javax.inject.Inject
import kotlin.properties.ReadOnlyProperty

class GlobalSettingsDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : GlobalSettingsLocalDataSource {
    private suspend fun <T> store(value: T, preferencesTips: OSPreferenceTips<T>) {
        dataStore.store(value = value, preferencesKey = preferencesTips.preferencesKey)
    }

    private fun <T> getAsFlow(preferencesTips: OSPreferenceTips<T>): Flow<T> {
        return dataStore.getAsFlow(preferencesKey = preferencesTips.preferencesKey, defaultValue = preferencesTips.defaultValue)
    }

    private fun <T, Output> get(preferencesTips: OSPreferenceTips<Output>): ReadOnlyProperty<T, Output> {
        return dataStore.get(preferencesKey = preferencesTips.preferencesKey, defaultValue = preferencesTips.defaultValue)
    }

    override fun hasVisitedLogin(): Flow<Boolean> {
        return getAsFlow(OSPreferenceTips.HasVisitedLogin)
    }

    override fun hasDoneTutorialOpenOsk(): Flow<Boolean> {
        return getAsFlow(OSPreferenceTips.HasDoneTutorialOpenOsk)
    }

    override fun hasDoneTutorialLockOsk(): Flow<Boolean> {
        return getAsFlow(OSPreferenceTips.HasDoneTutorialLockOsk)
    }

    override suspend fun setHasVisitedLogin(value: Boolean) {
        store(true, OSPreferenceTips.HasVisitedLogin)
    }

    override suspend fun setHasDoneTutorialOpenOsk(value: Boolean) {
        store(true, OSPreferenceTips.HasDoneTutorialOpenOsk)
    }

    override suspend fun setHasDoneTutorialLockOsk(value: Boolean) {
        store(true, OSPreferenceTips.HasDoneTutorialLockOsk)
    }
}
