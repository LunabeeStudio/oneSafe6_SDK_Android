/*
 * Copyright (c) 2023-2024 Lunabee Studio
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
 */

package studio.lunabee.messaging.repository

import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.model.MessageOrder
import studio.lunabee.messaging.domain.repository.MessageOrderRepository
import studio.lunabee.messaging.repository.datasource.MessageLocalDataSource
import studio.lunabee.onesafe.di.Inject

class MessageOrderRepositoryImpl @Inject constructor(
    private val datasource: MessageLocalDataSource,
) : MessageOrderRepository {
    override suspend fun getMostRecent(
        contactId: DoubleRatchetUUID,
        exceptIds: List<DoubleRatchetUUID>,
    ): MessageOrder? = datasource.getLastByContact(
        contactId,
        exceptIds,
    )

    override suspend fun getLeastRecent(
        contactId: DoubleRatchetUUID,
        exceptIds: List<DoubleRatchetUUID>,
    ): MessageOrder? = datasource.getFirstByContact(
        contactId,
        exceptIds,
    )

    override suspend fun count(contactId: DoubleRatchetUUID, exceptIds: List<DoubleRatchetUUID>): Int = datasource
        .countByContact(
            contactId,
            exceptIds,
        )

    override suspend fun getAt(contactId: DoubleRatchetUUID, position: Int, exceptIds: List<DoubleRatchetUUID>): MessageOrder? =
        datasource.getAtByContact(position, contactId, exceptIds)
}
