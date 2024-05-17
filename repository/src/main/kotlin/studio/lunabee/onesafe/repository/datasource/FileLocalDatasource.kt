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
 * Last modified 28/09/2023 09:53
 */

package studio.lunabee.onesafe.repository.datasource

import java.io.File
import java.io.InputStream
import java.util.UUID

interface FileLocalDatasource {
    fun getPlainFile(itemId: UUID, fieldId: UUID, filename: String): File
    fun getFile(filename: String): File
    fun createTempFile(fileId: String): File
    suspend fun addFile(filename: String, file: ByteArray): File
    fun removeAllFiles()
    fun deleteFile(filename: String): Boolean
    fun getAllFiles(): List<File>
    suspend fun copyAndDeleteFile(newFile: File, fileId: UUID)
    fun getFiles(filesId: List<String>): List<File>
    suspend fun deleteItemDir(itemId: UUID)
    suspend fun deletePlainFilesCacheDir()
    fun getThumbnailFile(thumbnailFileName: String, isFullWidth: Boolean): File
    suspend fun savePlainFile(inputStream: InputStream, filename: String, itemId: UUID, fieldId: UUID): File
}
