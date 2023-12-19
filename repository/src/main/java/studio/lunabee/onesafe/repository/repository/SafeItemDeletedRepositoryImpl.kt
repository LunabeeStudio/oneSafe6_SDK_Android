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

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.repository.SafeItemDeletedRepository
import studio.lunabee.onesafe.repository.datasource.SafeItemLocalDataSource
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class SafeItemDeletedRepositoryImpl @Inject constructor(
    private val localDataSource: SafeItemLocalDataSource,
) : SafeItemDeletedRepository {
    override suspend fun getDeletedItemsByDeletedParent(deletedParentId: UUID?, order: ItemOrder): List<SafeItem> =
        localDataSource.findByDeletedParentId(deletedParentId, order)

    override suspend fun getSiblingOriginalChildren(parentId: UUID, order: ItemOrder): List<SafeItem> =
        localDataSource.getSiblingOriginalChildren(parentId, order)

    override suspend fun updateSiblingOriginalChildrenParentId(parentId: UUID, newParentId: UUID?): Unit =
        localDataSource.updateSiblingOriginalChildrenParentId(parentId, newParentId)

    override fun countSafeItemByParentIdDeletedFlow(
        parentId: UUID?,
    ): Flow<Int> = localDataSource.countSafeItemByParentIdDeletedFlow(parentId)

    override suspend fun countSafeItemByParentIdDeleted(parentId: UUID?): Int =
        localDataSource.countSafeItemByParentIdDeleted(parentId)

    override fun getPagerItemByParentIdDeleted(
        config: PagingConfig,
        parentId: UUID?,
        order: ItemOrder,
    ): Flow<PagingData<SafeItem>> = localDataSource.getPagerItemByParentIdDeleted(config, parentId, order)

    override fun countAllDeletedWithNonDeletedParent(): Flow<Int> {
        return localDataSource.countAllDeletedWithNonDeletedParent()
    }

    override suspend fun getHighestDeletedPosition(parentId: UUID?): Double? = localDataSource.getHighestDeletedPosition(
        parentId,
    )

    override suspend fun removeItem(id: UUID) = localDataSource.removeItem(id)

    override suspend fun removeItems(ids: List<UUID>) = localDataSource.removeItems(ids)

    override suspend fun restoreItemToParentWithDescendants(id: UUID?) = localDataSource.restoreItemToParentWithDescendants(
        id,
    )

    override suspend fun updateParentToNonDeletedAncestor(id: UUID) = localDataSource.updateParentToNonDeletedAncestor(
        id,
    )

    override suspend fun findDeletedByIdWithDeletedDescendants(id: UUID): List<SafeItem> {
        return localDataSource.findDeletedByIdWithDeletedDescendants(id)
    }

    override suspend fun findByIdWithDeletedAncestors(id: UUID): List<SafeItem> {
        return localDataSource.findByIdWithDeletedAncestors(id)
    }

    override suspend fun removeOldItems(threshold: Instant) = localDataSource.removeOldItems(threshold)

    override fun getAllDeletedItemsCount(): Flow<Int> = localDataSource.getAllDeletedItemsCount()
}
