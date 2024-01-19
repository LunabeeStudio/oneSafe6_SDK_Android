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
 * Created by Lunabee Studio / Date - 6/14/2023 - for the oneSafe6 SDK.
 * Last modified 6/14/23, 1:18 PM
 */

package studio.lunabee.onesafe.storage.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import studio.lunabee.onesafe.bubbles.domain.model.ContactLocalKey
import java.util.UUID

@Entity(
    tableName = "ContactLocalKey",
    foreignKeys = [
        ForeignKey(
            entity = RoomContact::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("contact_id"),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
class RoomContactKey(
    @PrimaryKey
    @ColumnInfo(name = "contact_id")
    val contactId: UUID,
    @ColumnInfo(name = "enc_value") val encLocalKey: ContactLocalKey,
)
