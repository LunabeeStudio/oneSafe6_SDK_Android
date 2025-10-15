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
 * Created by Lunabee Studio / Date - 9/20/2023 - for the oneSafe6 SDK.
 * Last modified 9/20/23, 9:17 AM
 */

package studio.lunabee.messaging.repository

import studio.lunabee.doubleratchet.model.Conversation
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.storage.DoubleRatchetLocalDatasource
import studio.lunabee.messaging.domain.repository.ConversationRepository
import studio.lunabee.onesafe.di.Inject

class ConversationRepositoryImpl @Inject constructor(
    private val doubleRatchetLocalDatasource: DoubleRatchetLocalDatasource,
) : ConversationRepository {
    override suspend fun getConversation(id: DoubleRatchetUUID): Conversation? = doubleRatchetLocalDatasource
        .getConversation(id)
}
