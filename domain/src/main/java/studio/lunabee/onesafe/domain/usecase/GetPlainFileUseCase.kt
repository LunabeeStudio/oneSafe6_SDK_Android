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
 * Created by Lunabee Studio / Date - 10/2/2023 - for the oneSafe6 SDK.
 * Last modified 02/10/2023 13:31
 */

package studio.lunabee.onesafe.domain.usecase

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.domain.Constant
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.repository.FileRepository
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.error.OSError
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject

private val log = LBLogger.get<GetPlainFileUseCase>()

class GetPlainFileUseCase @Inject constructor(
    private val cryptoRepository: MainCryptoRepository,
    private val safeItemKeyRepository: SafeItemKeyRepository,
    private val fileRepository: FileRepository,
    private val decryptUseCase: ItemDecryptUseCase,
) {

    /**
     * Get and decrypt a file
     *
     * @param field The field containing the file to decrypt
     *
     * @return Plain data wrapped in a [LBResult]
     */
    suspend operator fun invoke(field: SafeItemField): LBResult<File> {
        return OSError.runCatching(log) {
            val key = safeItemKeyRepository.getSafeItemKey(field.itemId)
            val name = field.encName?.let { decryptUseCase(it, field.itemId, String::class).data }
                ?: field.id.toString() // fallback to field id if the name is null
            val existingFile = fileRepository.getPlainFile(itemId = field.itemId, fieldId = field.id, filename = name)
            if (existingFile.exists()) {
                existingFile
            } else {
                val encFileId = field.encValue
                    ?: throw OSDomainError(OSDomainError.Code.MISSING_FILE_ID_IN_FIELD)
                val plainFileResult = decryptUseCase(encFileId, key, String::class)
                when (plainFileResult) {
                    is LBResult.Failure -> return LBResult.Failure(plainFileResult.throwable)
                    is LBResult.Success -> {
                        val fileId = plainFileResult.successData.substringBefore(Constant.FileTypeExtSeparator)
                        val file = fileRepository.getFile(fileId = fileId)
                        try {
                            fileRepository.savePlainFile(
                                inputStream = cryptoRepository.getDecryptStream(cipherFile = file, key = key),
                                filename = name,
                                itemId = field.itemId,
                                fieldId = field.id,
                            )
                        } catch (e: FileNotFoundException) {
                            throw OSCryptoError(code = OSCryptoError.Code.DECRYPTION_FILE_NOT_FOUND, cause = e)
                        }
                    }
                }
            }
        }
    }
}
