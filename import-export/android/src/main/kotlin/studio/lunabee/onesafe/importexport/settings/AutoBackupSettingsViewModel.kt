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
 * Created by Lunabee Studio / Date - 10/17/2023 - for the oneSafe6 SDK.
 * Last modified 10/17/23, 3:41 PM
 */

package studio.lunabee.onesafe.importexport.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.importexport.repository.datasource.CloudBackupEngine
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.snackbar.ErrorSnackbarState
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.error.OSDriveError
import studio.lunabee.onesafe.importexport.GoogleDriveHelper
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import studio.lunabee.onesafe.importexport.repository.CloudBackupRepository
import studio.lunabee.onesafe.importexport.usecase.GetLatestBackupUseCase
import studio.lunabee.onesafe.importexport.worker.AutoBackupWorkersHelper
import javax.inject.Inject

@HiltViewModel
class AutoBackupSettingsViewModel @Inject constructor(
    private val settings: AutoBackupSettingsRepository,
    private val getLatestBackupUseCase: GetLatestBackupUseCase,
    private val cloudBackupEngine: CloudBackupEngine,
    private val cloudBackupRepository: CloudBackupRepository,
    private val autoBackupWorkersHelper: AutoBackupWorkersHelper,
    featureFlags: FeatureFlags,
) : ViewModel() {
    val featureFlagCloudBackup: Boolean = featureFlags.cloudBackup()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AutoBackupSettingsUiState?> = settings.autoBackupEnabled.flatMapLatest { enabled ->
        if (enabled) {
            combine(
                settings.autoBackupFrequencyFlow,
                getLatestBackupUseCase.flow(),
                settings.cloudBackupEnabled,
                settings.keepLocalBackupEnabled,
                cloudBackupEngine.getCloudInfo(),
            ) { frequency, backup, cloudBackupEnabled, keepLocalBackupEnabled, cloudInfo ->
                AutoBackupSettingsUiState(
                    isBackupEnabled = true,
                    autoBackupFrequency = AutoBackupFrequency.valueForDuration(frequency),
                    latestBackup = backup,
                    isCloudBackupEnabled = cloudBackupEnabled,
                    isKeepLocalBackupEnabled = keepLocalBackupEnabled,
                    toggleKeepLocalBackup = {
                        viewModelScope.launch {
                            val keepLocalBackup = settings.toggleKeepLocalBackupSettings()
                            if (keepLocalBackup) {
                                autoBackupWorkersHelper.start(synchronizeCloudFirst = false)
                            }
                        }
                    },
                    driveUri = cloudInfo.folderURI,
                    driveAccount = cloudInfo.driveAccount,
                )
            }
        } else {
            flowOf(AutoBackupSettingsUiState.disabled())
        }
    }.stateIn(
        viewModelScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        null,
    )

    private val _snackbarState: MutableStateFlow<SnackbarState?> = MutableStateFlow(null)
    val snackbarState: StateFlow<SnackbarState?> = _snackbarState.asStateFlow()

    private val _authorizeDrive: MutableStateFlow<AutoBackupSettingsDriveAuth?> = MutableStateFlow(null)
    val authorizeDrive: StateFlow<AutoBackupSettingsDriveAuth?> = _authorizeDrive.asStateFlow()

    fun toggleAutoBackupSetting(): Boolean {
        val isAutoBackupEnabled = settings.toggleAutoBackupSettings()
        if (isAutoBackupEnabled) {
            autoBackupWorkersHelper.start(synchronizeCloudFirst = false)
        } else {
            autoBackupWorkersHelper.cancel()
        }
        return isAutoBackupEnabled
    }

    fun setAutoBackupFrequency(frequency: AutoBackupFrequency) {
        settings.setAutoBackupFrequency(frequency.repeat)
        autoBackupWorkersHelper.start(synchronizeCloudFirst = false)
    }

    fun setupCloudBackupAndSync(accountName: String) {
        viewModelScope.launch {
            cloudBackupEngine.setupAccount(accountName)
            cloudBackupRepository.refreshBackupList().collect { result ->
                when (result) {
                    is LBFlowResult.Loading -> {
                        // TODO loading
                    }
                    is LBFlowResult.Failure -> {
                        val error = result.throwable as? OSDriveError
                        if (error?.code == OSDriveError.Code.AUTHENTICATION_REQUIRED) {
                            val authIntentRes = GoogleDriveHelper.getAuthorizationIntent(error)
                            when (authIntentRes) {
                                is LBResult.Failure -> _snackbarState.value = ErrorSnackbarState(result.throwable, ::dismissSnackbar)
                                is LBResult.Success -> {
                                    _authorizeDrive.value = AutoBackupSettingsDriveAuth(
                                        authorizeIntent = authIntentRes.successData,
                                        onAuthorize = { isAuthorized ->
                                            if (isAuthorized) {
                                                viewModelScope.launch {
                                                    finalizeCloudBackupEnable()
                                                }
                                            }
                                        },
                                    )
                                }
                            }
                        } else {
                            _snackbarState.value = ErrorSnackbarState(result.throwable, ::dismissSnackbar)
                        }
                    }
                    is LBFlowResult.Success -> {
                        finalizeCloudBackupEnable()
                    }
                }
            }
        }
    }

    private suspend fun finalizeCloudBackupEnable() {
        settings.setCloudBackupSettings(true)
        autoBackupWorkersHelper.start(synchronizeCloudFirst = true)
    }

    fun disableCloudBackupSettings() {
        viewModelScope.launch {
            settings.setCloudBackupSettings(false)
        }
    }

    private fun dismissSnackbar() {
        _snackbarState.value = null
    }

    fun showError(errorMessage: LbcTextSpec) {
        _snackbarState.value = ErrorSnackbarState(errorMessage, ::dismissSnackbar)
    }
}
