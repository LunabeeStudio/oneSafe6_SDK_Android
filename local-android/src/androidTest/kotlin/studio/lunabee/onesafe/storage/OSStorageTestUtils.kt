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
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:24 AM
 */

package studio.lunabee.onesafe.storage

import studio.lunabee.onesafe.storage.model.RoomSafeItem
import studio.lunabee.onesafe.storage.model.RoomUpdateSafeItem
import java.time.Instant
import java.util.UUID
import kotlin.random.Random

internal object OSStorageTestUtils {
    fun createRoomSafeItem(
        id: UUID = UUID.randomUUID(),
        encName: ByteArray = Random.nextBytes(0),
        parentId: UUID? = null,
        isFavorite: Boolean = false,
        updatedAt: Instant = Instant.ofEpochMilli(0),
        position: Double = 0.0,
        iconId: UUID = UUID.randomUUID(),
        encColor: ByteArray = Random.nextBytes(0),
        deletedAt: Instant? = null,
        deletedParentId: UUID? = null,
        consultedAt: Instant? = null,
        indexAlpha: Double = 0.0,
        createdAt: Instant = Instant.ofEpochMilli(0),
    ): RoomSafeItem {
        return RoomSafeItem(
            id = id,
            encName = encName,
            parentId = parentId,
            isFavorite = isFavorite,
            updatedAt = updatedAt,
            position = position,
            iconId = iconId,
            encColor = encColor,
            deletedAt = deletedAt,
            deletedParentId = deletedParentId,
            consultedAt = consultedAt,
            indexAlpha = indexAlpha,
            createdAt = createdAt,
        )
    }
}

fun RoomSafeItem.toRoomUpdateSafeItem(): RoomUpdateSafeItem =
    RoomUpdateSafeItem(
        id = id,
        encName = encName,
        parentId = parentId,
        isFavorite = isFavorite,
        updatedAt = updatedAt,
        position = position,
        iconId = iconId,
        encColor = encColor,
        deletedAt = deletedAt,
        deletedParentId = deletedParentId,
        indexAlpha = indexAlpha,
    )
