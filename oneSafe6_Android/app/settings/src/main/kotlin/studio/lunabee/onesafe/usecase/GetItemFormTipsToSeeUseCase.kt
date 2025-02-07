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
 * Created by Lunabee Studio / Date - 3/18/2024 - for the oneSafe6 SDK.
 * Last modified 18/03/2024 16:34
 */

package studio.lunabee.onesafe.usecase

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.domain.repository.SafeSettingsRepository
import studio.lunabee.onesafe.domain.repository.AppVisitRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.error.OSError
import javax.inject.Inject

/**
 * Return [ItemFormTips] to display to the user.
 * Get all in blocking mode has we do not expect any changes during the screen display.
 * Display url tips first, then emoji tips on the next launch.
 */
class GetItemFormTipsToSeeUseCase @Inject constructor(
    private val appVisitRepository: AppVisitRepository,
    private val appSettingsRepository: SafeSettingsRepository,
    private val safeRepository: SafeRepository,
) {
    suspend operator fun invoke(): LBResult<ItemFormTips?> = OSError.runCatching {
        val safeId = safeRepository.currentSafeId()
        val hasSeenUrlTips: Boolean = appVisitRepository.hasSeenItemEditionUrlToolTip(safeId)
        val hasSeenEmojiTips: Boolean = appVisitRepository.hasSeenItemEditionEmojiToolTip(safeId)
        val shouldSeenUrlTips: Boolean = appSettingsRepository.automation(safeId) && !hasSeenUrlTips
        when {
            shouldSeenUrlTips -> ItemFormTips.Url
            !hasSeenEmojiTips -> ItemFormTips.Emoji
            else -> null
        }
    }
}
