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
 * Last modified 6/24/24, 9:59 AM
 */

package studio.lunabee.onesafe.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.storage.model.RoomAutoBackupError
import java.util.UUID

@Dao
interface AutoBackupErrorDao {
    @Query("SELECT * FROM AutoBackupError WHERE safe_id IS :safeId ORDER BY date DESC LIMIT 1")
    fun getLastError(safeId: SafeId): Flow<RoomAutoBackupError?>

    @Insert
    suspend fun setError(backupError: RoomAutoBackupError)

    @Query("DELETE FROM AutoBackupError WHERE id = :errorId")
    fun removeError(errorId: UUID)
}
