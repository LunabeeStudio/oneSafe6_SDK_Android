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

package studio.lunabee.onesafe.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import studio.lunabee.onesafe.storage.model.RoomSafeItemKey
import java.util.UUID

@Dao
interface SafeItemKeyDao {

    @Query("SELECT * FROM SafeItemKey WHERE id = :id")
    suspend fun findById(id: UUID): RoomSafeItemKey?

    @Query("SELECT * FROM SafeItemKey WHERE id IN (:ids)")
    suspend fun findByIds(ids: List<UUID>): List<RoomSafeItemKey>

    @Query("SELECT * FROM SafeItemKey")
    suspend fun getAllSafeItemKeys(): List<RoomSafeItemKey>

    @Upsert
    suspend fun insert(safeItemKey: RoomSafeItemKey)

    @Insert
    suspend fun insert(safeItemKey: List<RoomSafeItemKey>)

    @Update
    suspend fun update(safeItemKey: List<RoomSafeItemKey>)

    @Query("DELETE FROM SafeItemKey")
    suspend fun clearTable()
}
