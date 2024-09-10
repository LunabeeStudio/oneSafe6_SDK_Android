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
 * Created by Lunabee Studio / Date - 6/22/2023 - for the oneSafe6 SDK.
 * Last modified 6/22/23, 10:52 AM
 */

package studio.lunabee.messaging.repository

import kotlinx.coroutines.flow.Flow
import studio.lunabee.bubbles.domain.di.Inject
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.model.SafeMessage
import studio.lunabee.messaging.domain.repository.MessageRepository
import studio.lunabee.messaging.repository.datasource.MessageLocalDataSource

class MessageRepositoryImpl @Inject constructor(
    private val datasource: MessageLocalDataSource,
) : MessageRepository {
    override suspend fun save(message: SafeMessage, order: Float): Unit = datasource.save(message, order)
    override suspend fun getAllByContact(contactId: DoubleRatchetUUID): List<SafeMessage> = datasource.getAllByContact(contactId)
    override suspend fun getLastMessage(contactId: DoubleRatchetUUID): Flow<SafeMessage?> {
        return datasource.getLastMessage(contactId)
    }

    override suspend fun getByContactByOrder(contactId: DoubleRatchetUUID, order: Float): SafeMessage = datasource.getByContactByOrder(
        contactId,
        order,
    )

    override suspend fun deleteAllMessages(contactId: DoubleRatchetUUID) {
        datasource.deleteAllMessages(contactId)
    }

    override suspend fun deleteMessage(messageId: DoubleRatchetUUID) {
        datasource.deleteMessage(messageId)
    }

    override suspend fun markMessagesAsRead(contactId: DoubleRatchetUUID) {
        datasource.markMessagesAsRead(contactId)
    }
}
