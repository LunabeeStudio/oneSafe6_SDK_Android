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
 * Created by Lunabee Studio / Date - 4/2/2024 - for the oneSafe6 SDK.
 * Last modified 4/2/24, 12:45 PM
 */

package studio.lunabee.onesafe.test

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import studio.lunabee.onesafe.domain.model.crypto.DatabaseKey
import studio.lunabee.onesafe.domain.repository.DatabaseKeyRepository

class DummyDatabaseCryptoRepository(private val databaseKey: DatabaseKey) : DatabaseKeyRepository {
    var throwInKeyFlow: Exception? = null

    val key: MutableStateFlow<DatabaseKey?> = MutableStateFlow(null)
    val backupKey: MutableStateFlow<DatabaseKey?> = MutableStateFlow(null)
    override fun generateKey(): DatabaseKey {
        return databaseKey
    }

    override suspend fun removeKey() {
        key.value = null
    }

    override fun getKeyFlow(): Flow<DatabaseKey?> = flow {
        throwInKeyFlow?.let {
            throw it
        }
        emit(key.value)
    }

    override suspend fun setKey(key: DatabaseKey, override: Boolean) {
        this.key.value = key
    }

    override fun getBackupKeyFlow(): Flow<DatabaseKey?> = backupKey

    override suspend fun removeBackupKey() {
        backupKey.value = null
    }

    override suspend fun copyKeyToBackupKey() {
        backupKey.value = key.value
    }
}
