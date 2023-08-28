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
 * Created by Lunabee Studio / Date - 8/23/2023 - for the oneSafe6 SDK.
 * Last modified 23/08/2023 13:13
 */

package studio.lunabee.onesafe.storage.datasource

import studio.lunabee.messaging.repository.datasource.SentMessageLocalDatasource
import studio.lunabee.onesafe.messaging.domain.model.SentMessage
import studio.lunabee.onesafe.storage.dao.SentMessageDao
import studio.lunabee.onesafe.storage.model.RoomSentMessage
import java.util.UUID
import javax.inject.Inject

class SentMessageLocalDatasourceImpl @Inject constructor(
    private val sentMessageDao: SentMessageDao,
) : SentMessageLocalDatasource {
    override suspend fun saveSentMessage(sentMessage: SentMessage) {
        sentMessageDao.insert(RoomSentMessage.fromSentMessage(sentMessage))
    }

    override suspend fun getSentMessage(id: UUID): SentMessage? {
        return sentMessageDao.getById(id)?.toSentMessage()
    }

    override suspend fun getOldestSentMessage(): SentMessage? {
        return sentMessageDao.getOldestSentMessage()?.toSentMessage()
    }

    override suspend fun deleteSentMessage(id: UUID) {
        sentMessageDao.deleteSentMessage(id)
    }
}
