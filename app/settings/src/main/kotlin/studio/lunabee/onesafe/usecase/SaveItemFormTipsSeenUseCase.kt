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

import studio.lunabee.onesafe.domain.usecase.settings.SetAppVisitUseCase
import javax.inject.Inject

class SaveItemFormTipsSeenUseCase @Inject constructor(
    private val setAppVisitUseCase: SetAppVisitUseCase,
) {
    suspend operator fun invoke(itemFormTips: ItemFormTips) {
        when (itemFormTips) {
            ItemFormTips.Url -> setAppVisitUseCase.setHasSeenItemEditionUrlToolTip()
            ItemFormTips.Emoji -> setAppVisitUseCase.setHasSeenItemEditionEmojiToolTip()
        }
    }
}
