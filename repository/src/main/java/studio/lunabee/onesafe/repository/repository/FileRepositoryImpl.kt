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
 * Last modified 28/09/2023 10:06
 */

package studio.lunabee.onesafe.repository.repository

import studio.lunabee.onesafe.domain.repository.FileRepository
import studio.lunabee.onesafe.repository.datasource.FileLocalDatasource
import java.io.File
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    private val fileLocalDatasource: FileLocalDatasource,
) : FileRepository {
    override fun getFile(fileId: String): File = fileLocalDatasource.getFile(fileId)
    override fun createTempFile(fileId: String): File = fileLocalDatasource.createTempFile(fileId)
    override fun getPlainFile(itemId: UUID, fieldId: UUID, filename: String): File {
        return fileLocalDatasource.getPlainFile(itemId = itemId, fieldId = fieldId, filename = filename)
    }

    override fun getFiles(filesId: List<String>): List<File> = fileLocalDatasource.getFiles(filesId)

    override fun getFiles(): List<File> = fileLocalDatasource.getAllFiles()

    override fun addFile(fileId: UUID, file: ByteArray): File = fileLocalDatasource.addFile(fileId.toString(), file)

    override fun deleteFile(fileId: UUID): Boolean = fileLocalDatasource.deleteFile(fileId.toString())

    override fun copyAndDeleteFile(file: File, fileId: UUID) {
        fileLocalDatasource.copyAndDeleteFile(newFile = file, fileId = fileId)
    }

    override fun deleteItemDir(itemId: UUID) {
        fileLocalDatasource.deleteItemDir(itemId)
    }

    override suspend fun savePlainFile(inputStream: InputStream, filename: String, itemId: UUID, fieldId: UUID): File {
        return fileLocalDatasource.savePlainFile(inputStream, filename, itemId, fieldId)
    }

    override fun deleteCacheDir() {
        fileLocalDatasource.deleteCacheDir()
    }
}
