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
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "SafeItem",
    foreignKeys = [
        ForeignKey(
            entity = RoomSafeItem::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("parent_id"),
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = RoomSafeItem::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("deleted_parent_id"),
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = RoomSafe::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("safe_id"),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class RoomSafeItem(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: UUID,
    @ColumnInfo(name = "enc_name") val encName: ByteArray?,
    @ColumnInfo(name = "parent_id", index = true) val parentId: UUID?,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean,
    @ColumnInfo(name = "created_at", index = true) val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
    @ColumnInfo(name = "position") val position: Double,
    @ColumnInfo(name = "icon_id") val iconId: UUID?,
    @ColumnInfo(name = "enc_color") val encColor: ByteArray?,
    @ColumnInfo(name = "deleted_at") val deletedAt: Instant?,
    @ColumnInfo(name = "deleted_parent_id", index = true) val deletedParentId: UUID?,
    @ColumnInfo(name = "consulted_at", index = true) val consultedAt: Instant?,
    @ColumnInfo(name = "index_alpha", index = true) val indexAlpha: Double,
    @ColumnInfo(name = "safe_id", index = true) val safeId: SafeId,
) {
    fun toSafeItem(): SafeItem =
        SafeItem(
            id = id,
            encName = encName,
            parentId = parentId,
            isFavorite = isFavorite,
            updatedAt = updatedAt,
            position = position,
            iconId = iconId,
            encColor = encColor,
            deletedAt = deletedAt,
            deletedParentId = deletedParentId,
            indexAlpha = indexAlpha,
            createdAt = createdAt,
            safeId = safeId,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomSafeItem

        if (id != other.id) return false
        if (encName != null) {
            if (other.encName == null) return false
            if (!encName.contentEquals(other.encName)) return false
        } else if (other.encName != null) return false
        if (parentId != other.parentId) return false
        if (isFavorite != other.isFavorite) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false
        if (position != other.position) return false
        if (iconId != other.iconId) return false
        if (encColor != null) {
            if (other.encColor == null) return false
            if (!encColor.contentEquals(other.encColor)) return false
        } else if (other.encColor != null) return false
        if (deletedAt != other.deletedAt) return false
        if (deletedParentId != other.deletedParentId) return false
        if (consultedAt != other.consultedAt) return false
        if (indexAlpha != other.indexAlpha) return false
        if (safeId != other.safeId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (encName?.contentHashCode() ?: 0)
        result = 31 * result + (parentId?.hashCode() ?: 0)
        result = 31 * result + isFavorite.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + position.hashCode()
        result = 31 * result + (iconId?.hashCode() ?: 0)
        result = 31 * result + (encColor?.contentHashCode() ?: 0)
        result = 31 * result + (deletedAt?.hashCode() ?: 0)
        result = 31 * result + (deletedParentId?.hashCode() ?: 0)
        result = 31 * result + (consultedAt?.hashCode() ?: 0)
        result = 31 * result + indexAlpha.hashCode()
        result = 31 * result + safeId.hashCode()
        return result
    }

    companion object {
        fun fromSafeItem(safeItem: SafeItem): RoomSafeItem {
            return RoomSafeItem(
                id = safeItem.id,
                encName = safeItem.encName,
                parentId = safeItem.parentId,
                isFavorite = safeItem.isFavorite,
                updatedAt = safeItem.updatedAt,
                position = safeItem.position,
                iconId = safeItem.iconId,
                encColor = safeItem.encColor,
                deletedAt = safeItem.deletedAt,
                deletedParentId = safeItem.deletedParentId,
                consultedAt = null,
                indexAlpha = safeItem.indexAlpha,
                createdAt = safeItem.createdAt,
                safeId = safeItem.safeId,
            )
        }
    }
}
