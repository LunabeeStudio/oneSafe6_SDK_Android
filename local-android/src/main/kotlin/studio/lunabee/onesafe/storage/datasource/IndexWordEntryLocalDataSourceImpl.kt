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

import com.lunabee.lbextensions.mapValues
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import studio.lunabee.onesafe.domain.model.search.IndexWordEntry
import studio.lunabee.onesafe.repository.datasource.IndexWordEntryLocalDataSource
import studio.lunabee.onesafe.storage.dao.IndexWordEntryDao
import studio.lunabee.onesafe.storage.model.RoomIndexWordEntry
import java.util.UUID
import javax.inject.Inject

class IndexWordEntryLocalDataSourceImpl @Inject constructor(
    private val searchIndexDao: IndexWordEntryDao,
) : IndexWordEntryLocalDataSource {
    override fun getAll(): Flow<List<IndexWordEntry>> = searchIndexDao.getAll()
        .distinctUntilChanged()
        .mapValues(RoomIndexWordEntry::toIndexWordEntry)

    override suspend fun insert(indexWordEntries: List<IndexWordEntry>) {
        searchIndexDao.insert(indexWordEntries.map(RoomIndexWordEntry::fromIndexWordEntry))
    }

    override suspend fun deleteNameIndexFromItemId(itemId: UUID) = searchIndexDao.deleteNameIndexFromItemId(itemId)
}
