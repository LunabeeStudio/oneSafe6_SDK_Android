/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 3/19/2024 - for the oneSafe6 SDK.
 * Last modified 3/19/24, 5:29 PM
 */

package studio.lunabee.onesafe.domain.repository

import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.crypto.DatabaseKey

interface DatabaseKeyRepository {
    /**
     * Create a key suitable for database encryption. Does not store it.
     */
    fun generateKey(): DatabaseKey

    suspend fun removeKey()

    fun getKeyFlow(): Flow<DatabaseKey?>

    suspend fun setKey(key: DatabaseKey, override: Boolean)

    fun getBackupKeyFlow(): Flow<DatabaseKey?>

    suspend fun removeBackupKey()

    suspend fun copyKeyToBackupKey()
}
