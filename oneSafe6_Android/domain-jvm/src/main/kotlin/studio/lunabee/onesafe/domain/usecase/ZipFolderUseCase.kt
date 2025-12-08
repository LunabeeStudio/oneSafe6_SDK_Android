/*
 * Copyright (c) 2025 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 12/4/2025 - for the oneSafe6 SDK.
 * Last modified 12/4/25, 9:44 AM
 */

package studio.lunabee.onesafe.domain.usecase

import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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

class ZipFolderUseCase @Inject constructor(
    @param:FileDispatcher private val fileDispatcher: CoroutineDispatcher,
) {
    /**
     * Zip a folder.
     *
     * @param inputFolder folder to zip
     * @param outputZipFile destination to keep the final archive
     * @param deleteFiles delete zipped files after zipping succeeded
     *
     * @return a flow emitting zip progress.
     */
    operator fun invoke(inputFolder: File, outputZipFile: File, deleteFiles: Boolean): Flow<LBFlowResult<File>> = flow<LBFlowResult<File>> {
        outputZipFile.delete()
        outputZipFile.parentFile?.mkdirs()

        @Suppress("BlockingMethodInNonBlockingContext")
        ZipOutputStream(BufferedOutputStream(FileOutputStream(outputZipFile))).use { outStream ->
            zip(zipDestStream = outStream, sourceFile = inputFolder, parentFile = null, archiveName = outputZipFile.name)
        }
        if (deleteFiles) {
            // Delete zipped files except the zip itself.
            inputFolder.listFiles()?.forEach { fileToDelete ->
                if (fileToDelete.name != outputZipFile.name) fileToDelete.deleteRecursively()
            }
        }
        emit(LBFlowResult.Success(outputZipFile))
    }.catch { e ->
        emit(LBFlowResult.Failure(OSDomainError(code = OSDomainError.Code.ZIP_FAILURE, cause = e)))
    }.flowOn(fileDispatcher)

    /**
     * @see invoke
     *
     * @param inputFolder folder to zip
     * @param outputZipFileStream destination to keep the final archive
     *
     * @return a flow emitting zip progress.
     */
    operator fun invoke(
        inputFolder: File,
        outputZipFileStream: OutputStream,
        deleteFiles: Boolean,
    ): Flow<LBFlowResult<Unit>> = flow<LBFlowResult<Unit>> {
        ZipOutputStream(BufferedOutputStream(outputZipFileStream)).use { outStream ->
            zip(zipDestStream = outStream, sourceFile = inputFolder, parentFile = null, archiveName = null)
        }
        if (deleteFiles) {
            // Delete zipped files except the zip itself.
            inputFolder.listFiles()?.forEach { fileToDelete ->
                fileToDelete.deleteRecursively()
            }
        }
        emit(LBFlowResult.Success(Unit))
    }.catch { e ->
        emit(LBFlowResult.Failure(OSDomainError(code = OSDomainError.Code.ZIP_FAILURE, cause = e)))
    }.flowOn(fileDispatcher)

    private fun zip(
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
