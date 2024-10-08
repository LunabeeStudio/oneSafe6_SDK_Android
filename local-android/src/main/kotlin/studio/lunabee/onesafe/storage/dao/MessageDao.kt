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
 * Created by Lunabee Studio / Date - 6/20/2023 - for the oneSafe6 SDK.
 * Last modified 6/20/23, 1:28 PM
 */

package studio.lunabee.onesafe.storage.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.storage.model.RoomMessage
import studio.lunabee.onesafe.storage.model.RoomMessageOrder
import java.util.UUID

@Dao
interface MessageDao {
    @Insert
    suspend fun insert(message: RoomMessage)

    @Insert
    suspend fun insertAll(messages: List<RoomMessage>)

    @Query("SELECT * FROM Message WHERE contact_id IS :contactId ORDER BY `order` DESC")
    suspend fun getAllByContact(contactId: UUID): List<RoomMessage>

    @Query(
        """
        SELECT id, enc_sent_at as encSentAt, `order` 
        FROM Message 
        WHERE contact_id IS :contactId AND id NOT IN (:exceptIds)
        ORDER BY `order` DESC
        LIMIT 1""",
    )
    suspend fun getLastMessageOrderByContact(contactId: UUID, exceptIds: List<UUID>): RoomMessageOrder?

    @Query(
        """
        SELECT id, enc_sent_at as encSentAt, `order` 
        FROM Message
        WHERE contact_id IS :contactId AND id NOT IN (:exceptIds)
        ORDER BY `order` ASC
        LIMIT 1""",
    )
    suspend fun getFirstMessageOrderByContact(contactId: UUID, exceptIds: List<UUID>): RoomMessageOrder?

    @Query("SELECT COUNT(*) FROM Message WHERE contact_id IS :contactId AND id NOT IN (:exceptIds)")
    suspend fun countByContact(contactId: UUID, exceptIds: List<UUID>): Int

    @Query(
        """
        SELECT id, enc_sent_at as encSentAt, `order` 
        FROM Message
        WHERE contact_id IS :contactId AND id NOT IN (:exceptIds)
        ORDER BY `order` DESC
        LIMIT 1
        OFFSET :position
        """,
    )
    suspend fun getMessageOrderAtByContact(position: Int, contactId: UUID, exceptIds: List<UUID>): RoomMessageOrder?

    @Query("SELECT * FROM Message WHERE contact_id IS :contactId AND `order` = :order")
    suspend fun getByContactByOrder(contactId: UUID, order: Float): RoomMessage

    @Query("SELECT * FROM Message WHERE contact_id IS :contactId ORDER BY `order` DESC")
    fun getAllAsPagingSource(contactId: UUID): PagingSource<Int, RoomMessage>

    @Query("DELETE FROM Message WHERE contact_id IS :contactId ")
    suspend fun deleteAllMessages(contactId: UUID)

    @Query("SELECT * FROM Message WHERE contact_id IS :contactId ORDER BY `order` DESC LIMIT 1")
    fun getLastMessage(contactId: UUID): Flow<RoomMessage?>

    @Query("DELETE FROM Message WHERE id IS :messageId")
    suspend fun deleteMessage(messageId: UUID)

    @Query("UPDATE Message SET is_read = 1 WHERE contact_id IS :contactId")
    suspend fun markMessagesAsRead(contactId: UUID)

    @Query("SELECT * FROM Message WHERE contact_id IN (:contactIds) ORDER BY `order` ASC")
    suspend fun getAllByContactList(contactIds: List<UUID>): List<RoomMessage>

    @Query("SELECT id FROM Message")
    suspend fun getAllIds(): List<UUID>
}
