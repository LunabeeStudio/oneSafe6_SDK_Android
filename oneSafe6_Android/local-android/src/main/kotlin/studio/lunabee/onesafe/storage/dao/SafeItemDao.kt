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

package studio.lunabee.onesafe.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safeitem.ItemNameWithIndex
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemIdName
import studio.lunabee.onesafe.storage.DaoUtils.IS_DELETED
import studio.lunabee.onesafe.storage.DaoUtils.IS_FAVORITE
import studio.lunabee.onesafe.storage.DaoUtils.IS_NOT_DELETED
import studio.lunabee.onesafe.storage.DaoUtils.ITEM_ORDER_BY_INDEX_ALPHA
import studio.lunabee.onesafe.storage.DaoUtils.SAFE_ID_IS_PARAM
import studio.lunabee.onesafe.storage.model.ItemIdWithAlphaIndex
import studio.lunabee.onesafe.storage.model.RoomDoubleRange
import studio.lunabee.onesafe.storage.model.RoomSafeItem
import studio.lunabee.onesafe.storage.model.RoomUpdateSafeItem
import java.time.Instant
import java.util.UUID

@Dao
interface SafeItemDao {

    @Insert
    suspend fun insert(safeItem: RoomSafeItem)

    @Insert
    suspend fun insert(safeItems: List<RoomSafeItem>)

    @Update(RoomSafeItem::class)
    suspend fun update(safeItem: RoomUpdateSafeItem)

    @Query("UPDATE SafeItem SET is_favorite = NOT is_favorite WHERE id = :id")
    suspend fun toggleFavorite(id: UUID)

    @Query("UPDATE SafeItem SET icon_id = :iconId WHERE id = :id")
    suspend fun updateIcon(id: UUID, iconId: UUID?): Int

    @Query("DELETE FROM SafeItem WHERE id = :id")
    suspend fun removeById(id: UUID)

    @Query("DELETE FROM SafeItem WHERE id IN (:ids)")
    suspend fun removeByIds(ids: List<UUID>)

    /**
     * Set deletedAt and deletedParentId for an item and all its non deleted children + unset isFavorite
     */
    @Query(
        """
    WITH RECURSIVE
        ItemWithChildren(id) AS (
            VALUES(:itemId)
            UNION
                SELECT SafeItem.id
                FROM SafeItem JOIN ItemWithChildren ON SafeItem.parent_id IS ItemWithChildren.id
                WHERE SafeItem.deleted_at IS NULL AND $SAFE_ID_IS_PARAM
        )
    UPDATE SafeItem 
    SET 
        deleted_at = :deletedAt,
        is_favorite = 0,
        deleted_parent_id = CASE
            WHEN id IS :itemId THEN NULL
            ELSE parent_id
        END
    WHERE SafeItem.id IN ItemWithChildren
    """,
    )
    suspend fun setDeletedAndRemoveFromFavorite(itemId: UUID?, deletedAt: Instant, safeId: SafeId)

    @Query("SELECT * FROM SafeItem WHERE id = :id")
    suspend fun findById(id: UUID): RoomSafeItem?

    @Query("SELECT * FROM SafeItem WHERE id = :id")
    fun findByIdAsFlow(id: UUID): Flow<RoomSafeItem?>

    @Query(
        """
        UPDATE SafeItem 
        SET parent_id = :newParentId 
        WHERE 
            parent_id = :parentId AND
            deleted_parent_id IS NOT :parentId AND
            $IS_DELETED
    """,
    )
    suspend fun updateParentIdOfDeletedByParentIdNotEqualDeletedParentId(parentId: UUID, newParentId: UUID?)

    @Query("SELECT MAX(position) FROM SafeItem WHERE parent_id IS :parentId AND $SAFE_ID_IS_PARAM")
    suspend fun getHighestPosition(parentId: UUID?, safeId: SafeId): Double?

    @Query("SELECT MAX(position) FROM SafeItem WHERE deleted_parent_id IS :deletedParentId AND $SAFE_ID_IS_PARAM")
    suspend fun getHighestDeletedPosition(deletedParentId: UUID?, safeId: SafeId): Double?

    /**
     * Return the next closest superior position of sibling item, or null if there is no next sibling item
     */
    @Query(
        """
        WITH 
            CurrentItem AS (
                SELECT parent_id, position
                FROM SafeItem
                WHERE id = :itemId
            )
        SELECT MIN(position) 
        FROM SafeItem
        WHERE parent_id IS (SELECT parent_id FROM CurrentItem) AND position > (SELECT position FROM CurrentItem)
    """,
    )
    suspend fun getNextSiblingPosition(itemId: UUID): Double?

    @Query(
        "UPDATE SafeItem SET parent_id = :newParentId, deleted_parent_id = :newDeletedParentId WHERE parent_id = :oldParentId",
    )
    suspend fun updateParentIdAndDeletedParentId(oldParentId: UUID, newParentId: UUID?, newDeletedParentId: UUID?)

    @Query("SELECT COUNT(*) FROM SafeItem WHERE parent_id IS :parentId AND $IS_NOT_DELETED AND $SAFE_ID_IS_PARAM")
    fun countSafeItemByParentIdNotDeletedFlow(parentId: UUID?, safeId: SafeId): Flow<Int>

    @Query("SELECT COUNT(*) FROM SafeItem WHERE parent_id IS :parentId AND $IS_NOT_DELETED AND $SAFE_ID_IS_PARAM")
    suspend fun countSafeItemByParentIdNotDeleted(parentId: UUID?, safeId: SafeId): Int

    @Query("SELECT COUNT(*) FROM SafeItem WHERE deleted_parent_id IS :parentId AND $IS_DELETED AND $SAFE_ID_IS_PARAM")
    fun countSafeItemByParentIdDeletedFlow(parentId: UUID?, safeId: SafeId): Flow<Int>

    @Query("SELECT COUNT(*) FROM SafeItem WHERE deleted_parent_id IS :parentId AND $IS_DELETED AND $SAFE_ID_IS_PARAM")
    suspend fun countSafeItemByParentIdDeleted(parentId: UUID?, safeId: SafeId): Int

    @Query("SELECT COUNT(*) FROM SafeItem WHERE $IS_FAVORITE AND $SAFE_ID_IS_PARAM")
    fun countAllFavoriteFlow(safeId: SafeId): Flow<Int>

    @Query("SELECT COUNT(*) FROM SafeItem WHERE $IS_FAVORITE AND $SAFE_ID_IS_PARAM")
    suspend fun countAllFavorite(safeId: SafeId): Int

    @Query(
        """
    WITH RECURSIVE
        ItemWithDescendants(id, depth) AS (
            VALUES(:itemId, 0)
            UNION
                SELECT SafeItem.id, ItemWithDescendants.depth+1
                FROM SafeItem JOIN ItemWithDescendants ON SafeItem.parent_id=ItemWithDescendants.id WHERE $IS_NOT_DELETED
        )
    SELECT SafeItem.* FROM SafeItem JOIN ItemWithDescendants ON SafeItem.id=ItemWithDescendants.id ORDER BY ItemWithDescendants.depth
    """,
    )
    suspend fun findByIdWithDescendants(itemId: UUID): List<RoomSafeItem>

    @Query(
        """
    WITH RECURSIVE
        ItemWithChildren(id, depth) AS (
            VALUES(:itemId, 0)
            UNION
                SELECT SafeItem.id, ItemWithChildren.depth+1
                FROM SafeItem JOIN ItemWithChildren ON SafeItem.deleted_parent_id=ItemWithChildren.id
        )
    SELECT SafeItem.* FROM SafeItem JOIN ItemWithChildren ON SafeItem.id=ItemWithChildren.id ORDER BY ItemWithChildren.depth
    """,
    )
    suspend fun findDeletedByIdWithDeletedDescendants(itemId: UUID): List<RoomSafeItem>

    @Query(
        """
    WITH RECURSIVE
        ItemWithAncestors(id, parent_id, depth) AS (
            VALUES(:itemId, (
                SELECT parent_id FROM SafeItem WHERE id=:itemId
            ), 0)
            UNION
                SELECT SafeItem.id, SafeItem.parent_id, ItemWithAncestors.depth-1
                FROM SafeItem
                JOIN ItemWithAncestors ON SafeItem.id=ItemWithAncestors.parent_id
                WHERE $IS_NOT_DELETED
        )
    SELECT SafeItem.* FROM SafeItem JOIN ItemWithAncestors ON SafeItem.id=ItemWithAncestors.id ORDER BY ItemWithAncestors.depth
    """,
    )
    suspend fun findByIdWithAncestors(itemId: UUID): List<RoomSafeItem>

    @Query(
        """
    WITH RECURSIVE
        ItemWithAncestors(id, deleted_parent_id, depth) AS (
            VALUES(:itemId, (
                SELECT deleted_parent_id FROM SafeItem WHERE id=:itemId
            ), 0)
            UNION
                SELECT SafeItem.id, SafeItem.deleted_parent_id, ItemWithAncestors.depth-1
                FROM SafeItem
                JOIN ItemWithAncestors ON SafeItem.id=ItemWithAncestors.deleted_parent_id
                WHERE $IS_DELETED
        )
    SELECT SafeItem.* FROM SafeItem JOIN ItemWithAncestors ON SafeItem.id=ItemWithAncestors.id ORDER BY ItemWithAncestors.depth
    """,
    )
    suspend fun findByIdWithDeletedAncestors(itemId: UUID): List<RoomSafeItem>

    @Query("SELECT enc_name FROM SafeItem WHERE id = :itemId")
    suspend fun getSafeItemName(itemId: UUID): ByteArray?

    @Query(
        """
    WITH RECURSIVE
        ItemWithDescendants(id) AS (
            VALUES(:itemId)
            UNION
                SELECT SafeItem.id
                FROM SafeItem JOIN ItemWithDescendants ON SafeItem.deleted_parent_id IS ItemWithDescendants.id
                WHERE $SAFE_ID_IS_PARAM
        )
    UPDATE SafeItem
    SET deleted_at = NULL, deleted_parent_id = NULL
    WHERE SafeItem.id
    IN ItemWithDescendants
    """,
    )
    suspend fun unsetDeletedAtAndDeletedParentIdForItemAndDescendants(itemId: UUID?, safeId: SafeId)

    /**
     * First check if the item has a deleted parent. If not, nothing to update.
     * Else, find recursively the first ancestor which is not deleted.
     */
    @Query(
        """
    UPDATE SafeItem 
    SET parent_id = (
        WITH RECURSIVE ItemWithAncestors(parent_id, row_order) AS (
            SELECT parent_id, 0 FROM SafeItem WHERE id=:itemId
            UNION
                SELECT SafeItem.parent_id, row_order+1
                FROM SafeItem JOIN ItemWithAncestors ON SafeItem.id=ItemWithAncestors.parent_id
                WHERE SafeItem.deleted_at IS NOT NULL
        )
        SELECT ItemWithAncestors.parent_id FROM ItemWithAncestors ORDER BY row_order DESC LIMIT 1)
    WHERE id=:itemId AND (
        SELECT SafeItem.deleted_at FROM SafeItem WHERE id=(
            SELECT SafeItem.parent_id FROM SafeItem WHERE id=:itemId
            )
        ) IS NOT NULL
    """,
    )
    suspend fun updateParentIdToFirstNonDeletedAncestor(itemId: UUID): Int

    @Query("DELETE FROM SafeItem WHERE SafeItem.deleted_at < :threshold")
    suspend fun removeOldItems(threshold: Instant)

    @Query("SELECT COUNT(*) FROM SafeItem WHERE $SAFE_ID_IS_PARAM")
    fun getSafeItemsCountFlow(safeId: SafeId): Flow<Int>

    @Query("SELECT COUNT(*) FROM SafeItem WHERE $SAFE_ID_IS_PARAM")
    suspend fun getSafeItemsCount(safeId: SafeId): Int

    @Query(
        """
       SELECT COUNT(*)
       FROM SafeItem
       LEFT JOIN SafeItemField ON SafeItemField.item_id = SafeItem.id AND SafeItemField.is_item_identifier = 1 
       WHERE SafeItemField.enc_value IS NOT NULL AND $SAFE_ID_IS_PARAM
       """,
    )
    fun getSafeItemsWithIdentifierCount(safeId: SafeId): Flow<Int>

    @Query("SELECT SafeItem.id FROM SafeItem WHERE $SAFE_ID_IS_PARAM")
    suspend fun getAllSafeItemIds(safeId: SafeId): List<UUID>

    @Transaction
    @Query("SELECT * FROM SafeItem WHERE $SAFE_ID_IS_PARAM")
    suspend fun getAllSafeItems(safeId: SafeId): List<RoomSafeItem>

    @Query("SELECT id, enc_name as encName FROM SafeItem WHERE $SAFE_ID_IS_PARAM")
    suspend fun getAllSafeItemsIdName(safeId: SafeId): List<SafeItemIdName>

    @Query(
        "UPDATE SafeItem SET parent_id = :parentId WHERE id = :itemId",
    )
    suspend fun updateSafeItemParentId(itemId: UUID, parentId: UUID?)

    @Query(
        "UPDATE SafeItem SET consulted_at = :consultedAt WHERE id = :itemId",
    )
    suspend fun updateConsultedAt(itemId: UUID, consultedAt: Instant)

    @Query(
        """
            SELECT * FROM SafeItem
            WHERE deleted_at IS NULL
            AND consulted_at IS NOT NULL
            AND $SAFE_ID_IS_PARAM
            ORDER BY consulted_at DESC LIMIT :limit
        """,
    )
    fun getSafeItemsOrderByConsultedAtNotDeleted(limit: Int, safeId: SafeId): Flow<List<RoomSafeItem>>

    @Query(
        """
            SELECT COUNT(*)
            FROM SafeItem
            WHERE $IS_DELETED
            AND $SAFE_ID_IS_PARAM
        """,
    )
    fun getAllDeletedItemsCount(safeId: SafeId): Flow<Int>

    @Update(RoomSafeItem::class)
    suspend fun updateAlphaIndices(idWithIndex: List<ItemIdWithAlphaIndex>)

    @Query(
        "UPDATE SafeItem SET index_alpha = :index WHERE id = :itemId",
    )
    suspend fun setAlphaIndex(itemId: UUID, index: Double)

    @Query(
        """
            SELECT id, enc_name as encName, index_alpha as indexAlpha
            FROM SafeItem
             WHERE $SAFE_ID_IS_PARAM
            $ITEM_ORDER_BY_INDEX_ALPHA
            LIMIT 1 OFFSET :index
        """,
    )
    suspend fun getItemNameWithIndexAt(index: Int, safeId: SafeId): ItemNameWithIndex?

    @Query(
        "SELECT MIN(index_alpha) AS 'first', MAX(index_alpha) AS 'last' FROM SafeItem WHERE $SAFE_ID_IS_PARAM",
    )
    suspend fun getAlphaIndexRange(safeId: SafeId): RoomDoubleRange
}
