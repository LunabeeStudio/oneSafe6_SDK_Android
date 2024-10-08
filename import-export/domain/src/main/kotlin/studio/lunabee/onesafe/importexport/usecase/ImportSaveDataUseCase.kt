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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.transformLatest
import studio.lunabee.onesafe.domain.model.importexport.ImportMode
import studio.lunabee.onesafe.importexport.engine.ImportEngine
import java.io.File
import java.util.UUID
import javax.inject.Inject

class ImportSaveDataUseCase @Inject constructor(
    private val importEngine: ImportEngine,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        archiveExtractedDirectory: File,
        mode: ImportMode,
    ): Flow<LBFlowResult<UUID?>> {
        return importEngine.prepareDataForImport(archiveExtractedDirectory = archiveExtractedDirectory, mode = mode)
            .transformLatest { result ->
                when (result) {
                    is LBFlowResult.Failure -> emit(LBFlowResult.Failure(throwable = result.throwable))
                    is LBFlowResult.Loading -> emit(LBFlowResult.Loading(progress = result.progress))
                    is LBFlowResult.Success -> emitAll(importEngine.saveImportData(mode = mode))
                }
            }
    }
}
