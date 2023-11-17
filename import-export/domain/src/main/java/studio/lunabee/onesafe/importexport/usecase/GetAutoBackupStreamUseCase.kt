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
 * Created by Lunabee Studio / Date - 11/15/2023 - for the oneSafe6 SDK.
 * Last modified 11/15/23, 9:45 AM
 */

package studio.lunabee.onesafe.importexport.usecase

import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBFlowResult.Companion.mapResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import studio.lunabee.onesafe.error.OSImportExportError
import studio.lunabee.onesafe.importexport.repository.CloudBackupRepository
import studio.lunabee.onesafe.importexport.repository.LocalBackupRepository
import java.io.InputStream
import javax.inject.Inject

/**
 * Get an [InputStream] on a local or cloud backup. Gives priority to local backup if the backup exist in both cloud and local storage.
 * It is the caller responsibility to close the [InputStream]
 */
class GetAutoBackupStreamUseCase @Inject constructor(
    private val localBackupRepository: LocalBackupRepository,
    private val cloudBackupRepository: CloudBackupRepository,
) {
    /**
     * @return A success with the input stream or a failure if no backup found for the given ID in both storages
     */
    operator fun invoke(backupId: String): Flow<LBFlowResult<InputStream>> = flow {
        val localBackupFile = localBackupRepository.getFile(backupId)
        if (localBackupFile != null) {
            emit(LBFlowResult.Success(localBackupFile.inputStream()))
        } else {
            val flow = cloudBackupRepository.getInputStream(backupId)
                .mapResult<InputStream, InputStream>(
                    mapError = {
                        if ((it as? OSImportExportError)?.code == OSImportExportError.Code.BACKUP_ID_NOT_FOUND) {
                            // TODO <AutoBackup> map error
                            it
                        } else {
                            it
                        }
                    },
                )
            emitAll(flow)
        }
    }
}
