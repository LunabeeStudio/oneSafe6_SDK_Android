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
 * Last modified 6/20/23, 1:25 PM
 */

package studio.lunabee.onesafe.messaging.domain.repository

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.messaging.domain.model.Message
import java.util.UUID

interface MessageRepository {
    suspend fun save(message: Message, order: Float)
    suspend fun getAllByContact(contactId: UUID): List<Message>
    suspend fun getLastMessage(contactId: UUID): Flow<Message?>
    suspend fun getByContactByOrder(contactId: UUID, order: Float): Message
    fun getAllPaged(config: PagingConfig, contactId: UUID): Flow<PagingData<Message>>
    suspend fun deleteAllMessages(contactId: UUID)
    suspend fun deleteMessage(messageId: UUID)
}
