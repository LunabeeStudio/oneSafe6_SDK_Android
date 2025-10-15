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

package studio.lunabee.onesafe.repository.repository

import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.repository.datasource.SafeItemKeyLocalDataSource
import java.util.UUID
import javax.inject.Inject

class SafeItemKeyRepositoryImpl @Inject constructor(
    private val safeItemKeyLocalDataSource: SafeItemKeyLocalDataSource,
) : SafeItemKeyRepository {
    override suspend fun save(itemKey: SafeItemKey) = safeItemKeyLocalDataSource.save(itemKey)

    override suspend fun update(itemKeys: List<SafeItemKey>) = safeItemKeyLocalDataSource.update(itemKeys)

    override suspend fun getSafeItemKey(id: UUID): SafeItemKey = safeItemKeyLocalDataSource.getSafeItemKey(id)

    override suspend fun getSafeItemKeys(ids: List<UUID>): List<SafeItemKey> = safeItemKeyLocalDataSource
        .getSafeItemKeys(ids)

    override suspend fun getAllSafeItemKeys(safeId: SafeId): List<SafeItemKey> = safeItemKeyLocalDataSource
        .getAllSafeItemKeys(safeId)
}
