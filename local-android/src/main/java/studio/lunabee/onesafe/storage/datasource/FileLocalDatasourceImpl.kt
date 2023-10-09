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
 * Created by Lunabee Studio / Date - 9/28/2023 - for the oneSafe6 SDK.
 * Last modified 28/09/2023 09:55
 */

package studio.lunabee.onesafe.storage.datasource

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import studio.lunabee.onesafe.repository.datasource.FileLocalDatasource
import studio.lunabee.onesafe.storage.utils.cancelableCopyTo
import java.io.File
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject

class FileLocalDatasourceImpl @Inject constructor(
    @ApplicationContext appContext: Context,
) : FileLocalDatasource {
    private val fileDir: File = File(appContext.filesDir, FILE_DIR)
    private val cacheDir: File = File(appContext.cacheDir, FILE_DIR).also {
        it.deleteOnExit()
    }

    companion object {
        private const val FILE_DIR: String = "files"
    }

    override fun getFile(filename: String): File {
        return File(fileDir, filename)
    }

    override fun createTempFile(fileId: String): File {
        if (!cacheDir.exists()) {
            cacheDir.mkdir()
        }
        return File.createTempFile(fileId, null, cacheDir)
    }

    override fun addFile(filename: String, file: ByteArray): File {
        if (!fileDir.exists()) {
            fileDir.mkdir()
        }

        val localFile = File(fileDir, filename)
        localFile.writeBytes(file)
        return localFile
    }

    override fun removeFile(filename: String): Boolean {
        val file = File(fileDir, filename)
        return file.delete()
    }

    override fun removeAllFiles(): Boolean {
        return fileDir.deleteRecursively()
    }

    override fun deleteFile(filename: String): Boolean {
        return File(fileDir, filename).delete()
    }

    override fun getAllFiles(): List<File> {
        return fileDir.listFiles()?.toList().orEmpty()
    }

    override fun copyAndDeleteFile(newFile: File, fileId: UUID) {
        newFile.copyTo(target = File(fileDir, fileId.toString()))
        newFile.delete()
    }

    override fun getFiles(filesId: List<String>): List<File> {
        return fileDir.listFiles()?.filter { filesId.contains(it.name) }.orEmpty()
    }

    /**
     * Save plain file in cacheDir/files/[itemId]/[fieldId]/[filename]
     */
    override suspend fun savePlainFile(inputStream: InputStream, filename: String, itemId: UUID, fieldId: UUID): File {
        val file = File(cacheDir, "$itemId/$fieldId/$filename")
        file.parentFile?.mkdirs()
        file.deleteOnExit()
        inputStream.use { input ->
            file.outputStream().use { output ->
                input.cancelableCopyTo(output)
            }
        }
        return file
    }

    override fun deleteItemDir(itemId: UUID) {
        File(cacheDir, itemId.toString()).deleteRecursively()
    }

    override fun deleteCacheDir() {
        cacheDir.deleteRecursively()
    }
}
