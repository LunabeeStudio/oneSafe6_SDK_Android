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
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safeitem.ItemNameWithIndex
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemIdName
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
    override fun getSafeItemListWithIdentifier(ids: Collection<UUID>, order: ItemOrder): Flow<List<SafeItemWithIdentifier>> =
        localDataSource.getSafeItemListWithIdentifier(ids, order)

    override fun getSafeItemWithIdentifier(id: UUID): Flow<SafeItemWithIdentifier?> =
        localDataSource.getSafeItemWithIdentifier(id)

    override fun getSafeItemFlow(id: UUID): Flow<SafeItem?> = localDataSource.getSafeItemFlow(id)
    override suspend fun getChildren(parentId: UUID, order: ItemOrder, safeId: SafeId): List<SafeItem> = localDataSource.findByParentId(
        parentId,
        order,
        safeId,
    )

    override fun countSafeItemByParentIdFlow(
        parentId: UUID?,
        safeId: SafeId,
    ): Flow<Int> = localDataSource.countSafeItemByParentIdFlow(parentId, safeId)

    override suspend fun countSafeItemByParentId(parentId: UUID?, safeId: SafeId): Int =
        localDataSource.countSafeItemByParentId(parentId, safeId)

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
        order: ItemOrder,
        safeId: SafeId,
    ): Flow<PagingData<SafeItem>> = localDataSource.getPagerItemByParentId(config, parentId, order, safeId)

    override fun getPagerItemByParentsWithIdentifier(
        config: PagingConfig,
        parentId: UUID?,
        order: ItemOrder,
        safeId: SafeId,
    ): Flow<PagingData<SafeItemWithIdentifier>> =
        localDataSource.getPagerItemByParentIdWithIdentifier(config, parentId, order, safeId)

    override fun getPagerItemFavorite(
        config: PagingConfig,
        order: ItemOrder,
        safeId: SafeId,
    ): Flow<PagingData<SafeItem>> = localDataSource.getPagerItemFavorite(config, order, safeId)

    override fun getPagerItemFavoriteWithIdentifier(
        pagingConfig: PagingConfig,
        itemOrder: ItemOrder,
        safeId: SafeId,
    ): Flow<PagingData<SafeItemWithIdentifier>> =
        localDataSource.getPagerItemFavoriteWithIdentifier(pagingConfig, itemOrder, safeId)

    override fun findLastFavorite(limit: Int, order: ItemOrder, safeId: SafeId): Flow<List<SafeItem>> {
        return localDataSource.findLastFavorite(limit, order, safeId)
    }

    override fun countAllFavoriteFlow(safeId: SafeId): Flow<Int> {
        return localDataSource.countAllFavoriteFlow(safeId)
    }

    override suspend fun countAllFavorite(safeId: SafeId): Int {
        return localDataSource.countAllFavorite(safeId)
    }

    override suspend fun updateIcon(id: UUID, iconId: UUID?) = localDataSource.updateIcon(id, iconId)
    override suspend fun toggleFavorite(id: UUID) = localDataSource.toggleFavorite(id)
    override suspend fun getHighestChildPosition(parentId: UUID?, safeId: SafeId): Double? = localDataSource.getHighestPosition(
        parentId,
        safeId,
    )

    override suspend fun getNextSiblingPosition(id: UUID): Double? = localDataSource.getNextSiblingPosition(id)
    override suspend fun setDeletedAndRemoveFromFavorite(
        id: UUID?,
        deletedAt: Instant,
        safeId: SafeId,
    ): Unit = localDataSource.setDeletedAndRemoveFromFavorite(id, deletedAt, safeId)

    override suspend fun updateParentIds(
        oldParentId: UUID,
        newParentId: UUID?,
        newDeletedParentId: UUID?,
    ): Unit = localDataSource.updateParentIds(
        oldParentId,
        newParentId,
        newDeletedParentId,
    )

    override suspend fun updateSafeItem(safeItem: SafeItem, indexWordEntries: List<IndexWordEntry>?): Unit =
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
    override suspend fun getAllSafeItems(safeId: SafeId): List<SafeItem> {
        return localDataSource.getAllSafeItems(safeId)
    }

    override fun getAllSafeItemsWithIdentifier(
        config: PagingConfig,
        idsToExclude: List<UUID>,
        order: ItemOrder,
        safeId: SafeId,
    ): Flow<PagingData<SafeItemWithIdentifier>> {
        return localDataSource.getAllSafeItemsWithIdentifier(config, idsToExclude, order, safeId)
    }

    override fun getSafeItemsCountFlow(safeId: SafeId): Flow<Int> = localDataSource.getSafeItemsCountFlow(safeId)
    override suspend fun getSafeItemsCount(safeId: SafeId): Int = localDataSource.getSafeItemsCount(safeId)

    override fun getSafeItemsWithIdentifierCount(safeId: SafeId): Flow<Int> = localDataSource.getSafeItemsWithIdentifierCount(safeId)

    override suspend fun getAllSafeItemIds(safeId: SafeId): List<UUID> {
        return localDataSource.getAllSafeItemIds(safeId)
    }

    override suspend fun updateSafeItemParentId(itemId: UUID, parentId: UUID?) {
        localDataSource.updateSafeItemParentId(itemId, parentId)
    }

    override suspend fun updateConsultedAt(itemId: UUID, consultedAt: Instant) {
        localDataSource.updateConsultedAt(itemId, consultedAt)
    }

    override fun getLastConsultedNotDeletedSafeItem(limit: Int, safeId: SafeId): Flow<List<SafeItem>> {
        return localDataSource.getLastConsultedNotDeletedSafeItem(limit, safeId)
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

    override suspend fun setAlphaIndices(indices: List<Pair<UUID, Double>>) {
        localDataSource.setAlphaIndices(indices)
    }

    override suspend fun getItemNameWithIndexAt(index: Int, safeId: SafeId): ItemNameWithIndex? {
        return localDataSource.getItemNameWithIndexAt(index, safeId)
    }

    override suspend fun getAlphaIndexRange(safeId: SafeId): Pair<Double, Double> {
        return localDataSource.getAlphaIndexRange(safeId)
    }

    override suspend fun getAllSafeItemIdName(safeId: SafeId): List<SafeItemIdName> {
        return localDataSource.getAllSafeItemIdName(safeId)
    }
}
