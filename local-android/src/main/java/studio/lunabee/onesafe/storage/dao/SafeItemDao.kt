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

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemWithIdentifier
import studio.lunabee.onesafe.storage.DaoUtils.IS_DELETED
import studio.lunabee.onesafe.storage.DaoUtils.IS_FAVORITE
import studio.lunabee.onesafe.storage.DaoUtils.IS_NOT_DELETED
import studio.lunabee.onesafe.storage.DaoUtils.ORDER_BY_DELETED_AT
import studio.lunabee.onesafe.storage.DaoUtils.ORDER_BY_POSITION
import studio.lunabee.onesafe.storage.DaoUtils.ORDER_BY_SAFE_ITEM_POSITION
import studio.lunabee.onesafe.storage.DaoUtils.ORDER_BY_UPDATED_AT
import studio.lunabee.onesafe.storage.model.RoomSafeItem
import java.time.Instant
import java.util.UUID

@Dao
interface SafeItemDao {

    @Insert
    suspend fun insert(safeItem: RoomSafeItem)

    @Insert
    suspend fun insert(safeItems: List<RoomSafeItem>)

    @Update
    suspend fun update(vararg safeItem: RoomSafeItem)

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
                FROM SafeItem JOIN ItemWithChildren ON SafeItem.parent_id=ItemWithChildren.id
                WHERE SafeItem.deleted_at IS NULL
        )
    UPDATE SafeItem 
    SET 
        deleted_at = :deletedAt,
        is_favorite = 0,
        deleted_parent_id = CASE
            WHEN id = :itemId THEN NULL
            ELSE parent_id
        END
    WHERE SafeItem.id IN ItemWithChildren
    """,
    )
    suspend fun setDeletedAndRemoveFromFavorite(itemId: UUID, deletedAt: Instant = Instant.now())

    @Query("SELECT * FROM SafeItem WHERE id = :id")
    suspend fun findById(id: UUID): RoomSafeItem?

    @Query(
        """
        SELECT DISTINCT
            SafeItem.id as id,
            SafeItem.enc_name as encName,
            SafeItem.icon_id as iconId,
            SafeItem.enc_color as encColor,
            SafeItem.deleted_at as deletedAt,
            identifier.enc_value as encIdentifier,
            identifier.enc_kind as encIdentifierKind,
            identifier.enc_secure_display_mask as encSecuredDisplayMask
        FROM SafeItem
        LEFT JOIN (
            SELECT 
                item_id, 
                enc_value ,
                enc_kind,
                enc_secure_display_mask
            FROM SafeItemField  
            WHERE is_item_identifier = 1 AND enc_value IS NOT NULL GROUP BY item_id
        ) identifier on identifier.item_id  = SafeItem.id
        WHERE SafeItem.id IN (:ids)
       """,
    )
    fun findByIdWithIdentifier(ids: List<UUID>): Flow<List<SafeItemWithIdentifier>>

    @Query("SELECT * FROM SafeItem WHERE id = :id")
    fun findByIdAsFlow(id: UUID): Flow<RoomSafeItem?>

    @Query("SELECT * FROM SafeItem WHERE parent_id = :parentId AND $IS_NOT_DELETED $ORDER_BY_POSITION")
    suspend fun findByParentId(parentId: UUID): List<RoomSafeItem>

    @Query(
        "SELECT * FROM SafeItem WHERE parent_id = :parentId AND deleted_parent_id IS NOT :parentId AND $IS_DELETED $ORDER_BY_POSITION",
    )
    suspend fun findDeletedByParentIdNotEqualDeletedParentId(parentId: UUID): List<RoomSafeItem>

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

    @Query("SELECT * FROM SafeItem WHERE deleted_parent_id IS :deletedParentId AND $IS_DELETED $ORDER_BY_DELETED_AT")
    suspend fun findByDeletedParentId(deletedParentId: UUID?): List<RoomSafeItem>

    @Query("SELECT * FROM SafeItem WHERE parent_id IS :parentId AND $IS_NOT_DELETED $ORDER_BY_POSITION")
    fun findByParentIdAsPagingSource(parentId: UUID?): PagingSource<Int, RoomSafeItem>

    @Query("SELECT * FROM SafeItem WHERE deleted_parent_id IS :deletedParentId AND $IS_DELETED $ORDER_BY_DELETED_AT")
    fun findByDeletedParentIdAsPagingSource(deletedParentId: UUID?): PagingSource<Int, RoomSafeItem>

    @Query(
        """
        SELECT * 
        FROM SafeItem 
        WHERE $IS_FAVORITE $ORDER_BY_UPDATED_AT
    """,
    )
    fun findFavoriteAsPagingSource(): PagingSource<Int, RoomSafeItem>

    @Query("SELECT MAX(position) FROM SafeItem WHERE parent_id IS :parentId")
    suspend fun getHighestPosition(parentId: UUID?): Double?

    @Query("SELECT MAX(position) FROM SafeItem WHERE deleted_parent_id IS :deletedParentId")
    suspend fun getHighestDeletedPosition(deletedParentId: UUID?): Double?

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

    @Query("SELECT * FROM SafeItem WHERE $IS_FAVORITE $ORDER_BY_UPDATED_AT LIMIT :limit")
    fun findLastFavorite(limit: Int): Flow<List<RoomSafeItem>>

    /**
     * Return last [limit] items without parent or with a non-deleted parent
     */
    @Query(
        """
            SELECT *
            FROM SafeItem
            WHERE $IS_DELETED AND deleted_parent_id IS NULL
            $ORDER_BY_DELETED_AT
            LIMIT :limit
        """,
    )
    fun findLastDeletedWithNonDeletedParent(limit: Int): Flow<List<RoomSafeItem>>

    @Query(
        """
            SELECT COUNT(*)
            FROM SafeItem
            WHERE $IS_DELETED AND deleted_parent_id IS NULL
        """,
    )
    fun countAllDeletedWithNonDeletedParent(): Flow<Int>

    @Query("SELECT COUNT(*) FROM SafeItem WHERE parent_id IS :parentId AND $IS_NOT_DELETED")
    fun countSafeItemByParentIdNotDeletedFlow(parentId: UUID?): Flow<Int>

    @Query("SELECT COUNT(*) FROM SafeItem WHERE parent_id IS :parentId AND $IS_NOT_DELETED")
    suspend fun countSafeItemByParentIdNotDeleted(parentId: UUID?): Int

    @Query("SELECT COUNT(*) FROM SafeItem WHERE deleted_parent_id IS :parentId AND $IS_DELETED")
    fun countSafeItemByParentIdDeletedFlow(parentId: UUID?): Flow<Int>

    @Query("SELECT COUNT(*) FROM SafeItem WHERE deleted_parent_id IS :parentId AND $IS_DELETED")
    suspend fun countSafeItemByParentIdDeleted(parentId: UUID?): Int

    @Query("SELECT COUNT(*) FROM SafeItem WHERE $IS_FAVORITE")
    fun countAllFavoriteFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM SafeItem WHERE $IS_FAVORITE")
    suspend fun countAllFavorite(): Int

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
        )
    UPDATE SafeItem
    SET deleted_at = NULL, deleted_parent_id = NULL
    WHERE SafeItem.id
    IN ItemWithDescendants
    """,
    )
    suspend fun unsetDeletedAtAndDeletedParentIdForItemAndDescendants(itemId: UUID?)

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

    @Query("SELECT * FROM SafeItem LIMIT :limit")
    fun getAllSafeItems(limit: Int): Flow<List<RoomSafeItem>>

    @Query("SELECT COUNT(*) FROM SafeItem")
    fun getSafeItemsCount(): Flow<Int>

    @Query(
        """
       SELECT COUNT(*)
       FROM SafeItem
       LEFT JOIN SafeItemField ON SafeItemField.item_id = SafeItem.id AND SafeItemField.is_item_identifier = 1 
       WHERE SafeItemField.enc_value IS NOT NULL
       """,
    )
    fun getSafeItemsWithIdentifierCount(): Flow<Int>

    @Query("SELECT SafeItem.id FROM SafeItem")
    suspend fun getAllSafeItemIds(): List<UUID>

    @Query("SELECT * FROM SafeItem")
    suspend fun getAllSafeItems(): List<RoomSafeItem>

    @Query(
        """
        SELECT
            SafeItem.id as id,
            SafeItem.enc_name as encName,
            SafeItem.icon_id as iconId,
            SafeItem.enc_color as encColor,
            SafeItem.deleted_at as deletedAt,
            SafeItemField.enc_value as encIdentifier,
            SafeItemField.enc_kind as encIdentifierKind,
            SafeItemField.enc_secure_display_mask as encSecuredDisplayMask
        FROM SafeItem
        LEFT JOIN SafeItemField ON SafeItemField.item_id = SafeItem.id AND SafeItemField.is_item_identifier = 1 
        WHERE SafeItem.id NOT IN (:idsToExclude) AND encIdentifier IS NOT NULL
        $ORDER_BY_SAFE_ITEM_POSITION
       """,
    )
    fun getAllSafeItemsWithIdentifierAsPagingSource(idsToExclude: List<UUID>): PagingSource<Int, SafeItemWithIdentifier>

    @Query(
        "UPDATE SafeItem SET parent_id = :parentId WHERE id = :itemId",
    )
    suspend fun updateSafeItemParentId(itemId: UUID, parentId: UUID?)

    @Query(
        "UPDATE SafeItem SET consulted_at = :consultedAt WHERE id = :itemId",
    )
    suspend fun updateConsultedAt(itemId: UUID, consultedAt: Instant)

    @Query("SELECT * FROM SafeItem WHERE deleted_at IS NULL AND consulted_at IS NOT NULL ORDER BY consulted_at DESC LIMIT :limit")
    fun getSafeItemsOrderByConsultedAtNotDeleted(limit: Int): Flow<List<RoomSafeItem>>

    @Query(
        """
            SELECT COUNT(*)
            FROM SafeItem
            WHERE $IS_DELETED
        """,
    )
    fun getAllDeletedItemsCount(): Flow<Int>
}
