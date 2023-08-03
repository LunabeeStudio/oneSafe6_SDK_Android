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
 * Created by Lunabee Studio / Date - 6/30/2023 - for the oneSafe6 SDK.
 * Last modified 30/06/2023 10:11
 */

package studio.lunabee.onesafe.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import studio.lunabee.onesafe.storage.model.RoomDoubleRatchetKey

@Dao
interface DoubleRatchetKeyDao {
    @Insert
    suspend fun insert(key: RoomDoubleRatchetKey)

    @Query("SELECT data FROM DoubleRatchetKey WHERE id = :id")
    suspend fun getById(id: String): ByteArray?

    @Query("DELETE FROM DoubleRatchetKey WHERE id = :id")
    suspend fun deleteById(id: String)
}
