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
 * Created by Lunabee Studio / Date - 12/14/2023 - for the oneSafe6 SDK.
 * Last modified 12/14/23, 1:26 PM
 */

package studio.lunabee.onesafe.storage.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemWithIdentifier
import studio.lunabee.onesafe.storage.DaoUtils.IS_DELETED
import studio.lunabee.onesafe.storage.DaoUtils.IS_FAVORITE
import studio.lunabee.onesafe.storage.DaoUtils.IS_NOT_DELETED
import studio.lunabee.onesafe.storage.DaoUtils.ITEM_ORDER_BY_CONSULTED_AT
import studio.lunabee.onesafe.storage.DaoUtils.ITEM_ORDER_BY_CREATED_AT
import studio.lunabee.onesafe.storage.DaoUtils.ITEM_ORDER_BY_DELETED_AT
import studio.lunabee.onesafe.storage.DaoUtils.ITEM_ORDER_BY_INDEX_ALPHA
import studio.lunabee.onesafe.storage.DaoUtils.ITEM_ORDER_BY_POSITION
import studio.lunabee.onesafe.storage.DaoUtils.ITEM_ORDER_BY_UPDATED_AT
import studio.lunabee.onesafe.storage.model.RoomSafeItem
import studio.lunabee.onesafe.storage.model.RoomSafeItemField
import studio.lunabee.onesafe.toByteArray
import java.util.UUID

/*
 * ⚠️ Every RawQuery must be test because these are not validated at compile time
 */
@Dao
interface SafeItemRawDao {

    companion object {
        internal fun findByParentIdQuery(parentId: UUID?, order: ItemOrder, safeId: SafeId): SimpleSQLiteQuery =
            SimpleSQLiteQuery(
                query = "SELECT * FROM SafeItem WHERE parent_id IS ? AND $IS_NOT_DELETED AND safe_id IS ? ${order.orderBy}",
                bindArgs = arrayOf(
                    parentId?.toByteArray(),
                    safeId.toByteArray(),
                ),
            )

        internal fun findDeletedByParentIdNotEqualDeletedParentIdQuery(parentId: UUID, order: ItemOrder): SimpleSQLiteQuery =
            SimpleSQLiteQuery(
                query = """
                            SELECT * FROM SafeItem 
                            WHERE parent_id = ? AND deleted_parent_id IS NOT ? AND $IS_DELETED
                            ${order.orderBy}
                        """,
                bindArgs = arrayOf(parentId.toByteArray(), parentId.toByteArray()),
            )

        internal fun getAllSafeItemsWithIdentifierQuery(idsToExclude: List<UUID>, order: ItemOrder, safeId: SafeId): SimpleSQLiteQuery {
            val placeHolder = idsToExclude.joinToString(",") { "?" }
            val args = idsToExclude.map { it.toByteArray() }.toTypedArray() + safeId.toByteArray()
            return SimpleSQLiteQuery(
                query = """
                            SELECT
                                SafeItem.id as id,
                                SafeItem.enc_name as encName,
                                SafeItem.icon_id as iconId,
                                SafeItem.enc_color as encColor,
                                SafeItem.deleted_at as deletedAt,
                                SafeItemField.enc_value as encIdentifier,
                                SafeItemField.enc_kind as encIdentifierKind,
                                SafeItemField.enc_secure_display_mask as encSecuredDisplayMask,
                                SafeItem.position as position,
                                SafeItem.updated_at as updatedAt
                            FROM SafeItem
                            LEFT JOIN SafeItemField ON SafeItemField.item_id = SafeItem.id AND SafeItemField.is_item_identifier = 1 
                            WHERE SafeItem.id NOT IN ($placeHolder) AND encIdentifier IS NOT NULL AND safe_id IS ?
                            ${order.orderBy}
                        """,
                bindArgs = args,
            )
        }

        internal fun findByParentIdWithIdentifierQuery(parentId: UUID?, order: ItemOrder, safeId: SafeId): SimpleSQLiteQuery =
            internalWithIdentifierQuery(
                "SafeItem.parent_id IS ? AND $IS_NOT_DELETED AND safe_id IS ?",
                order,
                arrayOf(parentId?.toByteArray(), safeId.toByteArray()),
            )

        internal fun findByDeletedParentIdWithIdentifierQuery(deletedParentId: UUID?, order: ItemOrder, safeId: SafeId): SimpleSQLiteQuery =
            internalWithIdentifierQuery(
                "SafeItem.deleted_parent_id IS ? AND $IS_DELETED AND safe_id IS ?",
                order,
                arrayOf(deletedParentId?.toByteArray(), safeId.toByteArray()),
            )

        fun findFavoriteWithIdentifierQuery(order: ItemOrder, safeId: SafeId): SupportSQLiteQuery =
            internalWithIdentifierQuery(
                "$IS_FAVORITE AND safe_id IS ?",
                order,
                arrayOf(safeId.toByteArray()),
            )

        private fun internalWithIdentifierQuery(
            whereClause: String,
            order: ItemOrder,
            bindArgs: Array<out Any?>?,
        ): SimpleSQLiteQuery {
            return SimpleSQLiteQuery(
                query = """
                            SELECT
                                SafeItem.id as id,
                                SafeItem.enc_name as encName,
                                SafeItem.icon_id as iconId,
                                SafeItem.enc_color as encColor,
                                SafeItem.deleted_at as deletedAt,
                                SafeItemField.enc_value as encIdentifier,
                                SafeItemField.enc_kind as encIdentifierKind,
                                SafeItemField.enc_secure_display_mask as encSecuredDisplayMask,
                                SafeItem.position as position,
                                SafeItem.updated_at as updatedAt
                            FROM SafeItem
                            LEFT JOIN SafeItemField ON SafeItemField.item_id = SafeItem.id AND SafeItemField.is_item_identifier = 1 
                            WHERE $whereClause
                            ${order.orderBy}
                        """,
                bindArgs = bindArgs,
            )
        }

        fun findByDeletedParentIdQuery(deletedParentId: UUID?, order: ItemOrder, safeId: SafeId): SupportSQLiteQuery {
            return SimpleSQLiteQuery(
                query = "SELECT * FROM SafeItem WHERE deleted_parent_id IS ? AND $IS_DELETED AND safe_id IS ? ${order.orderBy}",
                bindArgs = arrayOf(deletedParentId?.toByteArray(), safeId.toByteArray()),
            )
        }

        fun findFavoriteQuery(order: ItemOrder, safeId: SafeId): SupportSQLiteQuery {
            return SimpleSQLiteQuery(
                query = "SELECT * FROM SafeItem WHERE $IS_FAVORITE AND safe_id IS ? ${order.orderBy}",
                bindArgs = arrayOf(safeId.toByteArray()),
            )
        }

        fun findLastFavoriteQuery(limit: Int, order: ItemOrder, safeId: SafeId): SupportSQLiteQuery {
            return SimpleSQLiteQuery(
                query = "SELECT * FROM SafeItem WHERE $IS_FAVORITE AND safe_id IS ? ${order.orderBy} LIMIT ?",
                bindArgs = arrayOf(safeId.toByteArray(), limit),
            )
        }

        fun findByIdWithIdentifierQuery(ids: Collection<UUID>, order: ItemOrder): SupportSQLiteQuery {
            val placeHolder = ids.joinToString(",") { "?" }
            val idsArray = ids.map { it.toByteArray() }.toTypedArray()
            return SimpleSQLiteQuery(
                query = """
                    SELECT DISTINCT
                        SafeItem.id as id,
                        SafeItem.enc_name as encName,
                        SafeItem.icon_id as iconId,
                        SafeItem.enc_color as encColor,
                        SafeItem.deleted_at as deletedAt,
                        identifier.enc_value as encIdentifier,
                        identifier.enc_secure_display_mask as encSecuredDisplayMask,
                        identifier.enc_kind as encIdentifierKind,
                        SafeItem.position as position,
                        SafeItem.updated_at as updatedAt
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
                    WHERE SafeItem.id IN ($placeHolder)
                    ${order.orderBy}
                """,
                bindArgs = idsArray,
            )
        }
    }

    @RawQuery
    suspend fun getItems(query: SupportSQLiteQuery): List<RoomSafeItem>

    @RawQuery([RoomSafeItem::class, RoomSafeItemField::class])
    fun getSafeItemsWithIdentifierFlow(query: SupportSQLiteQuery): Flow<List<SafeItemWithIdentifier>>

    @RawQuery([RoomSafeItem::class, RoomSafeItemField::class])
    fun getPagedSafeItemsWithIdentifier(query: SupportSQLiteQuery): PagingSource<Int, SafeItemWithIdentifier>

    @RawQuery([RoomSafeItem::class])
    fun getPagedItems(query: SupportSQLiteQuery): PagingSource<Int, RoomSafeItem>

    @RawQuery([RoomSafeItem::class])
    fun getItemsFlow(query: SupportSQLiteQuery): Flow<List<RoomSafeItem>>
}

private val ItemOrder.orderBy: String
    get() = when (this) {
        ItemOrder.Position -> ITEM_ORDER_BY_POSITION
        ItemOrder.Alphabetic -> ITEM_ORDER_BY_INDEX_ALPHA
        ItemOrder.UpdatedAt -> ITEM_ORDER_BY_UPDATED_AT
        ItemOrder.DeletedAt -> ITEM_ORDER_BY_DELETED_AT
        ItemOrder.ConsultedAt -> ITEM_ORDER_BY_CONSULTED_AT
        ItemOrder.CreatedAt -> ITEM_ORDER_BY_CREATED_AT
    }
