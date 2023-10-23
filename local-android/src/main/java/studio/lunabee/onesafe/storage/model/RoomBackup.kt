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
import androidx.room.Index
import androidx.room.PrimaryKey
import studio.lunabee.onesafe.importexport.model.CloudBackup
import studio.lunabee.onesafe.importexport.model.LocalBackup
import java.io.File
import java.time.Instant

@Entity(
    tableName = "Backup",
    indices = [Index("remote_id", unique = true)],
)
data class RoomBackup(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "remote_id") val remoteId: String?,
    @ColumnInfo(name = "local_file") val localFile: File?,
    val date: Instant,
)

data class RoomLocalBackup(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "local_file") val localFile: File,
    val date: Instant,
) {
    fun toBackup(): LocalBackup = LocalBackup(
        date = date,
        file = localFile,
    )

    companion object {
        fun fromBackup(backup: LocalBackup): RoomLocalBackup =
            RoomLocalBackup(
                id = backup.file.name,
                localFile = backup.file,
                date = backup.date,
            )
    }
}

data class RoomCloudBackup(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "remote_id") val remoteId: String,
    val date: Instant,
) {
    fun toBackup(): CloudBackup = CloudBackup(
        remoteId = remoteId,
        name = id,
        date = date,
    )

    companion object {
        fun fromBackup(backup: CloudBackup): RoomCloudBackup =
            RoomCloudBackup(
                id = backup.name,
                remoteId = backup.remoteId,
                date = backup.date,
            )
    }
}
