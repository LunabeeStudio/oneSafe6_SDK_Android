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

package studio.lunabee.onesafe.repository.repository

import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.repository.RecentSearchRepository
import studio.lunabee.onesafe.repository.datasource.RecentSearchLocalDatasource
import javax.inject.Inject

class RecentSearchRepositoryImpl @Inject constructor(
    private val recentSearchLocalDatasource: RecentSearchLocalDatasource,
) : RecentSearchRepository {
    override fun getRecentSearch(safeId: SafeId): Flow<List<ByteArray>> = recentSearchLocalDatasource
        .getRecentSearch(safeId)

    override suspend fun saveRecentSearch(
        safeId: SafeId,
        searchHash: ByteArray,
        recentSearch: ByteArray,
        timestamp: Long,
        limitRecentSearchSaved: Int,
    ): Unit = recentSearchLocalDatasource.saveRecentSearch(
        safeId = safeId,
        searchHash = searchHash,
        encSearch = recentSearch,
        timestamp = timestamp,
        limitRecentSearchSaved = limitRecentSearchSaved,
    )
}
