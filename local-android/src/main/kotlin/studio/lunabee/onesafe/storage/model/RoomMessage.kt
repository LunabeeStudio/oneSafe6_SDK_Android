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
import androidx.room.Index
import androidx.room.PrimaryKey
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.model.MessageDirection
import studio.lunabee.messaging.domain.model.SafeMessage
import java.util.UUID

@Entity(
    tableName = "Message",
    foreignKeys = [
        ForeignKey(
            entity = RoomContact::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("contact_id"),
            onDelete = ForeignKey.CASCADE, // TODO <bubbles> to validate
        ),
    ],
    indices = [
        Index(value = ["order", "contact_id"], unique = true, orders = [Index.Order.DESC, Index.Order.DESC]),
    ],
)
class RoomMessage(
    @PrimaryKey val id: UUID,
    @ColumnInfo(name = "contact_id", index = true) val contactId: UUID,
    @ColumnInfo(name = "enc_sent_at") val encSentAt: ByteArray,
    @ColumnInfo(name = "enc_content") val encContent: ByteArray,
    @ColumnInfo(name = "direction") val direction: MessageDirection,
    @ColumnInfo(name = "order") val order: Float,
    @ColumnInfo(name = "enc_channel") val encChannel: ByteArray?,
    @ColumnInfo(name = "is_read", defaultValue = "true") val isRead: Boolean,
) {
    fun toMessage(): SafeMessage = SafeMessage(
        id = DoubleRatchetUUID(id),
        fromContactId = DoubleRatchetUUID(contactId),
        encSentAt = encSentAt,
        encContent = encContent,
        direction = direction,
        encChannel = encChannel,
        isRead = isRead,
    )

    companion object {
        fun fromMessage(message: SafeMessage, order: Float): RoomMessage = RoomMessage(
            id = message.id.uuid,
            contactId = message.fromContactId.uuid,
            encSentAt = message.encSentAt,
            encContent = message.encContent,
            direction = message.direction,
            order = order,
            encChannel = message.encChannel,
            isRead = message.isRead,
        )
    }
}
