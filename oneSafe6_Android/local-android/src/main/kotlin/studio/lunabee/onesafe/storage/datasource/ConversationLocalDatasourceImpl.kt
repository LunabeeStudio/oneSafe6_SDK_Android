/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 7/29/2024 - for the oneSafe6 SDK.
 * Last modified 29/07/2024 14:12
 */

package studio.lunabee.onesafe.storage.datasource

import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.model.EncConversation
import studio.lunabee.messaging.repository.datasource.ConversationLocalDatasource
import studio.lunabee.onesafe.storage.dao.DoubleRatchetConversationDao
import studio.lunabee.onesafe.storage.model.RoomDoubleRatchetConversation
import javax.inject.Inject

class ConversationLocalDatasourceImpl @Inject constructor(
    private val conversationDao: DoubleRatchetConversationDao,
) : ConversationLocalDatasource {
    override suspend fun insert(conversation: EncConversation) {
        conversationDao.insert(RoomDoubleRatchetConversation.fromEncConversation(conversation))
    }

    override suspend fun getById(id: DoubleRatchetUUID): EncConversation? = conversationDao
        .getById(id.uuid)
        ?.toEncConversation()
}
