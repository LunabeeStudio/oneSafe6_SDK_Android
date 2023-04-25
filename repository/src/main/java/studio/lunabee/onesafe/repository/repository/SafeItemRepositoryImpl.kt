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
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemWithIdentifier
import studio.lunabee.onesafe.domain.model.search.IndexWordEntry
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.repository.datasource.SafeItemLocalDataSource
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class SafeItemRepositoryImpl @Inject constructor(
    private val localDataSource: SafeItemLocalDataSource,
) : SafeItemRepository {
    override suspend fun getSafeItem(id: UUID): SafeItem = localDataSource.getSafeItem(id)
    override fun getSafeItemWithIdentifier(ids: List<UUID>): Flow<List<SafeItemWithIdentifier>> =
        localDataSource.getSafeItemWithIdentifier(ids)

    override fun getSafeItemFlow(id: UUID): Flow<SafeItem?> = localDataSource.getSafeItemFlow(id)
    override suspend fun getChildren(parentId: UUID): List<SafeItem> = localDataSource.findByParentId(parentId)

    override fun countSafeItemByParentIdFlow(
        parentId: UUID?,
    ): Flow<Int> = localDataSource.countSafeItemByParentIdFlow(parentId)

    override suspend fun countSafeItemByParentId(parentId: UUID?): Int =
        localDataSource.countSafeItemByParentId(parentId)

    override suspend fun save(item: SafeItem, safeItemKey: SafeItemKey, indexWordEntries: List<IndexWordEntry>?) =
        localDataSource.save(item, safeItemKey, null, indexWordEntries)

    override suspend fun save(
        items: List<SafeItem>,
        safeItemKeys: List<SafeItemKey>,
        fields: List<SafeItemField>?,
        indexWordEntries: List<IndexWordEntry>?,
    ) = localDataSource.save(items, safeItemKeys, fields, indexWordEntries)

    override fun getPagerItemByParents(
        config: PagingConfig,
        parentId: UUID?,
    ): Flow<PagingData<SafeItem>> = localDataSource.getPagerItemByParentId(config, parentId)

    override fun getPagerItemFavorite(
        config: PagingConfig,
    ): Flow<PagingData<SafeItem>> = localDataSource.getPagerItemFavorite(config)

    override fun findLastFavorite(limit: Int): Flow<List<SafeItem>> {
        return localDataSource.findLastFavorite(limit)
    }

    override fun countAllFavoriteFlow(): Flow<Int> {
        return localDataSource.countAllFavoriteFlow()
    }

    override suspend fun countAllFavorite(): Int {
        return localDataSource.countAllFavorite()
    }

    override suspend fun updateIcon(id: UUID, iconId: UUID?) = localDataSource.updateIcon(id, iconId)
    override suspend fun toggleFavorite(id: UUID) = localDataSource.toggleFavorite(id)
    override suspend fun getHighestChildPosition(parentId: UUID?): Double? = localDataSource.getHighestPosition(
        parentId,
    )

    override suspend fun getNextSiblingPosition(id: UUID): Double? = localDataSource.getNextSiblingPosition(id)

    override suspend fun setDeletedAndRemoveFromFavorite(id: UUID) = localDataSource.setDeletedAndRemoveFromFavorite(
        id,
    )

    override suspend fun updateParentIds(
        oldParentId: UUID,
        newParentId: UUID?,
        newDeletedParentId: UUID?,
    ) = localDataSource.updateParentIds(
        oldParentId,
        newParentId,
        newDeletedParentId,
    )

    override suspend fun updateSafeItem(safeItem: SafeItem, indexWordEntries: List<IndexWordEntry>?) =
        localDataSource.updateSafeItem(
            safeItem,
            indexWordEntries,
        )

    override suspend fun findByIdWithChildren(id: UUID): List<SafeItem> {
        return localDataSource.findByIdWithChildren(id)
    }

    override suspend fun findByIdWithAncestors(id: UUID): List<SafeItem> {
        return localDataSource.findByIdWithAncestors(id)
    }

    override suspend fun getSafeItemName(id: UUID): ByteArray? = localDataSource.getSafeItemName(id)
    override fun getAllSafeItems(limit: Int): Flow<List<SafeItem>> = localDataSource.getAllSafeItems(limit)
    override suspend fun getAllSafeItems(): List<SafeItem> {
        return localDataSource.getAllSafeItems()
    }

    override fun getAllSafeItemsWithIdentifier(config: PagingConfig, idsToExclude: List<UUID>): Flow<PagingData<SafeItemWithIdentifier>> {
        return localDataSource.getAllSafeItemsWithIdentifier(config, idsToExclude)
    }

    override fun getSafeItemsCount(): Flow<Int> = localDataSource.getSafeItemsCount()

    override fun getSafeItemsWithIdentifierCount(): Flow<Int> = localDataSource.getSafeItemsWithIdentifierCount()

    override suspend fun getAllSafeItemIds(): List<UUID> {
        return localDataSource.getAllSafeItemIds()
    }

    override suspend fun updateSafeItemParentId(itemId: UUID, parentId: UUID?) {
        localDataSource.updateSafeItemParentId(itemId, parentId)
    }

    override suspend fun updateConsultedAt(itemId: UUID, consultedAt: Instant) {
        localDataSource.updateConsultedAt(itemId, consultedAt)
    }

    override fun getLastConsultedNotDeletedSafeItem(limit: Int): Flow<List<SafeItem>> {
        return localDataSource.getLastConsultedNotDeletedSafeItem(limit)
    }

    override suspend fun getSafeItemsAndChildren(itemId: UUID, includeChildren: Boolean): List<SafeItem> {
        val items = mutableListOf<SafeItem>()
        if (includeChildren) {
            items.addAll(findByIdWithChildren(itemId))
        } else {
            items.add(localDataSource.getSafeItem(itemId))
        }
        return items
    }
}
