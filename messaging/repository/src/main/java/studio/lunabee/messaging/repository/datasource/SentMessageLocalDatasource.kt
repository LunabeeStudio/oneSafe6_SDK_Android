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
 * Last modified 23/08/2023 13:14
 */

package studio.lunabee.messaging.repository.datasource

import studio.lunabee.onesafe.messaging.domain.model.SentMessage
import java.util.UUID

interface SentMessageLocalDatasource {
    suspend fun saveSentMessage(sentMessage: SentMessage)
    suspend fun getSentMessage(id: UUID): SentMessage?
    suspend fun deleteSentMessage(id: UUID)
    suspend fun getOldestSentMessage(): SentMessage?
}
