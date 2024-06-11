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

package studio.lunabee.onesafe.storage.datasource

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.lunabee.messaging.repository.datasource.MessageLocalDataSource
import studio.lunabee.onesafe.mapPagingValues
import studio.lunabee.onesafe.messaging.domain.model.MessageOrder
import studio.lunabee.onesafe.messaging.domain.model.SafeMessage
import studio.lunabee.onesafe.storage.dao.MessageDao
import studio.lunabee.onesafe.storage.model.RoomMessage
import java.util.UUID
import javax.inject.Inject

class MessageLocalDataSourceImpl @Inject constructor(
    private val messageDao: MessageDao,
) : MessageLocalDataSource {
    override suspend fun save(message: SafeMessage, order: Float): Unit = messageDao.insert(RoomMessage.fromMessage(message, order))
    override suspend fun getAllByContact(contactId: UUID): List<SafeMessage> = messageDao.getAllByContact(contactId).map {
        it.toMessage()
    }

    override suspend fun getLastMessage(contactId: UUID): Flow<SafeMessage?> {
        return messageDao.getLastMessage(contactId).map { it?.toMessage() }
    }

    override suspend fun getLastByContact(contactId: UUID, exceptIds: List<UUID>): MessageOrder? = messageDao.getLastMessageOrderByContact(
        contactId,
        exceptIds,
    )

    override suspend fun getFirstByContact(
        contactId: UUID,
        exceptIds: List<UUID>,
    ): MessageOrder? = messageDao.getFirstMessageOrderByContact(contactId, exceptIds)

    override suspend fun countByContact(contactId: UUID, exceptIds: List<UUID>): Int = messageDao.countByContact(contactId, exceptIds)
    override suspend fun getAtByContact(
        position: Int,
        contactId: UUID,
        exceptIds: List<UUID>,
    ): MessageOrder? = messageDao.getMessageOrderAtByContact(
        position,
        contactId,
        exceptIds,
    )

    override suspend fun getByContactByOrder(contactId: UUID, order: Float): SafeMessage = messageDao.getByContactByOrder(contactId, order)
        .toMessage()

    override fun getAllPaged(config: PagingConfig, contactId: UUID): Flow<PagingData<SafeMessage>> {
        return Pager(config = config) {
            messageDao.getAllAsPagingSource(contactId)
        }.flow.mapPagingValues(RoomMessage::toMessage)
    }

    override suspend fun deleteAllMessages(contactId: UUID) {
        messageDao.deleteAllMessages(contactId)
    }

    override suspend fun deleteMessage(messageId: UUID) {
        messageDao.deleteMessage(messageId)
    }

    override suspend fun markMessagesAsRead(contactId: UUID) {
        messageDao.markMessagesAsRead(contactId)
    }
}
