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
 * Created by Lunabee Studio / Date - 6/20/2024 - for the oneSafe6 SDK.
 * Last modified 6/19/24, 5:21 PM
 */

package studio.lunabee.onesafe.domain.usecase.settings

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.repository.SafeSettingsRepository
import studio.lunabee.onesafe.error.OSError
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

class DismissIndependentSafeInfoCardUseCase @Inject constructor(
    private val settingsRepository: SafeSettingsRepository,
    private val clock: Clock,
    private val safeRepository: SafeRepository,
) {

    suspend operator fun invoke(): LBResult<Unit> = OSError.runCatching {
        val safeId = safeRepository.currentSafeId()
        settingsRepository.setIndependentSafeInfoCtaState(safeId, CtaState.DismissedAt(Instant.now(clock)))
    }
}
