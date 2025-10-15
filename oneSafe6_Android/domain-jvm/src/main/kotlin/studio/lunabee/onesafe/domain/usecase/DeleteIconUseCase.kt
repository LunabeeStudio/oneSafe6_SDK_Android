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
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.repository.IconRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.error.OSError
import java.util.UUID
import javax.inject.Inject

private val log = LBLogger.get<DeleteIconUseCase>()

/**
 * Delete the icon from a [SafeItem] and clean icon file
 */
class DeleteIconUseCase @Inject constructor(
    private val safeItemRepository: SafeItemRepository,
    private val iconRepository: IconRepository,
) {
    /**
     * @param safeItems The list of [SafeItem] whose icon will be removed
     */
    suspend operator fun invoke(
        safeItems: List<SafeItem>,
    ): LBResult<Unit> = safeItems
        .map {
            invoke(it)
        }.firstOrNull { result ->
            result is LBResult.Failure
        } ?: LBResult.Success(Unit)

    /**
     * @param safeItem The [SafeItem] which icon will be removed
     */
    suspend operator fun invoke(
        safeItem: SafeItem,
    ): LBResult<Unit> = OSError.runCatching(log) {
        if (safeItem.iconId == null) throw OSDomainError(OSDomainError.Code.SAFE_ITEM_NO_ICON)
        safeItemRepository.updateIcon(safeItem.id, null)
        invoke(safeItem.iconId)
    }

    /**
     * @param iconId The icon id to delete
     */
    internal suspend operator fun invoke(
        iconId: UUID,
    ): LBResult<Unit> = OSError.runCatching(log) {
        val deleted = iconRepository.deleteIcon(iconId)
        if (!deleted) {
            log.e("Unable to delete icon $iconId")
        }
    }
}
