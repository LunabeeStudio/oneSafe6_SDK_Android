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
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant
import studio.lunabee.bubbles.domain.model.contact.Contact
import studio.lunabee.bubbles.domain.model.contactkey.ContactSharedKey
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.domain.model.safe.SafeId
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "Contact",
    foreignKeys = [
        ForeignKey(
            entity = RoomSafe::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("safe_id"),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class RoomContact(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: UUID,
    @ColumnInfo(name = "enc_name") val encName: ByteArray,
    @ColumnInfo(name = "enc_shared_key") val encSharedKey: ByteArray?,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
    @ColumnInfo(name = "shared_conversation_id") val sharedConversationId: UUID,
    @ColumnInfo(name = "enc_sharing_mode") val encSharingMode: ByteArray,
    @ColumnInfo(name = "consulted_at", defaultValue = "null") val consultedAt: Instant?,
    @ColumnInfo(name = "safe_id", index = true) val safeId: SafeId,
    @ColumnInfo(name = "enc_reset_conversation_date", defaultValue = "null") val encResetConversationDate: ByteArray? = null,
) {
    fun toContact(): Contact =
        Contact(
            id = DoubleRatchetUUID(id),
            encName = encName,
            encSharedKey = encSharedKey?.let { ContactSharedKey(it) },
            updatedAt = updatedAt.toKotlinInstant(),
            sharedConversationId = DoubleRatchetUUID(sharedConversationId),
            encSharingMode = encSharingMode,
            consultedAt = consultedAt?.toKotlinInstant(),
            safeId = DoubleRatchetUUID(safeId.id),
            encResetConversationDate = encResetConversationDate,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomContact

        if (id != other.id) return false
        if (!encName.contentEquals(other.encName)) return false
        if (encSharedKey != other.encSharedKey) return false
        if (updatedAt != other.updatedAt) return false
        if (sharedConversationId != other.sharedConversationId) return false
        if (!encSharingMode.contentEquals(other.encSharingMode)) return false
        if (consultedAt != other.consultedAt) return false
        if (safeId != other.safeId) return false
        if (!encResetConversationDate.contentEquals(other.encResetConversationDate)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + encName.contentHashCode()
        result = 31 * result + (encSharedKey?.hashCode() ?: 0)
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + sharedConversationId.hashCode()
        result = 31 * result + encSharingMode.contentHashCode()
        result = 31 * result + (consultedAt?.hashCode() ?: 0)
        result = 31 * result + safeId.hashCode()
        result = 31 * result + encResetConversationDate.hashCode()
        return result
    }

    companion object {
        fun fromBubblesContact(bubblesContact: Contact): RoomContact =
            RoomContact(
                id = bubblesContact.id.uuid,
                encName = bubblesContact.encName,
                encSharedKey = bubblesContact.encSharedKey?.encKey,
                updatedAt = bubblesContact.updatedAt.toJavaInstant(),
                sharedConversationId = bubblesContact.sharedConversationId.uuid,
                encSharingMode = bubblesContact.encSharingMode,
                consultedAt = bubblesContact.consultedAt?.toJavaInstant(),
                safeId = SafeId(bubblesContact.safeId.uuid),
                encResetConversationDate = bubblesContact.encResetConversationDate,
            )
    }
}
