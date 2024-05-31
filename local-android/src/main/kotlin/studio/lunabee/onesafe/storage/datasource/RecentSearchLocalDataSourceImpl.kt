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

package studio.lunabee.onesafe.storage.datasource

import androidx.datastore.core.DataStore
import com.google.protobuf.ByteString
import com.google.protobuf.kotlin.toByteString
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.repository.datasource.RecentSearchLocalDatasource
import studio.lunabee.onesafe.storage.OSRecentSearchProto.RecentSearchProto
import studio.lunabee.onesafe.storage.copy
import javax.inject.Inject

class RecentSearchLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<RecentSearchProto>,
    @FileDispatcher private val fileDispatcher: CoroutineDispatcher,
) : RecentSearchLocalDatasource {
    private val limitRecentSearchSaved: Int = 12

    override fun getRecentSearch(): Flow<LinkedHashSet<ByteArray>> = dataStore.data.map { data ->
        LinkedHashSet<ByteArray>().also {
            it.addAll(data.recentSearchList.map(ByteString::toByteArray))
        }
    }.flowOn(fileDispatcher)

    override suspend fun saveRecentSearch(recentSearch: List<ByteArray>) {
        withContext(fileDispatcher) {
            dataStore.updateData {
                it.copy {
                    val list = recentSearch.map(ByteArray::toByteString)
                    this@copy.recentSearch.clear()
                    this@copy.recentSearch.addAll(list.take(limitRecentSearchSaved))
                }
            }
        }
    }
}
