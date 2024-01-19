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

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import studio.lunabee.importexport.repository.datasource.AutoBackupErrorLocalDataSource
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.importexport.model.AutoBackupError
import studio.lunabee.onesafe.storage.model.LocalAutoBackupError
import javax.inject.Inject

class AutoBackupErrorLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<LocalAutoBackupError>,
    @FileDispatcher private val fileDispatcher: CoroutineDispatcher,
) : AutoBackupErrorLocalDataSource {
    override fun getError(): Flow<AutoBackupError?> =
        dataStore.data.map { localAutoBackupError ->
            localAutoBackupError.takeUnless { it == LocalAutoBackupError.default }?.toAutoBackupError()
        }.flowOn(fileDispatcher)

    override suspend fun setError(error: AutoBackupError?): AutoBackupError? = dataStore.updateData {
        if (error == null) {
            LocalAutoBackupError.default
        } else {
            LocalAutoBackupError.fromAutoBackupError(error)
        }
    }.takeUnless { it == LocalAutoBackupError.default }?.toAutoBackupError()
}
