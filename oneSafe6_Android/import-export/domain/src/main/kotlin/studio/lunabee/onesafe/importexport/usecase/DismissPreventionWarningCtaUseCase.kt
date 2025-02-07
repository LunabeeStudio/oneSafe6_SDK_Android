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
 * Created by Lunabee Studio / Date - 10/21/2024 - for the oneSafe6 SDK.
 * Last modified 21/10/2024 10:33
 */

package studio.lunabee.onesafe.importexport.usecase

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.repository.SafeSettingsRepository
import studio.lunabee.onesafe.error.OSError
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

private val logger = LBLogger.get<DismissPreventionWarningCtaUseCase>()

class DismissPreventionWarningCtaUseCase @Inject constructor(
    private val settingsRepository: SafeSettingsRepository,
    private val safeRepository: SafeRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        settingsRepository.setPreventionWarningCtaState(safeId, CtaState.DismissedAt(Instant.now(clock)))
    }
}
