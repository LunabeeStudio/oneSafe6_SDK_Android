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
 * Created by Lunabee Studio / Date - 7/9/2024 - for the oneSafe6 SDK.
 * Last modified 7/9/24, 9:41 AM
 */

package studio.lunabee.onesafe.migration.migration

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.error.OSError
import javax.inject.Inject

/**
 * Remove BubblesActivation preference key (force true)
 */
class MigrationFromV2ToV3 @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    suspend operator fun invoke(): LBResult<Unit> = OSError.runCatching {
        dataStore.edit {
            it.remove(booleanPreferencesKey("ebb8bf03-8323-4190-8256-2023b11aab77"))
        }
    }
}
