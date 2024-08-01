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
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.storage.DaoUtils.FIELD_ORDER_BY_POSITION
import studio.lunabee.onesafe.storage.model.RoomSafeItemField
import java.util.UUID

@Dao
interface SafeItemFieldDao {
    @Query("SELECT * FROM SafeItemField WHERE id = :fieldId")
    suspend fun getSafeItemField(fieldId: UUID): RoomSafeItemField

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg safeItem: RoomSafeItemField)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(safeItem: List<RoomSafeItemField>)

    @Query("SELECT * FROM SafeItemField WHERE item_id = :itemId $FIELD_ORDER_BY_POSITION")
    suspend fun getSafeItemFields(itemId: UUID): List<RoomSafeItemField>

    @Query("SELECT * FROM SafeItemField WHERE item_id = :itemId $FIELD_ORDER_BY_POSITION")
    fun getSafeItemFieldsAsFlow(itemId: UUID): Flow<List<RoomSafeItemField>>

    @Query("DELETE FROM SafeItemField WHERE item_id = :itemId")
    suspend fun deleteByItemId(itemId: UUID)

    @Query(
        """
        SELECT SafeItemField.id
        FROM SafeItemField
        LEFT JOIN SafeItem ON SafeItemField.item_id = SafeItem.id
        WHERE SafeItem.safe_id = :safeId
        """,
    )
    suspend fun getAllSafeItemFieldIds(safeId: SafeId): List<UUID>

    @Query(
        """
        SELECT *
        FROM SafeItemField
        LEFT JOIN SafeItem ON SafeItemField.item_id = SafeItem.id
        WHERE SafeItem.safe_id = :safeId
        """,
    )
    suspend fun getAllSafeItemFields(safeId: SafeId): List<RoomSafeItemField>

    @Query("SELECT * FROM SafeItemField WHERE item_id IN (:items)")
    suspend fun getAllSafeItemFieldsOfItems(items: List<UUID>): List<RoomSafeItemField>

    @Query("UPDATE SafeItemField SET enc_thumbnail_file_name  = :encThumbnailFileName WHERE id = :fieldId")
    suspend fun saveThumbnailFileName(fieldId: UUID, encThumbnailFileName: ByteArray?)
}
