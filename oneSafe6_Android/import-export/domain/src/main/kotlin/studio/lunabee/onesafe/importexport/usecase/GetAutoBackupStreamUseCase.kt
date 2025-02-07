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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.error.OSDriveError
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.error.OSImportExportError
import studio.lunabee.onesafe.error.OSRepositoryError
import studio.lunabee.onesafe.error.osCatch
import studio.lunabee.onesafe.error.osCode
import studio.lunabee.onesafe.importexport.repository.CloudBackupRepository
import studio.lunabee.onesafe.importexport.repository.LocalBackupRepository
import studio.lunabee.onesafe.jvm.onFailure
import java.io.InputStream
import javax.inject.Inject

/**
 * Get an [InputStream] on a local or cloud backup. Gives priority to local backup if the backup exist in both cloud and local storage.
 * When using cloud backup, the safe id must be loaded in memory in order to create the stream from Drive.
 * It is the caller responsibility to close the [InputStream]
 */
class GetAutoBackupStreamUseCase @Inject constructor(
    private val localBackupRepository: LocalBackupRepository,
    private val cloudBackupRepository: CloudBackupRepository,
    private val safeRepository: SafeRepository,
) {
    /**
     * @return A success with the input stream or a failure if no backup found for the given ID in both storages
     */
    operator fun invoke(backupId: String): Flow<LBFlowResult<InputStream>> = flow {
        val localBackupFile = localBackupRepository.getFile(backupId)
        if (localBackupFile != null) {
            emit(LBFlowResult.Success(localBackupFile.inputStream()))
        } else {
            val safeId = safeRepository.currentSafeId()
            val flow = cloudBackupRepository.getInputStream(backupId, safeId)
                .onFailure {
                    if (it.throwable.osCode() == OSDriveError.Code.DRIVE_BACKUP_REMOTE_ID_NOT_FOUND) {
                        cloudBackupRepository.refreshBackupList(safeId).collect()
                    }
                }
            emitAll(flow)
        }
    }.osCatch { osError ->
        if (osError.code == OSRepositoryError.Code.SAFE_ID_NOT_LOADED) {
            OSImportExportError.Code.CANNOT_OPEN_STREAM_WITHOUT_SAFE_ID.get()
        } else {
            osError
        }
    }
}
