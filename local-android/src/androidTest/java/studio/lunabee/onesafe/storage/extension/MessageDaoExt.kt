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
 * Created by Lunabee Studio / Date - 6/21/2023 - for the oneSafe6 SDK.
 * Last modified 6/21/23, 5:21 PM
 */

package studio.lunabee.onesafe.storage.extension

import studio.lunabee.onesafe.messaging.domain.model.MessageDirection
import studio.lunabee.onesafe.storage.dao.MessageDao
import studio.lunabee.onesafe.storage.model.RoomMessage
import studio.lunabee.onesafe.test.testUUIDs
import java.util.UUID

suspend fun MessageDao.insert(
    id: UUID,
    contactId: UUID = testUUIDs[0],
    encSentAt: ByteArray = byteArrayOf(),
    encContent: ByteArray = byteArrayOf(),
    direction: MessageDirection = MessageDirection.SENT,
    order: Float = 0f,
    encChannel: ByteArray = byteArrayOf(),
) {
    insert(
        RoomMessage(
            id = id,
            contactId = contactId,
            encSentAt = encSentAt,
            encContent = encContent,
            direction = direction,
            order = order,
            encChannel = encChannel,
        ),
    )
}
