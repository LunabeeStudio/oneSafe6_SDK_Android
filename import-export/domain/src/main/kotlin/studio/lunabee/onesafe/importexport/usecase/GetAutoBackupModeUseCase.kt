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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import javax.inject.Inject

class GetAutoBackupModeUseCase @Inject constructor(
    private val settingsRepository: AutoBackupSettingsRepository,
) {
    suspend operator fun invoke(): AutoBackupMode {
        return if (settingsRepository.autoBackupEnabled.first()) {
            val cloudBackupEnabled = settingsRepository.cloudBackupEnabled.first()
            when {
                cloudBackupEnabled && settingsRepository.keepLocalBackupEnabled.first() -> AutoBackupMode.SYNCHRONIZED
                cloudBackupEnabled -> AutoBackupMode.CLOUD_ONLY
                else -> AutoBackupMode.LOCAL_ONLY
            }
        } else {
            AutoBackupMode.DISABLED
        }
    }

    fun flow(): Flow<AutoBackupMode> {
        return combine(
            settingsRepository.autoBackupEnabled,
            settingsRepository.cloudBackupEnabled,
            settingsRepository.keepLocalBackupEnabled,
        ) { autoBackupEnabled, cloudBackupEnabled, keepLocalBackupEnabled ->
            if (autoBackupEnabled) {
                when {
                    cloudBackupEnabled && keepLocalBackupEnabled -> AutoBackupMode.SYNCHRONIZED
                    cloudBackupEnabled -> AutoBackupMode.CLOUD_ONLY
                    else -> AutoBackupMode.LOCAL_ONLY
                }
            } else {
                AutoBackupMode.DISABLED
            }
        }
    }
}
