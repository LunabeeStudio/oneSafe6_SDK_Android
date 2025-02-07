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
 * Created by Lunabee Studio / Date - 6/26/2023 - for the oneSafe6 SDK.
 * Last modified 6/26/23, 7:56 AM
 */

package studio.lunabee.onesafe.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.storage.model.RoomEnqueuedMessage

@Dao
interface EnqueuedMessageDao {
    @Query("SELECT * FROM EnqueuedMessage ORDER BY id ASC LIMIT 1")
    fun getOldestAsFlow(): Flow<RoomEnqueuedMessage?>

    @Query("SELECT * FROM EnqueuedMessage ORDER BY id ASC")
    suspend fun getAll(): List<RoomEnqueuedMessage>

    @Query("DELETE FROM EnqueuedMessage WHERE id = :id")
    suspend fun delete(id: Int): Int

    @Insert
    suspend fun insert(message: RoomEnqueuedMessage)

    @Query("DELETE FROM EnqueuedMessage")
    suspend fun deleteAll()
}
