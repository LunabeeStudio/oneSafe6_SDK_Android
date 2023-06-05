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
 * Created by Lunabee Studio / Date - 5/22/2023 - for the oneSafe6 SDK.
 * Last modified 5/22/23, 11:04 AM
 */

package studio.lunabee.onesafe.bubbles.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.bubbles.local.model.RoomBubblesContact

@Dao
interface BubblesContactDao {
    @Insert
    suspend fun insert(contacts: List<RoomBubblesContact>)

    @Query("DELETE FROM BubblesContact")
    suspend fun clearTable()

    @Query("SELECT * FROM BubblesContact")
    fun getAll(): Flow<List<RoomBubblesContact>>
}
