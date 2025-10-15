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
import androidx.room.Index
import androidx.room.PrimaryKey
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "SafeItemField",
    foreignKeys = [
        ForeignKey(
            entity = RoomSafeItem::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("item_id"),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("item_id")],
)
data class RoomSafeItemField(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: UUID,
    @ColumnInfo(name = "enc_name", typeAffinity = ColumnInfo.BLOB) val encName: ByteArray?,
    @ColumnInfo(name = "position") val position: Double,
    @ColumnInfo(name = "item_id") val itemId: UUID,
    @ColumnInfo(name = "enc_placeholder", typeAffinity = ColumnInfo.BLOB) val encPlaceholder: ByteArray?,
    @ColumnInfo(name = "enc_value", typeAffinity = ColumnInfo.BLOB) val encValue: ByteArray?,
    @ColumnInfo(name = "show_prediction") val showPrediction: Boolean,
    @ColumnInfo(name = "enc_kind", typeAffinity = ColumnInfo.BLOB) val encKind: ByteArray?,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
    @ColumnInfo(name = "is_item_identifier") val isItemIdentifier: Boolean,
    @ColumnInfo(name = "enc_formatting_mask", typeAffinity = ColumnInfo.BLOB) val encFormattingMask: ByteArray?,
    @ColumnInfo(name = "enc_secure_display_mask", typeAffinity = ColumnInfo.BLOB) val encSecureDisplayMask: ByteArray?,
    @ColumnInfo(name = "is_secured") val isSecured: Boolean,
    @ColumnInfo(name = "enc_thumbnail_file_name", defaultValue = "null") val encThumbnailFileName: ByteArray?,
) {
    fun toSafeItemField(): SafeItemField =
        SafeItemField(
            id = id,
            encName = encName,
            position = position,
            itemId = itemId,
            encPlaceholder = encPlaceholder,
            encValue = encValue,
            showPrediction = showPrediction,
            encKind = encKind,
            updatedAt = updatedAt,
            isItemIdentifier = isItemIdentifier,
            encSecureDisplayMask = encSecureDisplayMask,
            encFormattingMask = encFormattingMask,
            isSecured = isSecured,
            encThumbnailFileName = encThumbnailFileName,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomSafeItemField

        if (id != other.id) return false
        if (encName != null) {
            if (other.encName == null) return false
            if (!encName.contentEquals(other.encName)) return false
        } else if (other.encName != null) {
            return false
        }
        if (position != other.position) return false
        if (itemId != other.itemId) return false
        if (encPlaceholder != null) {
            if (other.encPlaceholder == null) return false
            if (!encPlaceholder.contentEquals(other.encPlaceholder)) return false
        } else if (other.encPlaceholder != null) {
            return false
        }
        if (encValue != null) {
            if (other.encValue == null) return false
            if (!encValue.contentEquals(other.encValue)) return false
        } else if (other.encValue != null) {
            return false
        }
        if (showPrediction != other.showPrediction) return false
        if (encKind != null) {
            if (other.encKind == null) return false
            if (!encKind.contentEquals(other.encKind)) return false
        } else if (other.encKind != null) {
            return false
        }
        if (updatedAt != other.updatedAt) return false
        if (isItemIdentifier != other.isItemIdentifier) return false
        if (encFormattingMask.contentEquals(other.encFormattingMask)) return false
        if (encSecureDisplayMask.contentEquals(other.encSecureDisplayMask)) return false
        if (isSecured != other.isSecured) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (encName?.contentHashCode() ?: 0)
        result = 31 * result + position.hashCode()
        result = 31 * result + itemId.hashCode()
        result = 31 * result + (encPlaceholder?.contentHashCode() ?: 0)
        result = 31 * result + (encValue?.contentHashCode() ?: 0)
        result = 31 * result + showPrediction.hashCode()
        result = 31 * result + (encKind?.contentHashCode() ?: 0)
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + isItemIdentifier.hashCode()
        result = 31 * result + encFormattingMask.hashCode()
        result = 31 * result + encSecureDisplayMask.hashCode()
        result = 31 * result + isSecured.hashCode()
        return result
    }

    companion object {
        fun fromSafeItemField(itemField: SafeItemField): RoomSafeItemField = RoomSafeItemField(
            id = itemField.id,
            encName = itemField.encName,
            position = itemField.position,
            itemId = itemField.itemId,
            encPlaceholder = itemField.encPlaceholder,
            encValue = itemField.encValue,
            showPrediction = itemField.showPrediction,
            encKind = itemField.encKind,
            updatedAt = itemField.updatedAt,
            isItemIdentifier = itemField.isItemIdentifier,
            encFormattingMask = itemField.encFormattingMask,
            encSecureDisplayMask = itemField.encSecureDisplayMask,
            isSecured = itemField.isSecured,
            encThumbnailFileName = itemField.encThumbnailFileName,
        )
    }
}
