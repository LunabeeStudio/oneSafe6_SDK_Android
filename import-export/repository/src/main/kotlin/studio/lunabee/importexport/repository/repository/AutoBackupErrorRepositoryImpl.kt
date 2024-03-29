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
 * Created by Lunabee Studio / Date - 11/21/2023 - for the oneSafe6 SDK.
 * Last modified 11/21/23, 4:24 PM
 */

package studio.lunabee.importexport.repository.repository

import kotlinx.coroutines.flow.Flow
import studio.lunabee.importexport.repository.datasource.AutoBackupErrorLocalDataSource
import studio.lunabee.onesafe.importexport.model.AutoBackupError
import studio.lunabee.onesafe.importexport.repository.AutoBackupErrorRepository
import javax.inject.Inject

class AutoBackupErrorRepositoryImpl @Inject constructor(
    private val dataSource: AutoBackupErrorLocalDataSource,
) : AutoBackupErrorRepository {
    override fun getError(): Flow<AutoBackupError?> = dataSource.getError()
    override suspend fun setError(error: AutoBackupError?): Unit = dataSource.setError(error)
}
