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

import androidx.datastore.core.DataStore
import com.google.protobuf.kotlin.toByteString
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import javax.inject.Inject

class ClearDatastoreEngine @Inject constructor(
    dataStore: DataStore<ProtoData>,
    @FileDispatcher private val fileDispatcher: CoroutineDispatcher,
) : DatastoreEngine(dataStore) {
    override suspend fun editValue(value: ByteArray?, key: String) {
        withContext(fileDispatcher) {
            dataStore.updateData { data ->
                if (value == null) {
                    data.toBuilder().removeData(key).build()
                } else {
                    data.toBuilder().putData(key, value.toByteString()).build()
                }
            }
        }
    }

    override fun retrieveValue(key: String): Flow<ByteArray?> = dataStore.data.map { securedData ->
        val encValue = try {
            securedData.dataMap[key]
        } catch (e: Exception) {
            null
        }
        encValue?.toByteArray()
    }.flowOn(fileDispatcher)
}
