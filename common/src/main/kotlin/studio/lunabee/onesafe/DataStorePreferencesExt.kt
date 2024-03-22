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
 * Created by Lunabee Studio / Date - 11/9/2023 - for the oneSafe6 SDK.
 * Last modified 11/9/23, 12:14 PM
 */

package studio.lunabee.onesafe

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.properties.ReadOnlyProperty

suspend fun <T> DataStore<Preferences>.setOrRemove(prefKey: Preferences.Key<T>, value: T?) {
    edit {
        if (value == null) {
            it.remove(prefKey)
        } else {
            it[prefKey] = value
        }
    }
}

suspend fun <T> DataStore<Preferences>.store(value: T, preferencesKey: Preferences.Key<T>) {
    edit { preferences -> preferences[preferencesKey] = value }
}

fun <T> DataStore<Preferences>.getAsFlow(preferencesKey: Preferences.Key<T>, defaultValue: T): Flow<T> {
    return data.map { preferences -> preferences[preferencesKey] ?: defaultValue }
}

fun <T, Output> DataStore<Preferences>.get(preferencesKey: Preferences.Key<Output>, defaultValue: Output): ReadOnlyProperty<T, Output> {
    return blockingReadDatastore(dataStore = this, key = preferencesKey, defaultValue = defaultValue)
}
