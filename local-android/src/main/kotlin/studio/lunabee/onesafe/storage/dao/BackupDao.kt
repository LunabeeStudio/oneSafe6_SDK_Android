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
 * Created by Lunabee Studio / Date - 6/20/2023 - for the oneSafe6 SDK.
 * Last modified 6/20/23, 1:28 PM
 */

package studio.lunabee.onesafe.storage.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.importexport.model.CloudBackup
import studio.lunabee.onesafe.storage.model.RoomBackup
import studio.lunabee.onesafe.storage.model.RoomCloudBackup
import studio.lunabee.onesafe.storage.model.RoomLocalBackup
import java.io.File

@RewriteQueriesToDropUnusedColumns
@Dao
interface BackupDao {
    @Upsert(entity = RoomBackup::class)
    suspend fun insert(backup: RoomLocalBackup)

    @Upsert(entity = RoomBackup::class)
    suspend fun insertLocals(backup: List<RoomLocalBackup>)

    @Upsert(entity = RoomBackup::class)
    suspend fun insert(backup: RoomCloudBackup)

    @Upsert(entity = RoomBackup::class)
    suspend fun upsertCloudBackups(backup: List<RoomCloudBackup>)

    @Query("SELECT * FROM Backup WHERE local_file IS NOT NULL AND safe_id IS :safeId ORDER BY `date` DESC")
    suspend fun getAllLocal(safeId: SafeId): List<RoomLocalBackup>

    @Query("SELECT * FROM Backup WHERE local_file IS NOT NULL AND remote_id IS NULL AND safe_id IS :safeId ORDER BY `date` DESC")
    suspend fun getAllLocalWithoutRemote(safeId: SafeId): List<RoomLocalBackup>

    @Query("SELECT * FROM Backup WHERE remote_id IS NOT NULL AND (safe_id IS :safeId OR safe_id IS NULL) ORDER BY `date` DESC")
    suspend fun getAllCloud(safeId: SafeId): List<RoomCloudBackup>

    @Query("SELECT * FROM Backup WHERE remote_id IS NOT NULL AND (safe_id IS :safeId OR safe_id IS NULL) ORDER BY `date` DESC")
    fun getCloudBackupsFlow(safeId: SafeId): Flow<List<RoomCloudBackup>>

    @Query("SELECT * FROM Backup WHERE local_file IS NOT NULL AND safe_id IS :safeId ORDER BY `date` DESC")
    fun getAllLocalAsFlow(safeId: SafeId): Flow<List<RoomLocalBackup>>

    @Query("SELECT * FROM Backup WHERE id = :id AND local_file IS NOT NULL")
    suspend fun getLocalById(id: String): RoomLocalBackup?

    @Query("UPDATE Backup SET local_file = NULL WHERE id = :id")
    suspend fun nullifyLocalIdById(id: String)

    @Query("UPDATE Backup SET remote_id = NULL WHERE safe_id IS :safeId")
    suspend fun nullifyAllRemoteId(safeId: SafeId)

    @Query("UPDATE Backup SET remote_id = NULL WHERE id = :id")
    suspend fun nullifyRemoteIdById(id: String)

    @Query("DELETE FROM Backup WHERE local_file IS NULL AND remote_id IS NULL")
    suspend fun deleteOrphans()

    @Query("UPDATE Backup SET remote_id = NULL WHERE id NOT IN (:exceptIds)")
    suspend fun deleteCloudExceptIds(exceptIds: List<String>)

    @Transaction
    suspend fun refreshCloudBackups(backups: List<CloudBackup>) {
        deleteCloudExceptIds(backups.map { it.remoteId })
        upsertCloudBackups(backups.map(RoomCloudBackup::fromBackup))
        deleteOrphans()
    }

    @Transaction
    suspend fun deleteCloudBackup(id: String) {
        nullifyRemoteIdById(id)
        deleteOrphans()
    }

    @Transaction
    suspend fun deleteLocalBackup(id: String) {
        nullifyLocalIdById(id)
        deleteOrphans()
    }

    @Query("SELECT remote_id FROM Backup WHERE id = :backupId")
    suspend fun getRemoteId(backupId: String): String?

    @Query("SELECT local_file FROM Backup WHERE id = :backupId")
    suspend fun getFile(backupId: String): File?

    @Query("SELECT * FROM Backup WHERE remote_id IS NOT NULL AND (safe_id IS :safeId OR safe_id IS NULL) ORDER BY `date` DESC LIMIT 1")
    suspend fun getLatestCloudBackup(safeId: SafeId): RoomCloudBackup?

    @Query("SELECT * FROM Backup WHERE remote_id IS NOT NULL AND (safe_id IS :safeId OR safe_id IS NULL) ORDER BY `date` DESC LIMIT 1")
    fun getLatestCloudBackupFlow(safeId: SafeId): Flow<RoomCloudBackup?>

    @Query("SELECT EXISTS(SELECT 1 FROM Backup WHERE local_file IS NOT NULL AND safe_id IS :safeId LIMIT 1)")
    fun hasLocalBackup(safeId: SafeId): Flow<Boolean>

    @Transaction
    suspend fun deleteAllCloudBackup(safeId: SafeId) {
        nullifyAllRemoteId(safeId)
        deleteOrphans()
    }
}
