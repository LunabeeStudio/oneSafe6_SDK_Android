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

package studio.lunabee.onesafe.domain.repository

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safeitem.ItemNameWithIndex
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemIdName
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemWithIdentifier
import studio.lunabee.onesafe.domain.model.search.IndexWordEntry
import java.time.Instant
import java.util.UUID

interface SafeItemRepository {
    suspend fun getSafeItem(id: UUID): SafeItem
    fun getSafeItemWithIdentifier(ids: Collection<UUID>, order: ItemOrder): Flow<List<SafeItemWithIdentifier>>
    fun getSafeItemFlow(id: UUID): Flow<SafeItem?>
    suspend fun getChildren(parentId: UUID, order: ItemOrder): List<SafeItem>
    fun countSafeItemByParentIdFlow(parentId: UUID?): Flow<Int>
    suspend fun countSafeItemByParentId(parentId: UUID?): Int
    suspend fun save(item: SafeItem, safeItemKey: SafeItemKey, indexWordEntries: List<IndexWordEntry>?)
    suspend fun save(
        items: List<SafeItem>,
        safeItemKeys: List<SafeItemKey>,
        fields: List<SafeItemField>?,
        indexWordEntries: List<IndexWordEntry>?,
    )

    fun getPagerItemByParents(config: PagingConfig, parentId: UUID?, order: ItemOrder): Flow<PagingData<SafeItem>>
    fun getPagerItemByParentsWithIdentifier(
        config: PagingConfig,
        parentId: UUID?,
        order: ItemOrder,
    ): Flow<PagingData<SafeItemWithIdentifier>>

    fun getPagerItemFavorite(config: PagingConfig, order: ItemOrder): Flow<PagingData<SafeItem>>
    fun getPagerItemFavoriteWithIdentifier(pagingConfig: PagingConfig, itemOrder: ItemOrder): Flow<PagingData<SafeItemWithIdentifier>>
    fun findLastFavorite(limit: Int, order: ItemOrder): Flow<List<SafeItem>>
    fun countAllFavoriteFlow(): Flow<Int>
    suspend fun countAllFavorite(): Int
    suspend fun updateIcon(id: UUID, iconId: UUID?)
    suspend fun toggleFavorite(id: UUID)
    suspend fun getHighestChildPosition(parentId: UUID?): Double?
    suspend fun getNextSiblingPosition(id: UUID): Double?
    suspend fun setDeletedAndRemoveFromFavorite(id: UUID?, deletedAt: Instant)
    suspend fun updateParentIds(oldParentId: UUID, newParentId: UUID?, newDeletedParentId: UUID?)
    suspend fun updateSafeItem(safeItem: SafeItem, indexWordEntries: List<IndexWordEntry>?)
    suspend fun findByIdWithChildren(id: UUID): List<SafeItem>
    suspend fun findByIdWithAncestors(id: UUID): List<SafeItem>
    suspend fun getSafeItemName(id: UUID): ByteArray?

    suspend fun getAllSafeItems(): List<SafeItem>
    fun getAllSafeItemsWithIdentifier(
        config: PagingConfig,
        idsToExclude: List<UUID>,
        order: ItemOrder,
    ): Flow<PagingData<SafeItemWithIdentifier>>

    suspend fun getAllSafeItemIds(): List<UUID>
    fun getSafeItemsCountFlow(): Flow<Int>
    suspend fun getSafeItemsCount(): Int
    fun getSafeItemsWithIdentifierCount(): Flow<Int>
    suspend fun updateSafeItemParentId(itemId: UUID, parentId: UUID?)
    suspend fun updateConsultedAt(itemId: UUID, consultedAt: Instant)
    fun getLastConsultedNotDeletedSafeItem(limit: Int): Flow<List<SafeItem>>
    suspend fun getSafeItemsAndChildren(itemId: UUID, includeChildren: Boolean): List<SafeItem>

    suspend fun setAlphaIndices(indices: List<Pair<UUID, Double>>)
    suspend fun getItemNameWithIndexAt(index: Int): ItemNameWithIndex?
    suspend fun getAlphaIndexRange(): Pair<Double, Double>
    suspend fun getAllSafeItemIdName(): List<SafeItemIdName>
}
