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
 * Last modified 28/09/2023 09:51
 */

package studio.lunabee.onesafe.domain.repository

import java.io.File
import java.io.InputStream
import java.util.UUID

interface FileRepository {
    fun getPlainFile(itemId: UUID, fieldId: UUID, filename: String): File
    fun getFile(fileId: String): File
    fun createTempFile(fileId: String): File
    fun getFiles(filesId: List<String>): List<File>
    fun addFile(fileId: UUID, file: ByteArray): File
    fun deleteFile(fileId: UUID): Boolean
    fun getFiles(): List<File>
    fun copyAndDeleteFile(file: File, fileId: UUID)
    fun deleteItemDir(itemId: UUID)
    fun deleteCacheDir()
    suspend fun savePlainFile(inputStream: InputStream, filename: String, itemId: UUID, fieldId: UUID): File
    fun deleteAll()
}
