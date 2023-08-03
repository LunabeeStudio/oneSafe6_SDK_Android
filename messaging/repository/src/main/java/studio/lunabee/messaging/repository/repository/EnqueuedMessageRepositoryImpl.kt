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
 * Last modified 6/26/23, 8:27 AM
 */

package studio.lunabee.messaging.repository.repository

import kotlinx.coroutines.flow.Flow
import studio.lunabee.messaging.repository.datasource.EnqueuedMessageLocalDataSource
import studio.lunabee.onesafe.messaging.domain.model.EnqueuedMessage
import studio.lunabee.onesafe.messaging.domain.repository.EnqueuedMessageRepository
import javax.inject.Inject

class EnqueuedMessageRepositoryImpl @Inject constructor(
    private val localDataSource: EnqueuedMessageLocalDataSource,
) : EnqueuedMessageRepository {
    override suspend fun getOldestAsFlow(): Flow<EnqueuedMessage?> = localDataSource.getOldestAsFlow()
    override suspend fun getAll(): List<EnqueuedMessage> = localDataSource.getAll()
    override suspend fun delete(id: Int): Unit = localDataSource.delete(id)
    override suspend fun save(encMessage: ByteArray, encChannel: ByteArray?): Unit = localDataSource.save(
        encMessage,
        encChannel,
    )

    override suspend fun deleteAll(): Unit = localDataSource.deleteAll()
}
