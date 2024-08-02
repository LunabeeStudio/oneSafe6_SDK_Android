/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 6/20/2024 - for the oneSafe6 SDK.
 * Last modified 6/20/24, 11:13 AM
 */

package studio.lunabee.onesafe.messaging.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.domain.qualifier.ArchiveCacheDir
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.domain.utils.mkdirs
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class CreateSingleEntryArchiveUseCase @Inject constructor(
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.Message) private val archiveDir: File,
    @FileDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) {

    /**
     * Compresses a byte array and returns the compressed data in a file in [ArchiveCacheDir.Type.Message].
     * @param input [ByteArray] the text to compress
     * @return [File] the compressed data in a file
     */
    suspend operator fun invoke(input: ByteArray): File {
        archiveDir.mkdirs(override = true)
        val fileName = "${UUID.randomUUID()}.zip"
        val file = File(archiveDir, fileName)
        zipBytesStream(file, input)
        return file
    }

    /**
     * Zip a byte array into a single file archive
     *
     * @param target the file to write zipped data
     * @param input the byte array to be compressed
     */
    private suspend fun zipBytesStream(target: File, input: ByteArray) {
        withContext(coroutineDispatcher) {
            val zos = ZipOutputStream(target.outputStream())
            val entry = ZipEntry(target.nameWithoutExtension)
            entry.size = input.size.toLong()
            zos.putNextEntry(entry)
            zos.write(input)
            zos.closeEntry()
            zos.close()
        }
    }
}
