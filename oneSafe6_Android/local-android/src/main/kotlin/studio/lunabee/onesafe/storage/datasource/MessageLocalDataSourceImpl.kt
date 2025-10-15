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
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.model.MessageOrder
import studio.lunabee.messaging.domain.model.SafeMessage
import studio.lunabee.messaging.repository.datasource.MessageLocalDataSource
import studio.lunabee.messaging.repository.datasource.MessagePagingLocalDataSource
import studio.lunabee.onesafe.jvm.mapPagingValues
import studio.lunabee.onesafe.storage.dao.MessageDao
import studio.lunabee.onesafe.storage.model.RoomMessage
import javax.inject.Inject

class MessageLocalDataSourceImpl @Inject constructor(
    private val messageDao: MessageDao,
) : MessageLocalDataSource, MessagePagingLocalDataSource {
    override suspend fun save(message: SafeMessage, order: Float): Unit = messageDao
        .insert(RoomMessage.fromMessage(message, order))

    override suspend fun getAllByContact(contactId: DoubleRatchetUUID): List<SafeMessage> = messageDao
        .getAllByContact(contactId.uuid)
        .map { it.toMessage() }

    override suspend fun getLastMessage(contactId: DoubleRatchetUUID): Flow<SafeMessage?> = messageDao
        .getLastMessage(
            contactId.uuid,
        ).map { it?.toMessage() }

    override suspend fun getLastByContact(
        contactId: DoubleRatchetUUID,
        exceptIds: List<DoubleRatchetUUID>,
    ): MessageOrder? = messageDao
        .getLastMessageOrderByContact(
            contactId.uuid,
            exceptIds.map { it.uuid },
        )?.toMessageOrder()

    override suspend fun getFirstByContact(
        contactId: DoubleRatchetUUID,
        exceptIds: List<DoubleRatchetUUID>,
    ): MessageOrder? = messageDao
        .getFirstMessageOrderByContact(contactId.uuid, exceptIds.map { it.uuid })
        ?.toMessageOrder()

    override suspend fun countByContact(
        contactId: DoubleRatchetUUID,
        exceptIds: List<DoubleRatchetUUID>,
    ): Int = messageDao.countByContact(contactId.uuid, exceptIds.map { it.uuid })

    override suspend fun getAtByContact(
        position: Int,
        contactId: DoubleRatchetUUID,
        exceptIds: List<DoubleRatchetUUID>,
    ): MessageOrder? = messageDao
        .getMessageOrderAtByContact(
            position,
            contactId.uuid,
            exceptIds.map { it.uuid },
        )?.toMessageOrder()

    override suspend fun getByContactByOrder(
        contactId: DoubleRatchetUUID,
        order: Float,
    ): SafeMessage = messageDao.getByContactByOrder(contactId.uuid, order).toMessage()

    override fun getAllPaged(config: PagingConfig, contactId: DoubleRatchetUUID): Flow<PagingData<SafeMessage>> = Pager(config = config) {
        messageDao.getAllAsPagingSource(contactId.uuid)
    }.flow
        .mapPagingValues(RoomMessage::toMessage)

    override suspend fun deleteAllMessages(contactId: DoubleRatchetUUID) {
        messageDao.deleteAllMessages(contactId.uuid)
    }

    override suspend fun deleteMessage(messageId: DoubleRatchetUUID) {
        messageDao.deleteMessage(messageId.uuid)
    }

    override suspend fun markMessagesAsRead(contactId: DoubleRatchetUUID) {
        messageDao.markMessagesAsRead(contactId.uuid)
    }
}
