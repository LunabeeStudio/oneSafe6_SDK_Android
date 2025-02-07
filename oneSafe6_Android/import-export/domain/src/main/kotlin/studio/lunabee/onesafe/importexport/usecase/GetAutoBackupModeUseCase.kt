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
 * Created by Lunabee Studio / Date - 11/7/2023 - for the oneSafe6 SDK.
 * Last modified 11/7/23, 6:07 PM
 */

package studio.lunabee.onesafe.importexport.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import javax.inject.Inject

class GetAutoBackupModeUseCase @Inject constructor(
    private val settingsRepository: AutoBackupSettingsRepository,
    private val safeRepository: SafeRepository,
) {
    suspend operator fun invoke(safeId: SafeId): AutoBackupMode {
        return if (settingsRepository.autoBackupEnabledFlow(safeId).first()) {
            val cloudBackupEnabled = settingsRepository.cloudBackupEnabled(safeId).first()
            when {
                cloudBackupEnabled && settingsRepository.keepLocalBackupEnabled(safeId).first() -> AutoBackupMode.Synchronized
                cloudBackupEnabled -> AutoBackupMode.CloudOnly
                else -> AutoBackupMode.LocalOnly
            }
        } else {
            AutoBackupMode.Disabled
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun flow(currentSafeId: SafeId? = null): Flow<AutoBackupMode> {
        return currentSafeId?.let { autoBackupModeFlow(currentSafeId) }
            ?: safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
                safeId?.let { autoBackupModeFlow(safeId) } ?: flowOf()
            }
    }

    private fun autoBackupModeFlow(currentSafeId: SafeId) = combine(
        settingsRepository.autoBackupEnabledFlow(currentSafeId),
        settingsRepository.cloudBackupEnabled(currentSafeId),
        settingsRepository.keepLocalBackupEnabled(currentSafeId),
    ) { autoBackupEnabled, cloudBackupEnabled, keepLocalBackupEnabled ->
        if (autoBackupEnabled) {
            when {
                cloudBackupEnabled && keepLocalBackupEnabled -> AutoBackupMode.Synchronized
                cloudBackupEnabled -> AutoBackupMode.CloudOnly
                else -> AutoBackupMode.LocalOnly
            }
        } else {
            AutoBackupMode.Disabled
        }
    }
}
