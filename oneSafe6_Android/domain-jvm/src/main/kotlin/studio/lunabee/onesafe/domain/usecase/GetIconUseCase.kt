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
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:24 AM
 */

package studio.lunabee.onesafe.domain.usecase

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.domain.repository.IconRepository
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSError
import java.io.FileNotFoundException
import java.util.UUID
import javax.inject.Inject

private val log = LBLogger.get<GetIconUseCase>()

/**
 * Decrypt an icon file
 */
class GetIconUseCase @Inject constructor(
    private val cryptoRepository: MainCryptoRepository,
    private val safeItemKeyRepository: SafeItemKeyRepository,
    private val safeItemRepository: SafeItemRepository,
    private val iconRepository: IconRepository,
) {

    /**
     * Get and decrypt the icon
     *
     * @param iconId The encrypted icon ref to decrypt
     * @param itemId The ID of the linked item
     *
     * @return Plain data wrapped in a [LBResult]
     */
    suspend operator fun invoke(iconId: UUID, itemId: UUID): LBResult<ByteArray> {
        return OSError.runCatching(log) {
            val key = safeItemKeyRepository.getSafeItemKey(itemId)
            val iconFile = iconRepository.getIcon(iconId.toString())
            try {
                cryptoRepository.decrypt(iconFile, key, ByteArray::class)
            } catch (e: FileNotFoundException) {
                // Avoid putting the whole item in "corrupt" mode just because icon file is not found.
                safeItemRepository.updateIcon(itemId, null)
                throw OSCryptoError(code = OSCryptoError.Code.DECRYPTION_FILE_NOT_FOUND, cause = e)
            }
        }
    }
}
