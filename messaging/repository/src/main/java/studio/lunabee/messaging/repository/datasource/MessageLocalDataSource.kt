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
 * Last modified 6/20/23, 1:27 PM
 */

package studio.lunabee.messaging.repository.datasource

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.messaging.domain.model.Message
import studio.lunabee.onesafe.messaging.domain.model.MessageOrder
import java.util.UUID

interface MessageLocalDataSource {
    suspend fun save(message: Message, order: Float)
    suspend fun getAllByContact(contactId: UUID): List<Message>
    suspend fun getLastByContact(contactId: UUID): MessageOrder?
    suspend fun getFirstByContact(contactId: UUID): MessageOrder?
    suspend fun countByContact(contactId: UUID): Int
    suspend fun getAtByContact(position: Int, contactId: UUID): MessageOrder?
    suspend fun getByContactByOrder(contactId: UUID, order: Float): Message
    fun getAllPaged(config: PagingConfig, contactId: UUID): Flow<PagingData<Message>>
}
