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

package studio.lunabee.onesafe.storage.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.lunabee.importexport.datasource.AutoBackupErrorLocalDataSource
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.importexport.model.AutoBackupError
import studio.lunabee.onesafe.storage.dao.AutoBackupErrorDao
import studio.lunabee.onesafe.storage.model.RoomAutoBackupError
import java.util.UUID
import javax.inject.Inject

class AutoBackupErrorLocalDataSourceImpl @Inject constructor(
    private val dao: AutoBackupErrorDao,
) : AutoBackupErrorLocalDataSource {
    override fun getLastError(safeId: SafeId): Flow<AutoBackupError?> = dao.getLastError(safeId).map { it?.toAutoBackupError() }
    override suspend fun addError(error: AutoBackupError): Unit = dao.setError(RoomAutoBackupError.from(error))
    override suspend fun removeError(errorId: UUID): Unit = dao.removeError(errorId)
}
