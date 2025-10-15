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
 * Created by Lunabee Studio / Date - 7/6/2023 - for the oneSafe6 SDK.
 * Last modified 06/07/2023 10:29
 */

package studio.lunabee.onesafe.storage.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.model.EncHandShakeData
import java.util.UUID

@Entity(
    tableName = "HandShakeData",
    foreignKeys = [
        ForeignKey(
            entity = RoomContact::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("conversation_local_id"),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
class RoomHandShakeData(
    @PrimaryKey
    @ColumnInfo(name = "conversation_local_id")
    val conversationLocalId: UUID,
    val encConversationSharedId: ByteArray, // UUID
    val encOneSafePrivateKey: ByteArray?, // ByteArray
    val encOneSafePublicKey: ByteArray?, // ByteArray
) {
    fun toHandShakeData(): EncHandShakeData = EncHandShakeData(
        encConversationSharedId = encConversationSharedId,
        encOneSafePrivateKey = encOneSafePrivateKey,
        encOneSafePublicKey = encOneSafePublicKey,
        conversationLocalId = DoubleRatchetUUID(conversationLocalId),
    )

    companion object {
        fun fromHandShakeData(handShakeData: EncHandShakeData): RoomHandShakeData = RoomHandShakeData(
            conversationLocalId = handShakeData.conversationLocalId.uuid,
            encConversationSharedId = handShakeData.encConversationSharedId,
            encOneSafePrivateKey = handShakeData.encOneSafePrivateKey,
            encOneSafePublicKey = handShakeData.encOneSafePublicKey,
        )
    }
}
