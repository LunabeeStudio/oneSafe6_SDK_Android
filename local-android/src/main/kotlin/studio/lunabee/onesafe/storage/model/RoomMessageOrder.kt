/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 7/10/2024 - for the oneSafe6 SDK.
 * Last modified 10/07/2024 17:09
 */

package studio.lunabee.onesafe.storage.model

import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.model.MessageOrder
import java.util.UUID

data class RoomMessageOrder(
    val id: UUID,
    val encSentAt: ByteArray,
    val order: Float,
) {
    fun toMessageOrder(): MessageOrder {
        return MessageOrder(
            id = DoubleRatchetUUID(id),
            encSentAt = encSentAt,
            order = order,
        )
    }
}
