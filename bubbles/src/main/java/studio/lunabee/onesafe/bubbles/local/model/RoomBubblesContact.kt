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
 * Created by Lunabee Studio / Date - 5/22/2023 - for the oneSafe6 SDK.
 * Last modified 5/22/23, 11:02 AM
 */

package studio.lunabee.onesafe.bubbles.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import studio.lunabee.onesafe.bubbles.domain.model.BubblesContact
import studio.lunabee.onesafe.bubbles.domain.model.EncBubblesContactInfo
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "BubblesContact",
)
data class RoomBubblesContact(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: UUID,
    @ColumnInfo(name = "enc_name") val encName: ByteArray,
    @ColumnInfo(name = "enc_key") val encKey: ByteArray,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
) {

    fun toEncBubblesContactInfo(): EncBubblesContactInfo =
        EncBubblesContactInfo(
            id = this.id,
            encName = this.encName,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomBubblesContact

        if (id != other.id) return false
        if (!encName.contentEquals(other.encName)) return false
        if (!encKey.contentEquals(other.encKey)) return false
        if (updatedAt != other.updatedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + encName.contentHashCode()
        result = 31 * result + encKey.contentHashCode()
        result = 31 * result + updatedAt.hashCode()
        return result
    }

    companion object {
        fun fromBubblesContact(bubblesContact: BubblesContact): RoomBubblesContact =
            RoomBubblesContact(
                id = bubblesContact.id,
                encName = bubblesContact.encName,
                encKey = bubblesContact.encKey,
                updatedAt = bubblesContact.updatedAt,
            )
    }
}
