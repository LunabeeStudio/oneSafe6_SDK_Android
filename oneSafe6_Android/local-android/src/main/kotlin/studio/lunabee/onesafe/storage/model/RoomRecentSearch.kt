/*
 * Copyright (c) 2024-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 10/3/2024 - for the oneSafe6 SDK.
 * Last modified 10/3/24, 5:21 PM
 */

package studio.lunabee.onesafe.storage.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import studio.lunabee.onesafe.domain.model.safe.SafeId

@Entity(
    tableName = "RecentSearch",
    foreignKeys = [
        ForeignKey(
            entity = RoomSafe::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("safe_id"),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    primaryKeys = [
        "hash_search",
        "safe_id",
    ],
)
data class RoomRecentSearch(
    @ColumnInfo(name = "hash_search")
    val hashSearch: ByteArray,
    @ColumnInfo(name = "enc_search")
    val encSearch: ByteArray,
    @ColumnInfo(name = "timestamp_ms")
    val timestampMs: Long,
    @ColumnInfo(name = "safe_id")
    val safeId: SafeId,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomRecentSearch

        if (!hashSearch.contentEquals(other.hashSearch)) return false
        if (!encSearch.contentEquals(other.encSearch)) return false
        if (timestampMs != other.timestampMs) return false
        if (safeId != other.safeId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hashSearch.contentHashCode()
        result = 31 * result + encSearch.contentHashCode()
        result = 31 * result + timestampMs.hashCode()
        result = 31 * result + safeId.hashCode()
        return result
    }
}
