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
 * Created by Lunabee Studio / Date - 2/9/2024 - for the oneSafe6 SDK.
 * Last modified 2/9/24, 1:40 PM
 */

package studio.lunabee.onesafe.migration

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.domain.repository.IconRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.error.OSError
import javax.inject.Inject

private val logger = LBLogger.get<MigrationFromV10ToV11>()

/**
 * Remove all icons not referenced by an item due to UpdateItemUseCase not deleting old icon on icon update
 */
class MigrationFromV10ToV11 @Inject constructor(
    private val safeItemRepository: SafeItemRepository,
    private val iconRepository: IconRepository,
) {
    suspend operator fun invoke(): LBResult<Unit> = OSError.runCatching {
        val allIcons = iconRepository.getIcons()
        val allItemIcons = safeItemRepository.getAllSafeItems().mapNotNull { it.iconId?.toString() }
        val orphanIcons = allIcons.filterNot { it.name in allItemIcons }
        orphanIcons.forEach { it.delete() }
        logger.i("Found & removed ${orphanIcons.size} orphan icons")
    }
}
