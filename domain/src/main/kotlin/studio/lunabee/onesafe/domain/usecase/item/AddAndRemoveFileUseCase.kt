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
 * Created by Lunabee Studio / Date - 10/12/2023 - for the oneSafe6 SDK.
 * Last modified 12/10/2023 15:07
 */

package studio.lunabee.onesafe.domain.usecase.item

import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.model.safeitem.FileSavingData
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.domain.repository.FileRepository
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.error.OSError
import java.io.FileNotFoundException
import java.util.UUID
import javax.inject.Inject

class AddAndRemoveFileUseCase @Inject constructor(
    private val safeItemKeyRepository: SafeItemKeyRepository,
    private val cryptoRepository: MainCryptoRepository,
    private val fileRepository: FileRepository,
    @FileDispatcher private val dispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        itemId: UUID,
        fileSavingData: List<FileSavingData>,
    ): LBResult<Unit> = OSError.runCatching {
        val key = safeItemKeyRepository.getSafeItemKey(itemId)
        fileSavingData.forEach { data ->
            when (data) {
                is FileSavingData.ToRemove -> {
                    data.encThumbnailFileName?.let {
                        val fileName = cryptoRepository.decrypt(key, DecryptEntry(data.encThumbnailFileName, UUID::class)).toString()
                        fileRepository.getThumbnailFile(fileName, isFullWidth = true).delete()
                        fileRepository.getThumbnailFile(fileName, isFullWidth = false).delete()
                    }
                    fileRepository.deleteFile(data.fileId)
                }
                is FileSavingData.ToSave -> {
                    withContext(dispatcher) {
                        val file = fileRepository.getFile(data.fileId.toString())
                        file.parentFile.mkdirs()
                        cryptoRepository.getEncryptStream(file, key).use { outputStream ->
                            try {
                                val stream = data.getStream()
                                stream?.use { it.copyTo(outputStream) }
                            } catch (e: FileNotFoundException) {
                                throw OSDomainError(OSDomainError.Code.MISSING_URI_OUTPUT_STREAM)
                            }
                        }
                    }
                }
            }
        }
    }
}
