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
import androidx.room.Index
import androidx.room.PrimaryKey
import studio.lunabee.onesafe.messaging.domain.model.EnqueuedMessage

@Entity(
    tableName = "EnqueuedMessage",
    indices = [Index("enc_message", unique = true)],
)
class RoomEnqueuedMessage(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "enc_message") val encMessage: ByteArray,
    @ColumnInfo(name = "enc_channel") val encChannel: ByteArray?,
) {
    fun toEnqueuedMessage(): EnqueuedMessage = EnqueuedMessage(
        id = id,
        encIncomingMessage = encMessage,
        encChannel = encChannel,
    )

    companion object {
        fun fromEnqueuedMessage(encMessage: ByteArray, encChannel: ByteArray?): RoomEnqueuedMessage = RoomEnqueuedMessage(
            encMessage = encMessage,
            encChannel = encChannel,
        )
    }
}
