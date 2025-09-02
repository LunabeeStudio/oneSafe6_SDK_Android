/*
 * Copyright (c) 2024-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 9/4/2024 - for the oneSafe6 SDK.
 * Last modified 9/4/24, 10:01 AM
 */

package studio.lunabee.onesafe.cryptography.android

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import studio.lunabee.onesafe.cryptography.android.qualifier.DataStoreType
import studio.lunabee.onesafe.cryptography.android.qualifier.DatastoreEngineProvider
import studio.lunabee.onesafe.domain.model.crypto.DatabaseKey
import studio.lunabee.onesafe.domain.repository.DatabaseKeyRepository
import javax.inject.Inject

class DatabaseKeyRepositoryImpl @Inject constructor(
    @param:DatastoreEngineProvider(type = DataStoreType.Encrypted) private val dataStore: DatastoreEngine,
    private val keyProvider: RandomKeyProvider,
) : DatabaseKeyRepository {
    override fun generateKey(): DatabaseKey {
        val key = keyProvider()
        return DatabaseKey(key)
    }

    override suspend fun removeKey() {
        removeKey(DATABASE_KEY_ALIAS)
    }

    override fun getKeyFlow(): Flow<DatabaseKey?> {
        return getKeyFlow(DATABASE_KEY_ALIAS)
    }

    override suspend fun setKey(key: DatabaseKey, override: Boolean) {
        dataStore.insertValue(key = DATABASE_KEY_ALIAS, value = key.raw, override = override)
    }

    override fun getBackupKeyFlow(): Flow<DatabaseKey?> {
        return getKeyFlow(DATABASE_BACKUP_KEY_ALIAS)
    }

    override suspend fun removeBackupKey() {
        removeKey(DATABASE_BACKUP_KEY_ALIAS)
    }

    override suspend fun copyKeyToBackupKey() {
        getKeyFlow().firstOrNull()?.let { key ->
            dataStore.insertValue(key = DATABASE_BACKUP_KEY_ALIAS, value = key.raw)
        }
    }

    private fun getKeyFlow(alias: String) = dataStore.retrieveValue(alias).map { it?.let { DatabaseKey(it) } }

    private suspend fun removeKey(alias: String) = dataStore.removeValue(alias)

    companion object {
        private const val DATABASE_KEY_ALIAS = "fa8ea4fd-bea0-4705-be5c-78b5d9ec0d35"
        private const val DATABASE_BACKUP_KEY_ALIAS = "788a5a8f-a412-468b-9b80-3aff37f214b7"
    }
}
