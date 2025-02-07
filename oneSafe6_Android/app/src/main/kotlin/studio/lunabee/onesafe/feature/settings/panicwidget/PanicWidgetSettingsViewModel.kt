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
 * Created by Lunabee Studio / Date - 9/26/2024 - for the oneSafe6 SDK.
 * Last modified 26/09/2024 17:24
 */

package studio.lunabee.onesafe.feature.settings.panicwidget

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
import studio.lunabee.onesafe.domain.usecase.panicmode.AddPanicWidgetToHomeScreenUseCase
import studio.lunabee.onesafe.domain.usecase.panicmode.IsPanicDestructionEnabledUseCase
import studio.lunabee.onesafe.domain.usecase.panicmode.IsPanicWidgetInstalledUseCase
import studio.lunabee.onesafe.domain.usecase.panicmode.SetIsPanicDestructionEnabledUseCase
import studio.lunabee.onesafe.feature.settings.panicwidget.model.PanicWidgetSettingsUiState
import studio.lunabee.onesafe.importexport.usecase.GetAutoBackupSettingUseCase
import javax.inject.Inject

@HiltViewModel
class PanicWidgetSettingsViewModel @Inject constructor(
    getAutoBackupSettingUseCase: GetAutoBackupSettingUseCase,
    isPanicDestructionEnabledUseCase: IsPanicDestructionEnabledUseCase,
    private val setPanicDestructionEnabledUseCase: SetIsPanicDestructionEnabledUseCase,
    private val addPanicWidgetToHomeScreenUseCase: AddPanicWidgetToHomeScreenUseCase,
    private val isPanicWidgetInstalledUseCase: IsPanicWidgetInstalledUseCase,
) : ViewModel() {
    private val isPanicWidgetInstalled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val isExit: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val uiState: StateFlow<PanicWidgetSettingsUiState> = combine(
        getAutoBackupSettingUseCase.cloudBackupEnabled().take(1),
        isPanicDestructionEnabledUseCase(),
        isPanicWidgetInstalled,
        isExit,
    ) { isBackupEnabled, isPanicDestructionEnabled, isPanicWidgetInstalled, isExit ->
        PanicWidgetSettingsUiState(
            isAutoBackupEnabled = isBackupEnabled,
            isWidgetEnabled = !addPanicWidgetToHomeScreenUseCase.isSupported() || isPanicWidgetInstalled,
            isPanicDestructionEnabled = isPanicDestructionEnabled,
            isExit = isExit,
        )
    }.stateIn(
        viewModelScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        PanicWidgetSettingsUiState(
            isWidgetEnabled = false,
            isPanicDestructionEnabled = false,
            isAutoBackupEnabled = false,
            isExit = isExit.value,
        ),
    )

    fun togglePanicDestruction(value: Boolean) {
        viewModelScope.launch {
            setPanicDestructionEnabledUseCase(value)
            isExit.value = true
        }
    }

    fun pinWidgetToHomeScreen() {
        viewModelScope.launch {
            addPanicWidgetToHomeScreenUseCase.invoke()
            togglePanicDestruction(value = true)
        }
    }

    fun updatePanicEnabledWidgetState() {
        viewModelScope.launch {
            isPanicWidgetInstalled.value = isPanicWidgetInstalledUseCase()
        }
    }
}
