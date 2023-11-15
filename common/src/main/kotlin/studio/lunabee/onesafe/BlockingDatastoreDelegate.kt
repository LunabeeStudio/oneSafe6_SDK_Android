/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Last modified 11/9/23, 9:16 AM
 */

package studio.lunabee.onesafe

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Property delegates that read & write datastore preferences synchronously using [runBlocking]
 *
 * @param dataStore The datastore to read/write data from
 * @param key The preference key to read/write
 * @param readMapper Block to map the [Output] type to internal storage type [StoreType]
 * @param writeMapper Block to map the internal storage type [StoreType] to the [Output] type
 */
fun <T, Output, StoreType> blockingDatastore(
    dataStore: DataStore<Preferences>,
    key: Preferences.Key<StoreType>,
    readMapper: (StoreType?) -> Output,
    writeMapper: (Output) -> StoreType,
): ReadWriteProperty<T, Output> {
    return ReadWriteBlockingDatastoreDelegate(dataStore, key, readMapper, writeMapper)
}

/**
 * Specific implementation of [blockingDatastore] for primitive (non-null) types
 *
 * @see [blockingDatastore]
 */
fun <T, Output> blockingDatastore(
    dataStore: DataStore<Preferences>,
    key: Preferences.Key<Output>,
    defaultValue: Output,
): ReadWriteProperty<T, Output> {
    return ReadWriteBlockingDatastoreDelegate(dataStore, key, { it ?: defaultValue }, { it })
}

/**
 * Property delegates that read datastore preferences synchronously using [runBlocking]
 *
 * @param dataStore The datastore to read data from
 * @param key The preference key to read
 * @param readMapper Block to map the [Output] type to internal storage type [StoreType]
 */
fun <T, Output, StoreType> blockingReadDatastore(
    dataStore: DataStore<Preferences>,
    key: Preferences.Key<StoreType>,
    readMapper: (StoreType?) -> Output,
): ReadOnlyProperty<T, Output> {
    return ReadOnlyBlockingDatastoreDelegate(dataStore, key, readMapper)
}

/**
 * Specific implementation of [blockingReadDatastore] for primitive (non-null) types
 *
 * @see [blockingReadDatastore]
 */
fun <T, Output> blockingReadDatastore(
    dataStore: DataStore<Preferences>,
    key: Preferences.Key<Output>,
    defaultValue: Output,
): ReadOnlyProperty<T, Output> {
    return ReadOnlyBlockingDatastoreDelegate(dataStore, key) { it ?: defaultValue }
}

/**
 * Specific implementation of [blockingEnumDatastore] for enum types
 *
 * @see [blockingEnumDatastore]
 */
inline fun <T, reified Output : Enum<Output>> blockingEnumDatastore(
    dataStore: DataStore<Preferences>,
    key: Preferences.Key<String>,
    defaultValue: Output,
): ReadWriteProperty<T, Output> {
    return ReadWriteBlockingDatastoreDelegate(
        dataStore = dataStore,
        key = key,
        readMapper = { it?.let { enumValueOf<Output>(it) } ?: defaultValue },
        writeMapper = { it.toString() },
    )
}

/**
 * Specific implementation of [blockingEnumDatastore] for [Duration] type
 *
 * @see [blockingEnumDatastore]
 */
fun <T> blockingDurationDatastore(
    dataStore: DataStore<Preferences>,
    key: Preferences.Key<Long>,
    defaultValue: Duration,
): ReadWriteProperty<T, Duration> {
    return ReadWriteBlockingDatastoreDelegate(dataStore, key, { it?.milliseconds ?: defaultValue }, { it.inWholeMilliseconds })
}

class ReadWriteBlockingDatastoreDelegate<T, Output, StoreType>(
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<StoreType>,
    private val readMapper: (StoreType?) -> Output,
    private val writeMapper: (Output) -> StoreType,
) : ReadWriteProperty<T, Output>, ReadOnlyProperty<T, Output> by ReadOnlyBlockingDatastoreDelegate(dataStore, key, readMapper) {
    override fun setValue(thisRef: T, property: KProperty<*>, value: Output): Unit = runBlocking {
        dataStore.edit { settings ->
            if (value != null) {
                settings[key] = writeMapper(value)
            } else {
                settings.remove(key)
            }
        }
    }
}

class ReadOnlyBlockingDatastoreDelegate<T, Output, StoreType>(
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<StoreType>,
    private val readMapper: (StoreType?) -> Output,
) : ReadOnlyProperty<T, Output> {
    override fun getValue(thisRef: T, property: KProperty<*>): Output = runBlocking {
        readMapper(dataStore.data.map { preferences -> preferences[key] }.firstOrNull())
    }
}
