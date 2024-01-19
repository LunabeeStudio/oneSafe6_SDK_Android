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
 * Created by Lunabee Studio / Date - 6/30/2023 - for the oneSafe6 SDK.
 * Last modified 30/06/2023 09:56
 */

package studio.lunabee.onesafe.storage.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import studio.lunabee.onesafe.messaging.domain.model.EncConversation
import java.util.UUID

@Entity(
    tableName = "DoubleRatchetConversation",
    foreignKeys = [
        ForeignKey(
            entity = RoomContact::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("id"),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
class RoomDoubleRatchetConversation(
    @PrimaryKey
    val id: UUID,
    val encPersonalPublicKey: ByteArray, // DRPublicKey
    val encPersonalPrivateKey: ByteArray, // DRPrivateKey
    val encMessageNumber: ByteArray, // Int
    val encSequenceNumber: ByteArray, // Int
    val encRootKey: ByteArray? = null, // DRRootKey?
    val encSendingChainKey: ByteArray? = null, // DRChainKey?
    val encReceiveChainKey: ByteArray? = null, // DRChainKey?
    val encLastContactPublicKey: ByteArray? = null, // DRPublicKey?
    val encReceivedLastMessageNumber: ByteArray? = null, // Int?
) {
    fun toEncConversation(): EncConversation {
        return EncConversation(
            id = id,
            encPersonalPublicKey = encPersonalPublicKey,
            encPersonalPrivateKey = encPersonalPrivateKey,
            encMessageNumber = encMessageNumber,
            encSequenceNumber = encSequenceNumber,
            encRootKey = encRootKey,
            encSendingChainKey = encSendingChainKey,
            encReceiveChainKey = encReceiveChainKey,
            encLastContactPublicKey = encLastContactPublicKey,
            encReceivedLastMessageNumber = encReceivedLastMessageNumber,
        )
    }

    companion object {
        fun fromEncConversation(conversation: EncConversation): RoomDoubleRatchetConversation {
            return RoomDoubleRatchetConversation(
                id = conversation.id,
                encPersonalPublicKey = conversation.encPersonalPublicKey,
                encPersonalPrivateKey = conversation.encPersonalPrivateKey,
                encMessageNumber = conversation.encMessageNumber,
                encSequenceNumber = conversation.encSequenceNumber,
                encRootKey = conversation.encRootKey,
                encSendingChainKey = conversation.encSendingChainKey,
                encReceiveChainKey = conversation.encReceiveChainKey,
                encLastContactPublicKey = conversation.encLastContactPublicKey,
                encReceivedLastMessageNumber = conversation.encReceivedLastMessageNumber,
            )
        }
    }
}
