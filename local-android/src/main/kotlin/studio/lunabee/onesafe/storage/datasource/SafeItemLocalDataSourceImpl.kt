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

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.lunabee.lbextensions.mapValues
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import studio.lunabee.importexport.repository.datasource.ImportExportSafeItemLocalDataSource
import studio.lunabee.onesafe.domain.model.safeitem.ItemNameWithIndex
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemIdName
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemWithIdentifier
import studio.lunabee.onesafe.domain.model.search.IndexWordEntry
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.mapPagingValues
import studio.lunabee.onesafe.repository.datasource.SafeItemLocalDataSource
import studio.lunabee.onesafe.storage.MainDatabase
import studio.lunabee.onesafe.storage.dao.IndexWordEntryDao
import studio.lunabee.onesafe.storage.dao.SafeItemDao
import studio.lunabee.onesafe.storage.dao.SafeItemFieldDao
import studio.lunabee.onesafe.storage.dao.SafeItemKeyDao
import studio.lunabee.onesafe.storage.dao.SafeItemRawDao
import studio.lunabee.onesafe.storage.model.ItemIdWithAlphaIndex
import studio.lunabee.onesafe.storage.model.RoomIndexWordEntry
import studio.lunabee.onesafe.storage.model.RoomSafeItem
import studio.lunabee.onesafe.storage.model.RoomSafeItemField
import studio.lunabee.onesafe.storage.model.RoomSafeItemKey
import studio.lunabee.onesafe.storage.model.RoomUpdateSafeItem
import studio.lunabee.onesafe.storage.utils.TransactionProvider
import studio.lunabee.onesafe.storage.utils.runSQL
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class SafeItemLocalDataSourceImpl @Inject constructor(
    private val safeItemDao: SafeItemDao,
    private val safeItemRawDao: SafeItemRawDao,
    private val safeItemKeyDao: SafeItemKeyDao,
    private val safeItemFieldDao: SafeItemFieldDao,
    private val indexWordEntryDao: IndexWordEntryDao,
    private val transactionProvider: TransactionProvider<MainDatabase>,
) : SafeItemLocalDataSource, ImportExportSafeItemLocalDataSource {

    override suspend fun save(
        item: SafeItem,
        safeItemKey: SafeItemKey,
        fields: List<SafeItemField>?,
        indexWordEntries: List<IndexWordEntry>?,
    ) {
        runSQL {
            transactionProvider.runAsTransaction {
                safeItemDao.insert(RoomSafeItem.fromSafeItem(item))
                safeItemKeyDao.insert(RoomSafeItemKey.fromSafeItemKey(safeItemKey))
                fields?.let { safeItemFieldDao.insert(it.map(RoomSafeItemField::fromSafeItemField)) }
                indexWordEntries?.let { indexWordEntryDao.insert(it.map(RoomIndexWordEntry::fromIndexWordEntry)) }
            }
        }
    }

    override suspend fun save(
        items: List<SafeItem>,
        safeItemKeys: List<SafeItemKey>,
        fields: List<SafeItemField>?,
        indexWordEntries: List<IndexWordEntry>?,
    ) {
        runSQL {
            transactionProvider.runAsTransaction {
                safeItemDao.insert(items.map(RoomSafeItem::fromSafeItem))
                safeItemKeyDao.insert(safeItemKeys.map { RoomSafeItemKey.fromSafeItemKey(it) })
                fields?.let { safeItemFieldDao.insert(it.map(RoomSafeItemField::fromSafeItemField)) }
                indexWordEntries?.let { indexWordEntryDao.insert(it.map(RoomIndexWordEntry::fromIndexWordEntry)) }
            }
        }
    }

    override suspend fun save(
        items: List<SafeItem>,
        safeItemKeys: List<SafeItemKey>,
        fields: List<SafeItemField>?,
        indexWordEntries: List<IndexWordEntry>?,
        updateItemsAlphaIndices: Map<UUID, Double>,
    ) {
        runSQL {
            transactionProvider.runAsTransaction {
                safeItemDao.updateAlphaIndices(
                    updateItemsAlphaIndices.map { (id, index) -> ItemIdWithAlphaIndex(id, index) },
                )
                safeItemDao.insert(items.map(RoomSafeItem::fromSafeItem))
                safeItemKeyDao.insert(safeItemKeys.map { RoomSafeItemKey.fromSafeItemKey(it) })
                fields?.let { safeItemFieldDao.insert(it.map(RoomSafeItemField::fromSafeItemField)) }
                indexWordEntries?.let { indexWordEntryDao.insert(it.map(RoomIndexWordEntry::fromIndexWordEntry)) }
            }
        }
    }

    override suspend fun updateIcon(id: UUID, iconId: UUID?) {
        safeItemDao.updateIcon(id, iconId)
    }

    override suspend fun toggleFavorite(id: UUID) {
        safeItemDao.toggleFavorite(id)
    }

    override suspend fun getSafeItem(id: UUID): SafeItem =
        (safeItemDao.findById(id) ?: throw OSStorageError(OSStorageError.Code.ITEM_NOT_FOUND)).toSafeItem()

    override fun getSafeItemWithIdentifier(ids: Collection<UUID>, order: ItemOrder): Flow<List<SafeItemWithIdentifier>> =
        safeItemRawDao.getSafeItemsWithIdentifierFlow(SafeItemRawDao.findByIdWithIdentifierQuery(ids, order))

    override suspend fun findByParentId(parentId: UUID, order: ItemOrder): List<SafeItem> =
        safeItemRawDao.getItems(SafeItemRawDao.findByParentIdQuery(parentId, order)).map(RoomSafeItem::toSafeItem)

    override suspend fun findByDeletedParentId(deletedParentId: UUID?, order: ItemOrder): List<SafeItem> =
        safeItemRawDao.getItems(SafeItemRawDao.findByDeletedParentIdQuery(deletedParentId, order))
            .map(RoomSafeItem::toSafeItem)

    override suspend fun getSiblingOriginalChildren(parentId: UUID, order: ItemOrder): List<SafeItem> =
        safeItemRawDao.getItems(
            SafeItemRawDao.findDeletedByParentIdNotEqualDeletedParentIdQuery(parentId, order),
        ).map(RoomSafeItem::toSafeItem)

    override suspend fun updateSiblingOriginalChildrenParentId(parentId: UUID, newParentId: UUID?): Unit =
        safeItemDao.updateParentIdOfDeletedByParentIdNotEqualDeletedParentId(parentId, newParentId)

    override fun countSafeItemByParentIdFlow(parentId: UUID?): Flow<Int> {
        return safeItemDao.countSafeItemByParentIdNotDeletedFlow(parentId)
            .distinctUntilChanged()
    }

    override suspend fun countSafeItemByParentId(parentId: UUID?): Int {
        return safeItemDao.countSafeItemByParentIdNotDeleted(parentId)
    }

    override fun countSafeItemByParentIdDeletedFlow(parentId: UUID?): Flow<Int> {
        return safeItemDao.countSafeItemByParentIdDeletedFlow(parentId)
            .distinctUntilChanged()
    }

    override suspend fun countSafeItemByParentIdDeleted(parentId: UUID?): Int {
        return safeItemDao.countSafeItemByParentIdDeleted(parentId)
    }

    override fun getPagerItemByParentId(config: PagingConfig, parentId: UUID?, order: ItemOrder): Flow<PagingData<SafeItem>> {
        return Pager(config = config) {
            safeItemRawDao.getPagedItems(SafeItemRawDao.findByParentIdQuery(parentId, order))
        }.flow.mapPagingValues(RoomSafeItem::toSafeItem)
    }

    override fun getPagerItemByParentIdWithIdentifier(
        config: PagingConfig,
        parentId: UUID?,
        order: ItemOrder,
    ): Flow<PagingData<SafeItemWithIdentifier>> {
        return Pager(config = config) {
            safeItemRawDao.getPagedSafeItemsWithIdentifier(
                SafeItemRawDao.findByParentIdWithIdentifierQuery(parentId, order),
            )
        }.flow
    }

    override fun getPagerItemByParentIdDeleted(config: PagingConfig, parentId: UUID?, order: ItemOrder): Flow<PagingData<SafeItem>> {
        return Pager(config = config) {
            safeItemRawDao.getPagedItems(SafeItemRawDao.findByDeletedParentIdQuery(parentId, order))
        }.flow.mapPagingValues(RoomSafeItem::toSafeItem)
    }

    override fun getPagerItemByParentIdDeletedWithIdentifier(
        config: PagingConfig,
        deletedParentId: UUID?,
        order: ItemOrder,
    ): Flow<PagingData<SafeItemWithIdentifier>> {
        return Pager(config = config) {
            safeItemRawDao.getPagedSafeItemsWithIdentifier(
                SafeItemRawDao.findByDeletedParentIdWithIdentifierQuery(deletedParentId, order),
            )
        }.flow
    }

    override fun getPagerItemFavorite(config: PagingConfig, order: ItemOrder): Flow<PagingData<SafeItem>> {
        return Pager(config = config) {
            safeItemRawDao.getPagedItems(SafeItemRawDao.findFavoriteQuery(order))
        }.flow.mapPagingValues(RoomSafeItem::toSafeItem)
    }

    override fun getPagerItemFavoriteWithIdentifier(config: PagingConfig, order: ItemOrder): Flow<PagingData<SafeItemWithIdentifier>> {
        return Pager(config = config) {
            safeItemRawDao.getPagedSafeItemsWithIdentifier(
                SafeItemRawDao.findFavoriteWithIdentifierQuery(order),
            )
        }.flow
    }

    override fun findLastFavorite(limit: Int, order: ItemOrder): Flow<List<SafeItem>> {
        return safeItemRawDao.getItemsFlow(
            SafeItemRawDao.findLastFavoriteQuery(
                limit = limit,
                order = order,
            ),
        )
            .distinctUntilChanged()
            .mapValues(RoomSafeItem::toSafeItem)
    }

    override fun countAllFavoriteFlow(): Flow<Int> {
        return safeItemDao.countAllFavoriteFlow()
            .distinctUntilChanged()
    }

    override suspend fun countAllFavorite(): Int {
        return safeItemDao.countAllFavorite()
    }

    override fun countAllDeletedWithNonDeletedParent(): Flow<Int> {
        return safeItemDao.countAllDeletedWithNonDeletedParent()
            .distinctUntilChanged()
    }

    override fun getSafeItemFlow(id: UUID): Flow<SafeItem?> {
        return safeItemDao.findByIdAsFlow(id).map { it?.toSafeItem() }
            .distinctUntilChanged()
    }

    override suspend fun removeItem(id: UUID) {
        safeItemDao.removeById(id)
    }

    override suspend fun removeItems(ids: List<UUID>) {
        safeItemDao.removeByIds(ids)
    }

    override suspend fun setDeletedAndRemoveFromFavorite(id: UUID?, deletedAt: Instant) {
        safeItemDao.setDeletedAndRemoveFromFavorite(id, deletedAt)
    }

    override suspend fun restoreItemToParentWithDescendants(id: UUID?) {
        safeItemDao.unsetDeletedAtAndDeletedParentIdForItemAndDescendants(id)
    }

    override suspend fun updateParentToNonDeletedAncestor(id: UUID) {
        safeItemDao.updateParentIdToFirstNonDeletedAncestor(id)
    }

    override suspend fun updateParentIds(oldParentId: UUID, newParentId: UUID?, newDeletedParentId: UUID?) {
        safeItemDao.updateParentIdAndDeletedParentId(oldParentId, newParentId, newDeletedParentId)
    }

    override suspend fun getHighestPosition(parentId: UUID?): Double? {
        return safeItemDao.getHighestPosition(parentId)
    }

    override suspend fun getHighestDeletedPosition(deletedParentId: UUID?): Double? {
        return safeItemDao.getHighestDeletedPosition(deletedParentId)
    }

    override suspend fun getNextSiblingPosition(id: UUID): Double? {
        return safeItemDao.getNextSiblingPosition(id)
    }

    override suspend fun updateSafeItem(safeItem: SafeItem, indexWordEntries: List<IndexWordEntry>?) {
        runSQL {
            transactionProvider.runAsTransaction {
                safeItemDao.update(RoomUpdateSafeItem.fromSafeItem(safeItem))
                indexWordEntries?.let { indexWordEntryDao.insert(it.map(RoomIndexWordEntry::fromIndexWordEntry)) }
            }
        }
    }

    override suspend fun findByIdWithChildren(id: UUID): List<SafeItem> {
        return safeItemDao.findByIdWithDescendants(id).map(RoomSafeItem::toSafeItem)
    }

    override suspend fun findDeletedByIdWithDeletedDescendants(id: UUID): List<SafeItem> {
        return safeItemDao.findDeletedByIdWithDeletedDescendants(id).map(RoomSafeItem::toSafeItem)
    }

    override suspend fun findByIdWithAncestors(id: UUID): List<SafeItem> {
        return safeItemDao.findByIdWithAncestors(id).map(RoomSafeItem::toSafeItem)
    }

    override suspend fun findByIdWithDeletedAncestors(id: UUID): List<SafeItem> {
        return safeItemDao.findByIdWithDeletedAncestors(id).map(RoomSafeItem::toSafeItem)
    }

    override suspend fun getSafeItemName(id: UUID): ByteArray? = safeItemDao.getSafeItemName(id)

    override suspend fun removeOldItems(threshold: Instant): Unit = safeItemDao.removeOldItems(threshold)

    override suspend fun getAllSafeItems(): List<SafeItem> {
        return safeItemDao.getAllSafeItems().map { it.toSafeItem() }
    }

    override fun getAllSafeItemsWithIdentifier(
        config: PagingConfig,
        idsToExclude: List<UUID>,
        order: ItemOrder,
    ): Flow<PagingData<SafeItemWithIdentifier>> {
        return Pager(config = config) {
            safeItemRawDao.getPagedSafeItemsWithIdentifier(
                SafeItemRawDao.getAllSafeItemsWithIdentifierQuery(idsToExclude, order),
            )
        }.flow
    }

    override fun getSafeItemsCountFlow(): Flow<Int> = safeItemDao.getSafeItemsCountFlow()
        .distinctUntilChanged()

    override suspend fun getSafeItemsCount(): Int = safeItemDao.getSafeItemsCount()

    override fun getSafeItemsWithIdentifierCount(): Flow<Int> = safeItemDao.getSafeItemsWithIdentifierCount()
        .distinctUntilChanged()

    override suspend fun getAllSafeItemIds(): List<UUID> {
        return safeItemDao.getAllSafeItemIds()
    }

    override suspend fun updateSafeItemParentId(itemId: UUID, parentId: UUID?) {
        safeItemDao.updateSafeItemParentId(itemId, parentId)
    }

    override suspend fun updateConsultedAt(itemId: UUID, consultedAt: Instant) {
        safeItemDao.updateConsultedAt(itemId, consultedAt)
    }

    override fun getLastConsultedNotDeletedSafeItem(limit: Int): Flow<List<SafeItem>> {
        return safeItemDao.getSafeItemsOrderByConsultedAtNotDeleted(limit).map {
            it.map(RoomSafeItem::toSafeItem)
        }
    }

    override fun getAllDeletedItemsCount(): Flow<Int> = safeItemDao.getAllDeletedItemsCount()

    override suspend fun setAlphaIndices(indices: List<Pair<UUID, Double>>) {
        safeItemDao.updateAlphaIndices(indices.map { (id, index) -> ItemIdWithAlphaIndex(id, index) })
    }

    override suspend fun getItemNameWithIndexAt(index: Int): ItemNameWithIndex? {
        return safeItemDao.getItemNameWithIndexAt(index)
    }

    override suspend fun getAlphaIndexRange(): Pair<Double, Double> {
        return safeItemDao.getAlphaIndexRange().asPair()
    }

    override suspend fun getAllSafeItemIdName(): List<SafeItemIdName> {
        return safeItemDao.getAllSafeItemsIdName()
    }
}
