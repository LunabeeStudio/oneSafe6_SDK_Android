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
 * Created by Lunabee Studio / Date - 6/24/2024 - for the oneSafe6 SDK.
 * Last modified 6/24/24, 9:59 AM
 */

package studio.lunabee.onesafe.storage.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.storage.model.RoomRecentSearch

@Dao
abstract class RecentSearchDao {
    @Transaction
    open suspend fun upsertRecentSearchWithLimit(recentSearch: RoomRecentSearch, keepCount: Int) {
        upsert(recentSearch)
        deleteOldest(recentSearch.safeId, keepCount)
    }

    @Upsert
    protected abstract suspend fun upsert(recentSearch: RoomRecentSearch)

    @Query(
        """
            DELETE FROM RecentSearch
            WHERE enc_search IN (
                SELECT enc_search FROM RecentSearch
                WHERE safe_id = :safeId
                ORDER BY timestamp_ms DESC
                LIMIT -1 OFFSET :keepCount
            );
        """,
    )
    protected abstract suspend fun deleteOldest(safeId: SafeId, keepCount: Int)

    @Query("SELECT enc_search FROM RecentSearch WHERE safe_id = :safeId ORDER BY timestamp_ms DESC")
    abstract fun getAllOrderByDateDesc(safeId: SafeId): Flow<List<ByteArray>>
}
