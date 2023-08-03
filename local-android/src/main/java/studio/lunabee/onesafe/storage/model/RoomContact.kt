/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 6/14/2023 - for the oneSafe6 SDK.
 * Last modified 6/14/23, 9:00 AM
 */

package studio.lunabee.onesafe.storage.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import studio.lunabee.onesafe.bubbles.domain.model.Contact
import studio.lunabee.onesafe.bubbles.domain.model.ContactSharedKey
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "Contact",
)
data class RoomContact(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: UUID,
    @ColumnInfo(name = "enc_name") val encName: ByteArray,
    @ColumnInfo(name = "enc_shared_key") val encSharedKey: ContactSharedKey?,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
    @ColumnInfo(name = "shared_conversation_id") val sharedConversationId: UUID,
    @ColumnInfo(name = "enc_is_using_deeplink") val encIsUsingDeeplink: ByteArray,
) {
    fun toContact(): Contact =
        Contact(
            id = id,
            encName = encName,
            encSharedKey = encSharedKey,
            updatedAt = updatedAt,
            sharedConversationId = sharedConversationId,
            encIsUsingDeeplink = encIsUsingDeeplink,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomContact

        if (id != other.id) return false
        if (!encName.contentEquals(other.encName)) return false
        if (!encSharedKey?.encKey.contentEquals(other.encSharedKey?.encKey)) return false
        if (updatedAt != other.updatedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + encName.contentHashCode()
        result = 31 * result + encSharedKey?.encKey.contentHashCode()
        result = 31 * result + updatedAt.hashCode()
        return result
    }

    companion object {
        fun fromBubblesContact(bubblesContact: Contact): RoomContact =
            RoomContact(
                id = bubblesContact.id,
                encName = bubblesContact.encName,
                encSharedKey = bubblesContact.encSharedKey,
                updatedAt = bubblesContact.updatedAt,
                sharedConversationId = bubblesContact.sharedConversationId,
                encIsUsingDeeplink = bubblesContact.encIsUsingDeeplink,
            )
    }
}
