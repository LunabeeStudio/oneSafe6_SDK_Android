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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.domain.utils.mkdirs
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.importexport.model.ImportExportConstant
import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile
import javax.inject.Inject

class ArchiveUnzipUseCase @Inject constructor(
    @FileDispatcher private val fileDispatcher: CoroutineDispatcher,
) {
    /**
     * Unzip a file to a specific directory.
     *
     * @param inputStream stream from selected file
     * @param unzipFolderDestination target destination to unzip [inputStream]. Will be cleaned and created before starting unzip process.
     *
     * @return a flow emitting unzip progress.
     */
    operator fun invoke(inputStream: InputStream, unzipFolderDestination: File): Flow<LBFlowResult<Unit>> {
        return flow<LBFlowResult<Unit>> {
            val sourceFile = File(unzipFolderDestination, ImportExportConstant.ArchiveToImportCopyFileName)
            try {
                unzipFolderDestination.mkdirs(override = true)
                sourceFile.outputStream().use { inputStream.copyTo(it) }
                @Suppress("BlockingMethodInNonBlockingContext")
                val zipFile = ZipFile(sourceFile.path)
                val numberOfEntriesInZip = zipFile.size() // number of entries in the zip file.
                var currentUnzippedEntries = 0
                zipFile.use {
                    zipFile.entries().asSequence().forEach { entry ->
                        if (entry.name == ImportExportConstant.ArchiveToImportCopyFileName) return@forEach
                        zipFile.getInputStream(entry).use { inputStream ->
                            val unzipFolderDestinationPath = unzipFolderDestination.absolutePath + File.separator + entry.name
                            val file = File(unzipFolderDestinationPath)
                            if (entry.isDirectory) {
                                file.mkdirs()
                            } else {
                                file.outputStream().use { outputStream ->
                                    inputStream.copyTo(outputStream) // TODO copy "copyTo" method to emit a much better progress.
                                }
                            }
                        }
                        emit(LBFlowResult.Loading(progress = ((++currentUnzippedEntries).toFloat() / numberOfEntriesInZip.toFloat())))
                    }
                }
                emit(LBFlowResult.Success(Unit))
            } catch (e: Exception) {
                emit(LBFlowResult.Failure(OSDomainError(code = OSDomainError.Code.UNZIP_FAILURE, cause = e)))
            } finally {
                sourceFile.delete()
            }
        }.flowOn(fileDispatcher)
    }
}
