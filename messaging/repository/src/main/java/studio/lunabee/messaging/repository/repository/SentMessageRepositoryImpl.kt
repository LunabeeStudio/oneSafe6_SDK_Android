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
 * Last modified 23/08/2023 13:20
 */

package studio.lunabee.messaging.repository.repository

import studio.lunabee.messaging.repository.datasource.SentMessageLocalDatasource
import studio.lunabee.onesafe.messaging.domain.model.SentMessage
import studio.lunabee.onesafe.messaging.domain.repository.SentMessageRepository
import java.util.UUID
import javax.inject.Inject

class SentMessageRepositoryImpl @Inject constructor(
    private val datasource: SentMessageLocalDatasource,
) : SentMessageRepository {
    override suspend fun saveSentMessage(sentMessage: SentMessage) {
        datasource.saveSentMessage(sentMessage)
    }

    override suspend fun deleteSentMessage(id: UUID) {
        datasource.deleteSentMessage(id)
    }

    override suspend fun getSentMessage(id: UUID): SentMessage? {
        return datasource.getSentMessage(id)
    }

    override suspend fun getOldestSentMessage(): SentMessage? {
        return datasource.getOldestSentMessage()
    }
}
