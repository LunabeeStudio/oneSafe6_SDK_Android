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
 * Created by Lunabee Studio / Date - 5/17/2023 - for the oneSafe6 SDK.
 * Last modified 5/17/23, 4:23 PM
 */

package studio.lunabee.onesafe.domain.usecase.item

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemEditInfo
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.error.OSError
import java.util.UUID
import javax.inject.Inject

private val log = LBLogger.get<GetItemEditionInfoUseCase>()

class GetItemEditionInfoUseCase @Inject constructor(
    private val safeItemFieldRepository: SafeItemFieldRepository,
    private val safeItemRepository: SafeItemRepository,
) {
    suspend operator fun invoke(itemId: UUID): LBResult<SafeItemEditInfo> = OSError.runCatching(log) {
        val safeItem = safeItemRepository.getSafeItem(id = itemId)
        val encSafeItemName = safeItem.encName
        val encSafeIconId = safeItem.iconId
        val encSafeItemFields = safeItemFieldRepository.getSafeItemFields(itemId = itemId)
        val encColor = safeItem.encColor
        SafeItemEditInfo(
            id = itemId,
            encName = encSafeItemName,
            iconId = encSafeIconId,
            encSafeItemFields = encSafeItemFields,
            encColor = encColor,
        )
    }
}
