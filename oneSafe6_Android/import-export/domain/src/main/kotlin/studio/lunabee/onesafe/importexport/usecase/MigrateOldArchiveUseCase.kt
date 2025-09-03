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
 * Created by Lunabee Studio / Date - 9/29/2023 - for the oneSafe6 SDK.
 * Last modified 9/29/23, 6:05 PM
 */

package studio.lunabee.onesafe.importexport.usecase

import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.onStart
import studio.lunabee.onesafe.domain.model.importexport.ImportMode
import studio.lunabee.onesafe.domain.qualifier.ArchiveCacheDir
import studio.lunabee.onesafe.domain.repository.MigrationCryptoRepository
import studio.lunabee.onesafe.importexport.engine.ImportEngine
import java.io.File
import java.io.InputStream
import javax.inject.Inject

class MigrateOldArchiveUseCase @Inject constructor(
    private val importEngine: ImportEngine,
    private val archiveUnzipUseCase: ArchiveUnzipUseCase,
    private val migrationCryptoRepository: MigrationCryptoRepository,
    @param:ArchiveCacheDir(type = ArchiveCacheDir.Type.Migration) private val archiveDir: File,
) {
    operator fun invoke(importMode: ImportMode, inputStream: InputStream, encPassword: ByteArray): Flow<LBFlowResult<Unit>> = flow {
        var result = archiveUnzipUseCase(inputStream, archiveDir).last()

        if (result is LBFlowResult.Success) {
            val password = migrationCryptoRepository.decryptMigrationArchivePassword(encPassword)
            result = importEngine.authenticateAndExtractData(archiveDir, password).last()

            if (result is LBFlowResult.Success) {
                result = importEngine.prepareDataForImport(archiveDir, importMode).last()
                if (result is LBFlowResult.Success) {
                    result = when (val saveResult = importEngine.saveImportData(mode = importMode).last()) {
                        is LBFlowResult.Failure -> LBFlowResult.Failure(saveResult.throwable)
                        is LBFlowResult.Loading -> LBFlowResult.Loading()
                        is LBFlowResult.Success -> LBFlowResult.Success(Unit)
                    }
                }
            }
        }

        emit(result)
    }.onStart { emit(LBFlowResult.Loading()) }
}
