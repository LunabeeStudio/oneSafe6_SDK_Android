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
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:24 AM
 */

package studio.lunabee.onesafe.storage.datasource

import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.repository.datasource.SafeItemKeyLocalDataSource
import studio.lunabee.onesafe.storage.dao.SafeItemKeyDao
import studio.lunabee.onesafe.storage.model.RoomSafeItemKey
import java.util.UUID
import javax.inject.Inject

class SafeItemKeyLocalDataSourceImpl @Inject constructor(
    private val safeItemKeyDao: SafeItemKeyDao,
) : SafeItemKeyLocalDataSource {
    override suspend fun getSafeItemKey(id: UUID): SafeItemKey {
        return (safeItemKeyDao.findById(id) ?: throw OSStorageError(OSStorageError.Code.ITEM_KEY_NOT_FOUND)).toSafeItemKey()
    }

    override suspend fun getSafeItemKeys(ids: List<UUID>): List<SafeItemKey> {
        return safeItemKeyDao.findByIds(ids).map { it.toSafeItemKey() }.takeIf { it.size == ids.size }
            ?: throw OSStorageError(OSStorageError.Code.ITEM_KEY_NOT_FOUND)
    }

    override suspend fun save(itemKey: SafeItemKey) {
        safeItemKeyDao.insert(RoomSafeItemKey.fromSafeItemKey(itemKey))
    }

    override suspend fun update(itemKeys: List<SafeItemKey>) {
        safeItemKeyDao.update(itemKeys.map(RoomSafeItemKey::fromSafeItemKey))
    }

    override suspend fun getAllSafeItemKeys(safeId: SafeId): List<SafeItemKey> {
        return safeItemKeyDao.getAllSafeItemKeys(safeId).map { it.toSafeItemKey() }
    }
}
