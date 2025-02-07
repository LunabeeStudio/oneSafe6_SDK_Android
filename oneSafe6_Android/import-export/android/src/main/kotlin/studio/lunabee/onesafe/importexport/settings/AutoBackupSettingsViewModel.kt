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

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.snackbar.ErrorSnackbarState
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.error.OSDriveError
import studio.lunabee.onesafe.importexport.GoogleDriveHelper
import studio.lunabee.onesafe.importexport.dialog.ConfirmDeleteLocalBackupsDialogState
import studio.lunabee.onesafe.importexport.model.CloudInfo
import studio.lunabee.onesafe.importexport.model.LatestBackups
import studio.lunabee.onesafe.importexport.settings.backupnumber.AutoBackupMaxNumber
import studio.lunabee.onesafe.importexport.usecase.DeleteCloudBackupsLocallyUseCase
import studio.lunabee.onesafe.importexport.usecase.GetAutoBackupSettingUseCase
import studio.lunabee.onesafe.importexport.usecase.GetCloudInfoUseCase
import studio.lunabee.onesafe.importexport.usecase.GetLatestBackupUseCase
import studio.lunabee.onesafe.importexport.usecase.HasBackupUseCase
import studio.lunabee.onesafe.importexport.usecase.OpenAndroidInternalBackupStorageUseCase
import studio.lunabee.onesafe.importexport.usecase.SetAutoBackupSettingUseCase
import studio.lunabee.onesafe.importexport.usecase.SetKeepLocalBackupUseCase
import studio.lunabee.onesafe.importexport.usecase.SetupAndSyncCloudBackupUseCase
import studio.lunabee.onesafe.importexport.usecase.UpdateAutoBackUpsMaxNumberUseCase
import studio.lunabee.onesafe.importexport.worker.AutoBackupWorkersHelper
import studio.lunabee.onesafe.model.OSSwitchState
import javax.inject.Inject
import kotlin.time.Duration

private val logger = LBLogger.get<AutoBackupSettingsViewModel>()

@HiltViewModel
class AutoBackupSettingsViewModel @Inject constructor(
    private val getSettings: GetAutoBackupSettingUseCase,
    private val setSettings: SetAutoBackupSettingUseCase,
    private val getLatestBackupUseCase: GetLatestBackupUseCase,
    private val getCloudInfoUseCase: GetCloudInfoUseCase,
    private val autoBackupWorkersHelper: AutoBackupWorkersHelper,
    featureFlags: FeatureFlags,
    hasBackupUseCase: HasBackupUseCase,
    private val setKeepLocalBackupUseCase: SetKeepLocalBackupUseCase,
    private val updateAutoBackUpsMaxNumberUseCase: UpdateAutoBackUpsMaxNumberUseCase,
    private val deleteCloudBackupsLocallyUseCase: DeleteCloudBackupsLocallyUseCase,
    private val setupCloudBackupUseCase: SetupAndSyncCloudBackupUseCase,
    private val openInternalBackupStorageUseCase: OpenAndroidInternalBackupStorageUseCase,
) : ViewModel() {
    val featureFlagCloudBackup: Boolean = featureFlags.cloudBackup()

    private val cloudLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val cloudBackupEnabledState = combine(
        getSettings.cloudBackupEnabled(),
        cloudLoading,
    ) { cloudBackupEnabled, cloudLoading ->
        when {
            cloudLoading -> OSSwitchState.Loading
            cloudBackupEnabled -> OSSwitchState.True
            else -> OSSwitchState.False
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AutoBackupSettingsUiState?> = getSettings.autoBackupEnabled().flatMapLatest { enabled ->
        if (enabled) {
            combine(
                getSettings.autoBackupFrequencyFlow(),
                getLatestBackupUseCase.flow(),
                cloudBackupEnabledState,
                getSettings.keepLocalBackupEnabled(),
                getCloudInfoUseCase.current(),
                hasBackupUseCase(),
                getSettings.autoBackupMaxNumberFlow(),
            ) { values ->
                val frequency = values[0] as Duration
                val latestBackups = values[1] as LatestBackups?
                val cloudBackupEnabledState = values[2] as OSSwitchState
                val isKeepLocalBackupEnabled = values[3] as Boolean
                val cloudInfo = values[4] as CloudInfo
                val hasBackup = values[5] as Boolean
                val autoBackupMaxNumber = values[6] as Int
                AutoBackupSettingsUiState(
                    isAutoBackupEnabled = true,
                    autoBackupFrequency = AutoBackupFrequency.valueForDuration(frequency),
                    autoBackupMaxNumber = AutoBackupMaxNumber.valueForInt(autoBackupMaxNumber),
                    latestBackups = latestBackups,
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

    fun toggleAutoBackupSetting() {
        viewModelScope.launch {
            val isAutoBackupEnabled = setSettings.toggleAutoBackupSettings()
            when (isAutoBackupEnabled) {
                is LBResult.Failure -> {
                    _snackbarState.value = ErrorSnackbarState(isAutoBackupEnabled.throwable, ::dismissSnackbar)
                }
                is LBResult.Success -> {
                    if (isAutoBackupEnabled.successData) {
                        autoBackupWorkersHelper.start(synchronizeCloudFirst = false)
                    } else {
                        autoBackupWorkersHelper.cancel()
                    }
                }
            }
        }
    }

    fun setAutoBackupFrequency(frequency: AutoBackupFrequency) {
        viewModelScope.launch {
            setSettings.setAutoBackupFrequency(frequency.repeat)
            autoBackupWorkersHelper.start(synchronizeCloudFirst = false)
        }
    }

    fun setAutoBackupMaxNumber(frequency: AutoBackupMaxNumber) {
        // We don't care about the result
        updateAutoBackUpsMaxNumberUseCase(frequency.value).launchIn(viewModelScope)
    }

    fun setupCloudBackupAndSync(accountName: String) {
        viewModelScope.launch {
            setupCloudBackupUseCase(accountName).collect { result ->
                when (result) {
                    is LBFlowResult.Loading -> {
                        cloudLoading.value = true
                    }
                    is LBFlowResult.Failure -> {
                        val error = result.throwable as? OSDriveError
                        if (error?.code == OSDriveError.Code.DRIVE_AUTHENTICATION_REQUIRED) {
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
        setSettings.setCloudBackupEnabled(true)
        autoBackupWorkersHelper.start(synchronizeCloudFirst = true)
    }

    fun disableCloudBackupSettings() {
        viewModelScope.launch {
            setSettings.setCloudBackupEnabled(false)
            deleteCloudBackupsLocallyUseCase()
        }
    }

    private fun dismissSnackbar() {
        _snackbarState.value = null
    }

    fun showError(errorMessage: LbcTextSpec) {
        _snackbarState.value = ErrorSnackbarState(errorMessage, ::dismissSnackbar)
    }

    fun openInternalBackupStorage(context: Context) {
        if (!openInternalBackupStorageUseCase(context)) {
            showError(LbcTextSpec.StringResource(OSString.common_error_noFileManager))
        }
    }
}
