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

package studio.lunabee.messaging.repository.repository

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import studio.lunabee.messaging.repository.datasource.MessageLocalDataSource
import studio.lunabee.onesafe.messaging.domain.model.Message
import studio.lunabee.onesafe.messaging.domain.repository.MessageRepository
import java.util.UUID
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val datasource: MessageLocalDataSource,
) : MessageRepository {
    override suspend fun save(message: Message, order: Float): Unit = datasource.save(message, order)
    override suspend fun getAllByContact(contactId: UUID): List<Message> = datasource.getAllByContact(contactId)
    override suspend fun getByContactByOrder(contactId: UUID, order: Float): Message = datasource.getByContactByOrder(contactId, order)
    override fun getAllPaged(config: PagingConfig, contactId: UUID): Flow<PagingData<Message>> =
        datasource.getAllPaged(config, contactId)
}
