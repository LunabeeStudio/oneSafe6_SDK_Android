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
 * Created by Lunabee Studio / Date - 10/11/2023 - for the oneSafe6 SDK.
 * Last modified 10/11/23, 10:02 AM
 */

package studio.lunabee.onesafe.storage.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.importexport.model.CloudBackup
import studio.lunabee.onesafe.importexport.model.LocalBackup
import java.io.File
import java.time.Instant

@Entity(
    tableName = "Backup",
    indices = [Index("remote_id", unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = RoomSafe::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("safe_id"),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class RoomBackup(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "remote_id") val remoteId: String?,
    @ColumnInfo(name = "local_file") val localFile: File?,
    @ColumnInfo(name = "date") val date: Instant,
    @ColumnInfo(name = "safe_id", index = true) val safeId: SafeId?,
    @ColumnInfo(name = "name") val name: String?,
)

data class RoomLocalBackup(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "local_file") val localFile: File,
    @ColumnInfo(name = "date") val date: Instant,
    @ColumnInfo(name = "safe_id", index = true) val safeId: SafeId,
) {
    fun toBackup(): LocalBackup = LocalBackup(
        date = date,
        file = localFile,
        safeId = safeId,
    )

    companion object {
        fun fromBackup(backup: LocalBackup): RoomLocalBackup =
            RoomLocalBackup(
                id = backup.file.name + backup.safeId.toString(),
                localFile = backup.file,
                date = backup.date,
                safeId = backup.safeId,
            )
    }
}

data class RoomCloudBackup(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "remote_id") val remoteId: String,
    @ColumnInfo(name = "date") val date: Instant,
    @ColumnInfo(name = "safe_id") val safeId: SafeId?,
    @ColumnInfo(name = "name") val name: String,
) {
    fun toBackup(): CloudBackup = CloudBackup(
        remoteId = remoteId,
        date = date,
        safeId = safeId,
        name = name,
    )

    companion object {
        fun fromBackup(backup: CloudBackup): RoomCloudBackup =
            RoomCloudBackup(
                id = backup.name + backup.safeId?.toString().orEmpty(),
                remoteId = backup.remoteId,
                date = backup.date,
                safeId = backup.safeId,
                name = backup.name,
            )
    }
}
