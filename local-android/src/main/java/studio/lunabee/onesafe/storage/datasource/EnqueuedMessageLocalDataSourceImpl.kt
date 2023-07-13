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
 * Last modified 6/26/23, 8:24 AM
 */

package studio.lunabee.onesafe.storage.datasource

import android.database.sqlite.SQLiteConstraintException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import studio.lunabee.messaging.repository.datasource.EnqueuedMessageLocalDataSource
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.messaging.domain.model.EnqueuedMessage
import studio.lunabee.onesafe.storage.dao.EnqueuedMessageDao
import studio.lunabee.onesafe.storage.model.RoomEnqueuedMessage
import javax.inject.Inject

class EnqueuedMessageLocalDataSourceImpl @Inject constructor(
    private val dao: EnqueuedMessageDao,
) : EnqueuedMessageLocalDataSource {
    override suspend fun save(encMessage: ByteArray, encChannel: ByteArray?): Unit = try {
        dao.insert(RoomEnqueuedMessage.fromEnqueuedMessage(encMessage, encChannel))
    } catch (e: SQLiteConstraintException) {
        throw OSStorageError(OSStorageError.Code.ENQUEUED_MESSAGE_ALREADY_EXIST_ERROR)
    }

    override suspend fun getAll(): List<EnqueuedMessage> = dao.getAll().map(RoomEnqueuedMessage::toEnqueuedMessage)
    override suspend fun delete(id: Int) {
        if (dao.delete(id) != 1) {
            throw OSStorageError(OSStorageError.Code.ENQUEUED_MESSAGE_NOT_FOUND_FOR_DELETE)
        }
    }

    override suspend fun getOldestAsFlow(): Flow<EnqueuedMessage?> = dao.getOldestAsFlow()
        .distinctUntilChanged { old, new ->
            old?.id == new?.id
        }.map {
            it?.toEnqueuedMessage()
        }
}
