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
 * Created by Lunabee Studio / Date - 6/21/2023 - for the oneSafe6 SDK.
 * Last modified 6/21/23, 11:41 AM
 */

package studio.lunabee.messaging.repository.repository

import studio.lunabee.messaging.repository.datasource.MessageLocalDataSource
import studio.lunabee.onesafe.messaging.domain.model.MessageOrder
import studio.lunabee.onesafe.messaging.domain.repository.MessageOrderRepository
import java.util.UUID
import javax.inject.Inject

class MessageOrderRepositoryImpl @Inject constructor(
    private val datasource: MessageLocalDataSource,
) : MessageOrderRepository {
    override suspend fun getMostRecent(contactId: UUID): MessageOrder? = datasource.getLastByContact(contactId)
    override suspend fun getLeastRecent(contactId: UUID): MessageOrder? = datasource.getFirstByContact(contactId)
    override suspend fun count(contactId: UUID): Int = datasource.countByContact(contactId)
    override suspend fun getAt(contactId: UUID, position: Int): MessageOrder? = datasource.getAtByContact(position, contactId)
}
