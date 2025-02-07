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
 * Created by Lunabee Studio / Date - 9/11/2024 - for the oneSafe6 SDK.
 * Last modified 11/09/2024 14:24
 */

package studio.lunabee.onesafe.feature.settings.autodestruction.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.domain.usecase.autodestruction.DisabledAutoDestructionUseCase
import studio.lunabee.onesafe.domain.usecase.autodestruction.IsAutoDestructionEnabledUseCase
import studio.lunabee.onesafe.feature.settings.autodestruction.onboarding.model.AutoDestructionOnBoardingUiState
import studio.lunabee.onesafe.importexport.usecase.GetAutoBackupSettingUseCase
import javax.inject.Inject

@HiltViewModel
class AutoDestructionOnBoardingViewModel @Inject constructor(
    getAutoBackupSettingUseCase: GetAutoBackupSettingUseCase,
    isAutoDestructionEnabledUseCase: IsAutoDestructionEnabledUseCase,
    private val disabledAutoDestructionUseCase: DisabledAutoDestructionUseCase,
) : ViewModel() {
    private val isExit: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val uiState: StateFlow<AutoDestructionOnBoardingUiState> = combine(
        getAutoBackupSettingUseCase.cloudBackupEnabled().take(1),
        isExit,
    ) { isBackupEnabled, isExit ->
        AutoDestructionOnBoardingUiState(
            isAutoBackupEnabled = isBackupEnabled,
            isAutoDestructionEnabled = isAutoDestructionEnabledUseCase(),
            isExit = isExit,
        )
    }.stateIn(
        viewModelScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        AutoDestructionOnBoardingUiState(
            isAutoDestructionEnabled = false,
            isAutoBackupEnabled = false,
            isExit = false,
        ),
    )

    fun disableAutoDestruction() {
        viewModelScope.launch {
            disabledAutoDestructionUseCase()
            isExit.value = true
        }
    }
}
