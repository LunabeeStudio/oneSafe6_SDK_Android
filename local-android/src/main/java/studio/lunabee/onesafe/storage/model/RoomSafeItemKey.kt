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
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import java.util.UUID

@Entity(
    tableName = "SafeItemKey",
    foreignKeys = [
        ForeignKey(
            entity = RoomSafeItem::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("id"),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class RoomSafeItemKey(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: UUID,
    @ColumnInfo(name = "enc_value") val encValue: ByteArray,
) {
    internal fun toSafeItemKey(): SafeItemKey {
        return SafeItemKey(
            id = id,
            encValue = encValue,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomSafeItemKey

        if (id != other.id) return false
        if (!encValue.contentEquals(other.encValue)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + encValue.contentHashCode()
        return result
    }

    companion object {
        fun fromSafeItemKey(safeItemKey: SafeItemKey): RoomSafeItemKey {
            return RoomSafeItemKey(
                id = safeItemKey.id,
                encValue = safeItemKey.encValue,
            )
        }
    }
}
