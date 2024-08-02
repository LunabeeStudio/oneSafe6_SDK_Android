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
import com.lunabee.lbcore.model.LBFlowResult.Companion.transformResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.onCompletion
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.qualifier.ArchiveCacheDir
import studio.lunabee.onesafe.domain.qualifier.BackupType
import studio.lunabee.onesafe.importexport.engine.BackupExportEngine
import studio.lunabee.onesafe.importexport.model.CloudBackup
import studio.lunabee.onesafe.importexport.repository.CloudBackupRepository
import java.io.File
import javax.inject.Inject

// TODO <AutoBackup> unit test

/**
 * Creates a backup and send it to cloud storage
 */
class CloudAutoBackupUseCase @Inject constructor(
    private val exportBackupUseCase: ExportBackupUseCase,
    private val cloudBackupRepository: CloudBackupRepository,
    @BackupType(BackupType.Type.Auto) private val exportEngine: BackupExportEngine,
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.AutoBackup) private val archiveDir: File,
) {
    operator fun invoke(safeId: SafeId): Flow<LBFlowResult<CloudBackup>> {
        return exportBackupUseCase(
            exportEngine = exportEngine,
            archiveExtractedDirectory = archiveDir,
            safeId = safeId,
        ).transformResult { result ->
            val backup = result.successData
            val uploadFlow = cloudBackupRepository.uploadBackup(backup)
                .onCompletion {
                    backup.file.delete()
                }
            emitAll(uploadFlow)
        }
    }
}
