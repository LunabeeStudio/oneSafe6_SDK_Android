/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 9/29/2023 - for the oneSafe6 SDK.
 * Last modified 9/29/23, 2:46 PM
 */

package studio.lunabee.onesafe.importexport.usecase

import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.lunabee.onesafe.domain.qualifier.ArchiveCacheDir
import studio.lunabee.onesafe.domain.qualifier.BackupType
import studio.lunabee.onesafe.domain.repository.BackupRepository
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.importexport.engine.BackupExportEngine
import java.io.File
import javax.inject.Inject

/**
 * Creates a backup and moves it to the auto backups internal storage
 */
class AutoBackupUseCase @Inject constructor(
    private val exportBackupUseCase: ExportBackupUseCase,
    private val backupRepository: BackupRepository,
    @BackupType(BackupType.Type.Auto) private val exportEngine: BackupExportEngine,
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.AutoBackup) private val archiveDir: File,
) {
    operator fun invoke(): Flow<LBFlowResult<Unit>> {
        return exportBackupUseCase(exportEngine, archiveDir).map { result ->
            when (result) {
                is LBFlowResult.Failure -> LBFlowResult.Failure(throwable = result.throwable)
                is LBFlowResult.Loading -> LBFlowResult.Loading(progress = result.progress)
                is LBFlowResult.Success -> {
                    val backupFile = result.successData
                    val moveResult = OSError.runCatching {
                        backupRepository.addBackup(backupFile)
                    }.asFlowResult()
                    backupFile.delete()
                    moveResult
                }
            }
        }
    }
}
