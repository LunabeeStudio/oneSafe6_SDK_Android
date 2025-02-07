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
 * Created by Lunabee Studio / Date - 6/24/2024 - for the oneSafe6 SDK.
 * Last modified 6/24/24, 9:53 AM
 */

package studio.lunabee.onesafe.storage.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.importexport.model.AutoBackupError
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import java.time.ZonedDateTime
import java.util.UUID

@Entity(
    tableName = "AutoBackupError",
    foreignKeys = [
        ForeignKey(
            entity = RoomSafe::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("safe_id"),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class RoomAutoBackupError(
    @PrimaryKey @ColumnInfo(name = "id") val id: UUID,
    @ColumnInfo(name = "date") val date: ZonedDateTime,
    @ColumnInfo(name = "code") val code: String,
    @ColumnInfo(name = "message") val message: String?,
    @ColumnInfo(name = "source") val source: AutoBackupMode,
    @ColumnInfo(name = "safe_id", index = true) val safeId: SafeId,
) {
    fun toAutoBackupError(): AutoBackupError = AutoBackupError(
        id = id,
        date = date,
        code = code,
        message = message,
        source = source,
        safeId = safeId,
    )

    companion object {
        fun from(error: AutoBackupError): RoomAutoBackupError =
            RoomAutoBackupError(error.id, error.date, error.code, error.message, error.source, error.safeId)
    }
}
