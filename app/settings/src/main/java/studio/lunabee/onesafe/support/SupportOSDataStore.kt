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
 * Created by Lunabee Studio / Date - 6/6/2023 - for the oneSafe6 SDK.
 * Last modified 6/6/23, 2:41 PM
 */

package studio.lunabee.onesafe.support

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.first
import studio.lunabee.onesafe.repository.datasource.SupportOSDataSource
import java.time.Instant
import javax.inject.Inject

class SupportOSDataStore @Inject constructor(
    private val datastore: DataStore<Preferences>,
) : SupportOSDataSource {

    private val appVisitsCountKey: Preferences.Key<Int> = intPreferencesKey(SupportOSConstants.AppVisitKey)
    private val ratingTimeStampKey: Preferences.Key<Long> = longPreferencesKey(SupportOSConstants.RatingTimeStampKey)
    private val dismissTimeStampKey: Preferences.Key<Long> = longPreferencesKey(SupportOSConstants.DismissTimeStampKey)

    override val appVisitsCount: Flow<Int> =
        datastore.data.map { preferences -> preferences[appVisitsCountKey] ?: SupportOSConstants.AppVisitDefault }

    override val dismissInstant: Flow<Instant?> = datastore.data.map { preferences ->
        val timeStamp = preferences[dismissTimeStampKey]
        if (timeStamp != null) {
            Instant.ofEpochMilli(timeStamp)
        } else {
            null
        }
    }

    override val ratingInstant: Flow<Instant?> = datastore.data.map { preferences ->
        val timeStamp = preferences[ratingTimeStampKey]
        if (timeStamp != null) {
            Instant.ofEpochMilli(timeStamp)
        } else {
            null
        }
    }

    override suspend fun incrementAppVisit() {
        val actualCount = appVisitsCount.first()
        datastore.edit { preferences ->
            preferences[appVisitsCountKey] = actualCount + 1
        }
    }

    override suspend fun resetAppVisit() {
        datastore.edit { preferences ->
            preferences[appVisitsCountKey] = 0
        }
    }

    override suspend fun setAppVisit(count: Int) {
        datastore.edit { preferences ->
            preferences[appVisitsCountKey] = count
        }
    }

    override suspend fun setRattingInstant(instant: Instant?) {
        datastore.edit { preferences ->
            if (instant != null) {
                preferences[dismissTimeStampKey] = instant.toEpochMilli()
            } else {
                preferences.remove(dismissTimeStampKey)
            }
        }
    }

    override suspend fun setRatingInstant(instant: Instant?) {
        datastore.edit { preferences ->
            if (instant != null) {
                preferences[ratingTimeStampKey] = instant.toEpochMilli()
            } else {
                preferences.remove(ratingTimeStampKey)
            }
        }
    }
}
