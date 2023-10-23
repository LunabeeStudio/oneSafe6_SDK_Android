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
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.error.OSDomainError
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class ArchiveZipUseCase @Inject constructor(
    @FileDispatcher private val fileDispatcher: CoroutineDispatcher,
) {
    /**
     * Zip a file to a specific directory.
     *
     * @param folderToZip folder to zip
     * @param outputZipFile destination to keep the final archive
     *
     * @return a flow emitting unzip progress.
     */
    operator fun invoke(folderToZip: File, outputZipFile: File): Flow<LBFlowResult<File>> {
        return flow<LBFlowResult<File>> {
            try {
                @Suppress("BlockingMethodInNonBlockingContext")
                ZipOutputStream(BufferedOutputStream(FileOutputStream(outputZipFile))).use { outStream ->
                    zip(zipDestStream = outStream, sourceFile = folderToZip, parentFile = null, archiveName = outputZipFile.name)
                }
                // Delete zipped files except the zip itself.
                folderToZip.listFiles()?.forEach { fileToDelete ->
                    if (fileToDelete.name != outputZipFile.name) fileToDelete.deleteRecursively()
                }
                emit(LBFlowResult.Success(outputZipFile))
            } catch (e: Exception) {
                emit(LBFlowResult.Failure(OSDomainError(code = OSDomainError.Code.ZIP_FAILURE, cause = e)))
            }
        }.flowOn(fileDispatcher)
    }

    /**
     * @see invoke
     *
     * @param folderToZip folder to zip
     * @param outputZipFileStream destination to keep the final archive
     *
     * @return a flow emitting unzip progress.
     */
    operator fun invoke(folderToZip: File, outputZipFileStream: OutputStream): Flow<LBFlowResult<Unit>> {
        return flow<LBFlowResult<Unit>> {
            try {
                ZipOutputStream(BufferedOutputStream(outputZipFileStream)).use { outStream ->
                    zip(zipDestStream = outStream, sourceFile = folderToZip, parentFile = null, archiveName = null)
                }
                // Delete zipped files except the zip itself.
                folderToZip.listFiles()?.forEach { fileToDelete ->
                    fileToDelete.deleteRecursively()
                }
                emit(LBFlowResult.Success(Unit))
            } catch (e: Exception) {
                emit(LBFlowResult.Failure(OSDomainError(code = OSDomainError.Code.ZIP_FAILURE, cause = e)))
            }
        }.flowOn(fileDispatcher)
    }

    private fun <T : Any> FlowCollector<LBFlowResult<T>>.zip(
        zipDestStream: ZipOutputStream,
        sourceFile: File,
        parentFile: File?,
        archiveName: String?,
    ) {
        sourceFile.listFiles()?.forEach { file ->
            if (file.name == archiveName) return@forEach // avoid zipping archive currently in creation.
            // If current file read is a directory, create a zip entry and recursively add all its children.
            if (file.isDirectory) {
                val entry = ZipEntry(file.name + File.separator)
                zipDestStream.putNextEntry(entry)
                zip(zipDestStream, file, file, archiveName)
            } else {
                // Determine file name depending on its parent.
                val fileName: String = if (parentFile == null) file.name else parentFile.name + File.separator + file.name
                zipDestStream.putNextEntry(ZipEntry(fileName))
                BufferedInputStream(FileInputStream(file)).use { inStream -> inStream.copyTo(zipDestStream) }
            }
        }
    }
}
