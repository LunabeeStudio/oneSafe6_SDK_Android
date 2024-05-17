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
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:24 AM
 */

package studio.lunabee.onesafe.cryptography

import androidx.annotation.CallSuper
import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSError.Companion.get

abstract class DatastoreEngine(
    protected val dataStore: DataStore<ProtoData>,
) {

    /**
     * Insert or update a data in the [dataStore]
     *
     * @param value The data to insert or update.
     * @param key The key of the datastore entry
     * @param override Allow overriding the entry with new value. Default true.
     */
    @CallSuper
    @Throws(OSCryptoError::class)
    open suspend fun insertValue(key: String, value: ByteArray, override: Boolean = true) {
        if (!override && dataStore.data.map { it.dataMap[key] }.firstOrNull() != null) {
            throw OSCryptoError.Code.DATASTORE_ENTRY_KEY_ALREADY_EXIST.get()
        }
    }

    suspend fun removeValue(key: String) {
        dataStore.updateData { it.toBuilder().removeData(key).build() }
    }

    /**
     * Retrieve a value from the [dataStore]
     *
     * @param key The key of the datastore entry
     * @return A flow of the stored data
     */
    abstract fun retrieveValue(key: String): Flow<ByteArray?>

    /**
     * Clear the whole datastore
     */
    open suspend fun clearDataStore() {
        dataStore.updateData { it.toBuilder().clearData().build() }
    }
}
