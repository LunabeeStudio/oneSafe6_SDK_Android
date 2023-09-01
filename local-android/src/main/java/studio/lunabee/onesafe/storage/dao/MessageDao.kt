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
import studio.lunabee.onesafe.messaging.domain.model.MessageOrder
import studio.lunabee.onesafe.storage.model.RoomMessage
import java.util.UUID

@Dao
interface MessageDao {
    @Insert
    suspend fun insert(message: RoomMessage)

    @Query("SELECT * FROM Message WHERE contact_id IS :contactId ORDER BY `order` DESC")
    suspend fun getAllByContact(contactId: UUID): List<RoomMessage>

    @Query("SELECT enc_sent_at as encSentAt, `order` FROM Message WHERE contact_id IS :contactId ORDER BY `order` DESC LIMIT 1")
    suspend fun getLastMessageOrderByContact(contactId: UUID): MessageOrder?

    @Query("SELECT enc_sent_at as encSentAt, `order` FROM Message WHERE contact_id IS :contactId ORDER BY `order` ASC LIMIT 1")
    suspend fun getFirstMessageOrderByContact(contactId: UUID): MessageOrder?

    @Query("SELECT COUNT(*) FROM Message WHERE contact_id IS :contactId")
    suspend fun countByContact(contactId: UUID): Int

    @Query(
        """
        SELECT enc_sent_at as encSentAt, `order` 
        FROM Message
        WHERE contact_id IS :contactId 
        ORDER BY `order` DESC
        LIMIT 1 OFFSET :position
        """,
    )
    suspend fun getMessageOrderAtByContact(position: Int, contactId: UUID): MessageOrder?

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
}
