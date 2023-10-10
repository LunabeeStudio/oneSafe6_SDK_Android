/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 10/3/2023 - for the oneSafe6 SDK.
 * Last modified 10/3/23, 1:25 PM
 */

package studio.lunabee.onesafe.importexport.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import studio.lunabee.onesafe.importexport.usecase.GetBackupsUseCase
import studio.lunabee.onesafe.importexport.usecase.IsLatestBackupOutdatedUseCase
import studio.lunabee.onesafe.importexport.worker.AutoBackupWorker
import javax.inject.Inject

@HiltViewModel
class AutoBackupSettingsViewModel @Inject constructor(
    private val settings: AutoBackupSettingsRepository,
    private val getBackupsUseCase: GetBackupsUseCase,
    private val isLatestBackupOutdatedUseCase: IsLatestBackupOutdatedUseCase,
) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AutoBackupSettingsUiState?> = settings.autoBackupEnabled.flatMapLatest { enabled ->
        if (enabled) {
            combine(settings.autoBackupFrequencyFlow, getBackupsUseCase.flow()) { frequency, backups ->
                AutoBackupSettingsUiState.Enabled(
                    autoBackupFrequency = AutoBackupFrequency.valueForDuration(frequency),
                    backups = backups,
                )
            }
        } else {
            flowOf(
                AutoBackupSettingsUiState.Disabled,
            )
        }
    }.stateIn(
        viewModelScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        null,
    )

    fun toggleAutoBackupSetting(context: Context): Boolean {
        val isAutoBackupEnabled = settings.toggleAutoBackupSettings()
        if (isAutoBackupEnabled) {
            val autoBackupFrequency = settings.autoBackupFrequency
            AutoBackupWorker.schedule(
                context = context,
                frequency = AutoBackupFrequency.valueForDuration(autoBackupFrequency),
            )
            if (isLatestBackupOutdatedUseCase()) {
                AutoBackupWorker.start(context)
            }
        } else {
            AutoBackupWorker.cancel(context)
        }
        return isAutoBackupEnabled
    }

    fun setAutoBackupFrequency(context: Context, frequency: AutoBackupFrequency) {
        settings.setAutoBackupFrequency(frequency.repeat)
        AutoBackupWorker.schedule(
            context = context,
            frequency = frequency,
        )
        if (isLatestBackupOutdatedUseCase()) {
            AutoBackupWorker.start(context)
        }
    }
}
