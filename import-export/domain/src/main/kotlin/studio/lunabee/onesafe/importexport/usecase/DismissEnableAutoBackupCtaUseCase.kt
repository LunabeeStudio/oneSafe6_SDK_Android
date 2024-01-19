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
 * Created by Lunabee Studio / Date - 1/12/2024 - for the oneSafe6 SDK.
 * Last modified 1/12/24, 5:35 PM
 */

package studio.lunabee.onesafe.importexport.usecase

import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

class DismissEnableAutoBackupCtaUseCase @Inject constructor(
    private val autoBackupSettingsRepository: AutoBackupSettingsRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke() {
        autoBackupSettingsRepository.setEnableAutoBackupCtaState(CtaState.DismissedAt(Instant.now(clock)))
    }
}
