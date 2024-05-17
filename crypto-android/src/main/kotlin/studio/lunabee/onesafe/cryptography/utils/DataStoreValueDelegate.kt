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

package studio.lunabee.onesafe.cryptography.utils

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import studio.lunabee.onesafe.cryptography.DatastoreEngine
import studio.lunabee.onesafe.error.OSCryptoError
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal fun <T> dataStoreValueDelegate(
    key: String,
    datastoreEngine: DatastoreEngine,
    errorCodeIfOverrideExistingValue: OSCryptoError.Code,
): ReadWriteProperty<T, ByteArray?> {
    return DataStoreValueDelegate(key, datastoreEngine, errorCodeIfOverrideExistingValue)
}

/**
 * Property delegate of datastore which:
 * force the value to be reset before setting a new value (to avoid unexpected override of the value)
 */
private class DataStoreValueDelegate<T>(
    private val key: String,
    private val datastoreEngine: DatastoreEngine,
    private val errorCodeIfOverrideExistingValue: OSCryptoError.Code,
) : ReadWriteProperty<T, ByteArray?> {

    override fun getValue(thisRef: T, property: KProperty<*>): ByteArray? {
        return getValueInDataStore()
    }

    @Throws(OSCryptoError::class)
    override fun setValue(thisRef: T, property: KProperty<*>, value: ByteArray?) {
        val currentValue = getValueInDataStore()
        if (currentValue == null || value == null) {
            if (value == null) {
                runBlocking { datastoreEngine.removeValue(key) }
            } else {
                runBlocking { datastoreEngine.insertValue(key, value) }
            }
        } else {
            throw OSCryptoError(errorCodeIfOverrideExistingValue)
        }
    }

    fun getValueInDataStore() = runBlocking { datastoreEngine.retrieveValue(key).first() }
}
