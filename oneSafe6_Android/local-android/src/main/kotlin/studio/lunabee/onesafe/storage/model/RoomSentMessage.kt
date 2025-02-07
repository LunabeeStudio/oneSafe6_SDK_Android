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
 * Created by Lunabee Studio / Date - 8/18/2023 - for the oneSafe6 SDK.
 * Last modified 18/08/2023 10:05
 */

package studio.lunabee.onesafe.storage.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.model.SentMessage
import java.util.UUID

@Entity(
    tableName = "SentMessage",
    foreignKeys = [
        ForeignKey(
            entity = RoomMessage::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("id"),
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
class RoomSentMessage(
    @PrimaryKey val id: UUID,
    @ColumnInfo("enc_content") val encContent: ByteArray,
    @ColumnInfo("enc_created_at") val encCreatedAt: ByteArray,
    @ColumnInfo("contact_id") val contactId: UUID,
    @ColumnInfo("safe_id", index = true) val safeId: UUID,
    val order: Float,
) {
    fun toSentMessage(): SentMessage = SentMessage(
        id = DoubleRatchetUUID(id),
        encContent = encContent,
        encCreatedAt = encCreatedAt,
        contactId = DoubleRatchetUUID(contactId),
        order = order,
        safeId = DoubleRatchetUUID(id),
    )

    companion object {
        fun fromSentMessage(sentMessage: SentMessage): RoomSentMessage = RoomSentMessage(
            id = sentMessage.id.uuid,
            encContent = sentMessage.encContent,
            encCreatedAt = sentMessage.encCreatedAt,
            contactId = sentMessage.contactId.uuid,
            order = sentMessage.order,
            safeId = sentMessage.safeId.uuid,
        )
    }
}
