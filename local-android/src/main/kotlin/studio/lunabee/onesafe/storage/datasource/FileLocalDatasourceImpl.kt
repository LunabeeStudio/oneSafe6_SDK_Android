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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.repository.datasource.FileLocalDatasource
import studio.lunabee.onesafe.storage.utils.cancelableCopyTo
import java.io.File
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject

// TODO rework/align cache directories injection to avoid using the app context directly (also see @ImageCacheDirectory)
class FileLocalDatasourceImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    @FileDispatcher private val dispatcher: CoroutineDispatcher,
) : FileLocalDatasource {
    private val encFilesDir: File = File(appContext.filesDir, FILE_DIR)
    private val encThumbnailsCacheDir: File = File(appContext.cacheDir, THUMBNAIL_DIR)
    private val plainFilesCacheDir: File = File(appContext.cacheDir, FILE_DIR).also {
        it.deleteOnExit()
    }

    companion object {
        private const val FILE_DIR: String = "files"
        private const val LARGE_SIZE_DIR = "large"
        private const val SMALL_SIZE_DIR = "small"
        private const val THUMBNAIL_DIR = "thumbnail"
    }

    override fun getPlainFile(itemId: UUID, fieldId: UUID, filename: String): File {
        return File(plainFilesCacheDir, "$itemId/$fieldId/$filename")
    }

    override fun getFile(filename: String): File {
        return File(encFilesDir, filename)
    }

    override fun createTempFile(fileId: String): File {
        if (!plainFilesCacheDir.exists()) {
            plainFilesCacheDir.mkdir()
        }
        return File.createTempFile(fileId, null, plainFilesCacheDir).also {
            it.deleteOnExit()
        }
    }

    override suspend fun addFile(filename: String, file: ByteArray): File = withContext(dispatcher) {
        if (!encFilesDir.exists()) {
            encFilesDir.mkdir()
        }

        val localFile = File(encFilesDir, filename)
        localFile.writeBytes(file)
        localFile
    }

    override fun removeAllFiles() {
        encFilesDir.deleteRecursively()
        encThumbnailsCacheDir.deleteRecursively()
    }

    override fun deleteFile(filename: String): Boolean {
        return File(encFilesDir, filename).delete()
    }

    override fun getAllFiles(): List<File> {
        return encFilesDir.listFiles()?.toList().orEmpty()
    }

    override suspend fun copyAndDeleteFile(newFile: File, fileId: UUID) {
        withContext(dispatcher) {
            newFile.copyTo(target = File(encFilesDir, fileId.toString()))
            newFile.delete()
        }
    }

    override fun getFiles(filesId: List<String>): List<File> {
        return encFilesDir.listFiles()?.filter { filesId.contains(it.name) }.orEmpty()
    }

    /**
     * Save plain file in cacheDir/files/[itemId]/[fieldId]/[filename]
     */
    override suspend fun savePlainFile(
        inputStream: InputStream,
        filename: String,
        itemId: UUID,
        fieldId: UUID,
    ): File = withContext(dispatcher) {
        val file = File(plainFilesCacheDir, "$itemId/$fieldId/$filename")
        file.parentFile?.mkdirs()
        file.deleteOnExit()
        inputStream.use { input ->
            file.outputStream().use { output ->
                input.cancelableCopyTo(output)
            }
        }
        file
    }

    override fun deleteItemDir(itemId: UUID) {
        File(plainFilesCacheDir, itemId.toString()).deleteRecursively()
    }

    override fun deletePlainFilesCacheDir() {
        plainFilesCacheDir.deleteRecursively()
    }

    override fun getThumbnailFile(thumbnailFileName: String, isFullWidth: Boolean): File {
        val size = if (isFullWidth) LARGE_SIZE_DIR else SMALL_SIZE_DIR
        val file = File(encThumbnailsCacheDir, "$size/$thumbnailFileName")
        file.parentFile?.mkdirs()
        return file
    }
}
