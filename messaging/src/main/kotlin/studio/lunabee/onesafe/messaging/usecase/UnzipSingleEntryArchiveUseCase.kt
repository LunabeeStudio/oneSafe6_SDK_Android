/*
 * Copyright (c) 2024-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 7/8/2024 - for the oneSafe6 SDK.
 * Last modified 7/8/24, 2:44 PM
 */

package studio.lunabee.onesafe.messaging.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject

class UnzipSingleEntryArchiveUseCase @Inject constructor(
    @FileDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) {

    /**
     * Uncompress the content's file
     *
     * @param inputStream [InputStream] the content of the file to uncompress
     *
     * @return [ByteArray] the content of the file
     */
    suspend operator fun invoke(inputStream: InputStream): ByteArray {
        return withContext(coroutineDispatcher) {
            ZipInputStream(inputStream).use { zipInputStream ->
                zipInputStream.nextEntry
                val bytes = zipInputStream.readBytes()
                zipInputStream.closeEntry()
                bytes
            }
        }
    }
}
