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

package studio.lunabee.onesafe.domain.usecase.archive

import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lblogger.LBLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.lunabee.onesafe.domain.engine.ImportEngine
import studio.lunabee.onesafe.domain.model.importexport.ImportMetadata
import studio.lunabee.onesafe.error.OSError
import java.io.File
import java.io.InputStream
import javax.inject.Inject

private val log = LBLogger.get<GetMetadataFromArchiveUseCase>()

class GetMetadataFromArchiveUseCase @Inject constructor(
    private val importEngine: ImportEngine,
    private val archiveUnzipUseCase: ArchiveUnzipUseCase,
) {
    /**
     * Extract archive content from [inputStream] in [archiveExtractedDirectory] and read metadata file.
     * Return an [LBFlowResult] with [ImportMetadata] in case of success.
     */
    operator fun invoke(inputStream: InputStream, archiveExtractedDirectory: File): Flow<LBFlowResult<ImportMetadata>> {
        return archiveUnzipUseCase(inputStream, archiveExtractedDirectory).map { unzipResult ->
            when (unzipResult) {
                is LBFlowResult.Failure -> {
                    archiveExtractedDirectory.deleteRecursively()
                    LBFlowResult.Failure(throwable = unzipResult.throwable)
                }
                is LBFlowResult.Loading -> LBFlowResult.Loading(progress = unzipResult.progress)
                is LBFlowResult.Success -> {
                    OSError.runCatching(logger = log) {
                        importEngine.getMetadata(archiveExtractedDirectory)
                    }.asFlowResult()
                }
            }
        }
    }
}
