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

package studio.lunabee.onesafe.storage.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.search.IndexWordEntry
import java.util.UUID

@Entity(
    tableName = "IndexWordEntry",
    foreignKeys = [
        ForeignKey(
            entity = RoomSafeItem::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("item_match"),
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = RoomSafeItemField::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("field_match"),
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = RoomSafe::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("safe_id"),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
class RoomIndexWordEntry(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "word") val encWord: ByteArray,
    @ColumnInfo(name = "item_match", index = true) val itemMatch: UUID,
    @ColumnInfo(name = "field_match", index = true) val fieldMatch: UUID?,
    @ColumnInfo(name = "safe_id", index = true) val safeId: SafeId, // TODO <multisafe> should we use multi-column index?
) {

    fun toIndexWordEntry(): IndexWordEntry =
        IndexWordEntry(
            encWord = encWord,
            itemMatch = itemMatch,
            fieldMatch = fieldMatch,
            safeId = safeId,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomIndexWordEntry

        if (id != other.id) return false
        if (!encWord.contentEquals(other.encWord)) return false
        if (itemMatch != other.itemMatch) return false
        if (fieldMatch != other.fieldMatch) return false
        if (safeId != other.safeId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + encWord.contentHashCode()
        result = 31 * result + itemMatch.hashCode()
        result = 31 * result + (fieldMatch?.hashCode() ?: 0)
        result = 31 * result + safeId.hashCode()
        return result
    }

    companion object {
        fun fromIndexWordEntry(indexWordEntry: IndexWordEntry): RoomIndexWordEntry = RoomIndexWordEntry(
            itemMatch = indexWordEntry.itemMatch,
            encWord = indexWordEntry.encWord,
            fieldMatch = indexWordEntry.fieldMatch,
            safeId = indexWordEntry.safeId,
        )
    }
}
