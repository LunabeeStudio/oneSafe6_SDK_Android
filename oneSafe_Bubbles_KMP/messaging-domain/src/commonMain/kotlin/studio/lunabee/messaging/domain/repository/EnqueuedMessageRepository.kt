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
 * Created by Lunabee Studio / Date - 6/26/2023 - for the oneSafe6 SDK.
 * Last modified 6/26/23, 8:09 AM
 */

package studio.lunabee.messaging.domain.repository

import kotlinx.coroutines.flow.Flow
import studio.lunabee.messaging.domain.model.EnqueuedMessage

interface EnqueuedMessageRepository {
    suspend fun getOldestAsFlow(): Flow<EnqueuedMessage?>
    suspend fun getAll(): List<EnqueuedMessage>
    suspend fun delete(id: Int)
    suspend fun save(encMessage: ByteArray, encChannel: ByteArray?)
    suspend fun deleteAll()
}
