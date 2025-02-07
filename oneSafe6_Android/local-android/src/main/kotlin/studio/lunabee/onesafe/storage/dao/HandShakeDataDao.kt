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
 * Created by Lunabee Studio / Date - 7/6/2023 - for the oneSafe6 SDK.
 * Last modified 06/07/2023 10:32
 */

package studio.lunabee.onesafe.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import studio.lunabee.onesafe.storage.model.RoomHandShakeData
import java.util.UUID

@Dao
interface HandShakeDataDao {

    @Insert
    suspend fun insert(roomHandShakeData: RoomHandShakeData)

    @Query("DELETE FROM HandShakeData WHERE conversation_local_id = :conversationId")
    suspend fun deleteById(conversationId: UUID)

    @Query("SELECT * FROM HandShakeData WHERE conversation_local_id = :conversationId")
    suspend fun getById(conversationId: UUID): RoomHandShakeData?
}
