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
 * Created by Lunabee Studio / Date - 6/14/2023 - for the oneSafe6 SDK.
 * Last modified 6/14/23, 9:00 AM
 */

package studio.lunabee.onesafe.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.storage.model.RoomContact
import java.time.Instant
import java.util.UUID

@Dao
interface ContactDao {
    @Insert
    suspend fun insert(contact: RoomContact)

    @Insert
    suspend fun insertAll(contacts: List<RoomContact>)

    @Query("DELETE FROM Contact WHERE safe_id is :safeId")
    suspend fun deleteAll(safeId: UUID)

    @Query("SELECT * FROM Contact WHERE safe_id is :safeId")
    fun getAllInFlow(safeId: UUID): Flow<List<RoomContact>>

    @Query("SELECT * FROM Contact WHERE safe_id is :safeId ORDER BY updated_at DESC LIMIT :maxNumber")
    fun getRecentContactsFlow(maxNumber: Int, safeId: UUID): Flow<List<RoomContact>>

    @Query("SELECT * FROM Contact WHERE id = :id")
    fun getByIdFlow(id: UUID): Flow<RoomContact?>

    @Query("SELECT id FROM Contact")
    suspend fun getAllIds(): List<UUID>

    @Query("SELECT * FROM Contact WHERE id = :id")
    suspend fun getById(id: UUID): RoomContact?

    @Query("SELECT * FROM Contact WHERE id = :id AND safe_id = :safeId")
    suspend fun getByIdInSafe(id: UUID, safeId: UUID): RoomContact?

    @Query("SELECT enc_shared_key FROM Contact WHERE id = :id")
    suspend fun getContactSharedKey(id: UUID): ByteArray?

    @Query("UPDATE Contact SET enc_shared_key = :sharedKey WHERE id = :id")
    suspend fun addContactSharedKey(id: UUID, sharedKey: ByteArray)

    @Query("DELETE FROM Contact WHERE id = :id")
    suspend fun remote(id: UUID)

    @Query("UPDATE Contact SET enc_sharing_mode = :encSharingMode, updated_at =:updateAt WHERE id = :id")
    suspend fun updateMessageSharingMode(id: UUID, encSharingMode: ByteArray, updateAt: Instant)

    @Query("UPDATE Contact SET enc_sharing_mode = :encSharingMode, updated_at =:updateAt, enc_name = :encName WHERE id = :id")
    suspend fun updateContact(id: UUID, encSharingMode: ByteArray, encName: ByteArray, updateAt: Instant)

    @Query("UPDATE Contact SET updated_at =:updateAt WHERE id = :id")
    suspend fun updateUpdatedAt(id: UUID, updateAt: Instant)

    @Query("UPDATE Contact SET consulted_at =:consultedAt WHERE id = :id")
    suspend fun updateContactConsultedAt(id: UUID, consultedAt: Instant)

    @Query("UPDATE Contact SET enc_reset_conversation_date = :encResetConversationDate WHERE id = :id")
    suspend fun updateContactResetConversationDate(id: UUID, encResetConversationDate: ByteArray)

    @Query("SELECT COUNT(*) FROM Contact WHERE safe_id = :safeId")
    suspend fun getContactCount(safeId: UUID): Int
}
