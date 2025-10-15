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
import studio.lunabee.onesafe.domain.common.IconIdProvider
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.repository.IconRepository
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.error.OSError
import java.util.UUID
import javax.inject.Inject

/**
 * Add an icon to a [SafeItem]
 */
class SetIconUseCase @Inject constructor(
    private val cryptoRepository: MainCryptoRepository,
    private val safeItemKeyRepository: SafeItemKeyRepository,
    private val safeItemRepository: SafeItemRepository,
    private val iconRepository: IconRepository,
    private val deleteIconUseCase: DeleteIconUseCase,
    private val resizeIconUseCase: ResizeIconUseCase,
    private val idProvider: IconIdProvider,
) {
    /**
     * @param safeItem The [SafeItem] whose icon will be added
     * @param icon The raw icon to be set
     */
    suspend operator fun invoke(
        safeItem: SafeItem,
        icon: ByteArray,
    ): LBResult<Unit> = OSError.runCatching {
        val itemKey = safeItemKeyRepository.getSafeItemKey(safeItem.id)

        if (safeItem.iconId != null) {
            deleteIconUseCase(safeItem)
        }

        val iconId = invoke(itemKey, icon, safeItem.safeId)
        safeItemRepository.updateIcon(safeItem.id, iconId)
    }

    /**
     * Internal fun to avoid [SafeItemKey] fetch when chaining use case on same [SafeItem]
     * ⚠️ It's the caller responsibility to delete the old icon
     */
    internal suspend operator fun invoke(
        itemKey: SafeItemKey,
        icon: ByteArray,
        safeId: SafeId,
    ): UUID {
        val iconId = idProvider()
        val resizedIcon = resizeIconUseCase(icon)
        val encData: ByteArray = cryptoRepository.encrypt(itemKey, EncryptEntry(resizedIcon))
        iconRepository.addIcon(iconId, encData, safeId)
        return iconId
    }
}
