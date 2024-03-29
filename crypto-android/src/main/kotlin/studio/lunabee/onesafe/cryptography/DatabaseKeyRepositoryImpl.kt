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
 * Last modified 3/19/24, 5:37 PM
 */

package studio.lunabee.onesafe.cryptography

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.lunabee.onesafe.cryptography.qualifier.DataStoreType
import studio.lunabee.onesafe.cryptography.qualifier.DatastoreEngineProvider
import studio.lunabee.onesafe.domain.model.crypto.DatabaseKey
import studio.lunabee.onesafe.domain.repository.DatabaseKeyRepository
import javax.inject.Inject

class DatabaseKeyRepositoryImpl @Inject constructor(
    @DatastoreEngineProvider(type = DataStoreType.Encrypted) private val dataStore: DatastoreEngine,
    private val keyProvider: RandomKeyProvider,
) : DatabaseKeyRepository {
    override suspend fun createKey(): DatabaseKey {
        val key = keyProvider()
        dataStore.insertValue(value = key, key = DATABASE_KEY_ALIAS, override = false)
        return DatabaseKey(key)
    }

    override suspend fun removeKey() {
        dataStore.removeValue(DATABASE_KEY_ALIAS)
    }

    override fun getKeyFlow(): Flow<DatabaseKey?> {
        return dataStore.retrieveValue(DATABASE_KEY_ALIAS).map { it?.let { DatabaseKey(it) } }
    }

    companion object {
        private const val DATABASE_KEY_ALIAS = "fa8ea4fd-bea0-4705-be5c-78b5d9ec0d35"
    }
}
