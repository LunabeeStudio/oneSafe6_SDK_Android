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
import com.lunabee.lbcore.model.LBFlowResult.Companion.transformResult
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.importexport.repository.datasource.CloudBackupEngine
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.snackbar.ErrorSnackbarState
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.error.OSDriveError
import studio.lunabee.onesafe.importexport.GoogleDriveHelper
import studio.lunabee.onesafe.importexport.dialog.ConfirmDeleteLocalBackupsDialogState
import studio.lunabee.onesafe.importexport.model.Backup
import studio.lunabee.onesafe.importexport.model.CloudInfo
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import studio.lunabee.onesafe.importexport.repository.CloudBackupRepository
import studio.lunabee.onesafe.importexport.repository.LocalBackupRepository
import studio.lunabee.onesafe.importexport.usecase.GetLatestBackupUseCase
import studio.lunabee.onesafe.importexport.usecase.SetKeepLocalBackupUseCase
import studio.lunabee.onesafe.importexport.worker.AutoBackupWorkersHelper
import studio.lunabee.onesafe.model.OSSwitchState
import javax.inject.Inject
import kotlin.time.Duration

private val logger = LBLogger.get<AutoBackupSettingsViewModel>()

@HiltViewModel
class AutoBackupSettingsViewModel @Inject constructor(
    private val settings: AutoBackupSettingsRepository,
    private val getLatestBackupUseCase: GetLatestBackupUseCase,
    private val cloudBackupEngine: CloudBackupEngine,
    private val cloudBackupRepository: CloudBackupRepository,
    private val autoBackupWorkersHelper: AutoBackupWorkersHelper,
    featureFlags: FeatureFlags,
    private val localBackupRepository: LocalBackupRepository,
    private val setKeepLocalBackupUseCase: SetKeepLocalBackupUseCase,
) : ViewModel() {
    val featureFlagCloudBackup: Boolean = featureFlags.cloudBackup()

    private val cloudLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val cloudBackupEnabledState = settings.cloudBackupEnabled.combine(cloudLoading) { cloudBackupEnabled, cloudLoading ->
        when {
            cloudLoading -> OSSwitchState.Loading
            cloudBackupEnabled -> OSSwitchState.True
            else -> OSSwitchState.False
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AutoBackupSettingsUiState?> = settings.autoBackupEnabled.flatMapLatest { enabled ->
        if (enabled) {
            combine(
                settings.autoBackupFrequencyFlow,
                getLatestBackupUseCase.flow(),
                cloudBackupEnabledState,
                settings.keepLocalBackupEnabled,
                cloudBackupEngine.getCloudInfo(),
                localBackupRepository.hasBackupFlow(),
            ) { values ->
                val frequency = values[0] as Duration
                val backup = values[1] as Backup?
                val cloudBackupEnabledState = values[2] as OSSwitchState
                val isKeepLocalBackupEnabled = values[3] as Boolean
                val cloudInfo = values[4] as CloudInfo
                val hasBackup = values[5] as Boolean
                AutoBackupSettingsUiState(
                    isBackupEnabled = true,
                    autoBackupFrequency = AutoBackupFrequency.valueForDuration(frequency),
                    latestBackup = backup,
                    cloudBackupEnabledState = cloudBackupEnabledState,
                    isKeepLocalBackupEnabled = isKeepLocalBackupEnabled,
                    toggleKeepLocalBackup = { toggleKeepLocalBackup(hasBackup, isKeepLocalBackupEnabled) },
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

    private val _dialogState = MutableStateFlow<DialogState?>(null)
    val dialogState: StateFlow<DialogState?> get() = _dialogState.asStateFlow()

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
            cloudBackupEngine.setupAccount(accountName).transformResult {
                emitAll(cloudBackupRepository.refreshBackupList())
            }.collect { result ->
                when (result) {
                    is LBFlowResult.Loading -> {
                        cloudLoading.value = true
                    }
                    is LBFlowResult.Failure -> {
                        val error = result.throwable as? OSDriveError
                        if (error?.code == OSDriveError.Code.AUTHENTICATION_REQUIRED) {
                            val authIntentRes = GoogleDriveHelper.getAuthorizationIntent(error)
                            when (authIntentRes) {
                                is LBResult.Failure -> {
                                    cloudLoading.value = false
                                    _snackbarState.value = ErrorSnackbarState(result.throwable, ::dismissSnackbar)
                                }
                                is LBResult.Success -> {
                                    _authorizeDrive.value = AutoBackupSettingsDriveAuth(
                                        authorizeIntent = authIntentRes.successData,
                                        onAuthorize = { isAuthorized ->
                                            if (isAuthorized) {
                                                viewModelScope.launch {
                                                    finalizeCloudBackupEnable()
                                                }
                                            } else {
                                                cloudLoading.value = false
                                            }
                                        },
                                    )
                                }
                            }
                        } else {
                            error?.let(logger::e)
                            cloudLoading.value = false
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

    private fun toggleKeepLocalBackup(hasBackup: Boolean, isKeepLocalBackupEnabled: Boolean) {
        if (hasBackup && isKeepLocalBackupEnabled) {
            _dialogState.value = ConfirmDeleteLocalBackupsDialogState(
                confirm = { viewModelScope.launch { setKeepLocalBackupUseCase(false) } },
                dismiss = { _dialogState.value = null },
            )
        } else {
            viewModelScope.launch {
                setKeepLocalBackupUseCase(!isKeepLocalBackupEnabled)
                autoBackupWorkersHelper.start(synchronizeCloudFirst = false)
            }
        }
    }

    private suspend fun finalizeCloudBackupEnable() {
        cloudLoading.value = false
        settings.setCloudBackupSettings(true)
        autoBackupWorkersHelper.start(synchronizeCloudFirst = true)
    }

    fun disableCloudBackupSettings() {
        viewModelScope.launch {
            settings.setCloudBackupSettings(false)
            cloudBackupRepository.clearBackupsLocally()
        }
    }

    private fun dismissSnackbar() {
        _snackbarState.value = null
    }

    fun showError(errorMessage: LbcTextSpec) {
        _snackbarState.value = ErrorSnackbarState(errorMessage, ::dismissSnackbar)
    }
}
