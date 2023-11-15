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
import studio.lunabee.importexport.repository.datasource.CloudBackupEngine
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.error.OSDriveError
import studio.lunabee.onesafe.importexport.GoogleDriveHelper
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import studio.lunabee.onesafe.importexport.repository.CloudBackupRepository
import studio.lunabee.onesafe.importexport.usecase.GetLocalBackupsUseCase
import studio.lunabee.onesafe.importexport.worker.AutoBackupSchedulerWorker
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AutoBackupSettingsViewModel @Inject constructor(
    private val settings: AutoBackupSettingsRepository,
    private val getBackupsUseCase: GetLocalBackupsUseCase, // TODO <AutoBackup> also get cloud backups depending on backup mode
    private val cloudBackupEngine: CloudBackupEngine,
    private val cloudBackupRepository: CloudBackupRepository,
    featureFlags: FeatureFlags,
) : ViewModel() {
    val featureFlagCloudBackup: Boolean = featureFlags.cloudBackup()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AutoBackupSettingsUiState?> = settings.autoBackupEnabled.flatMapLatest { enabled ->
        if (enabled) {
            combine(
                settings.autoBackupFrequencyFlow,
                getBackupsUseCase.flow(),
                settings.cloudBackupEnabled,
                settings.keepLocalBackupEnabled,
                cloudBackupEngine.getCloudInfo(),
            ) { frequency, backups, cloudBackupEnabled, keepLocalBackupEnabled, cloudInfo ->
                AutoBackupSettingsUiState(
                    isBackupEnabled = true,
                    autoBackupFrequency = AutoBackupFrequency.valueForDuration(frequency),
                    backups = backups,
                    isCloudBackupEnabled = cloudBackupEnabled,
                    isKeepLocalBackupEnabled = keepLocalBackupEnabled,
                    toggleKeepLocalBackup = { context ->
                        viewModelScope.launch {
                            val keepLocalBackup = settings.toggleKeepLocalBackupSettings()
                            if (keepLocalBackup) {
                                AutoBackupSchedulerWorker.start(context = context, synchronizeCloudFirst = false)
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

    private val _authorizeDrive: MutableStateFlow<AutoBackupSettingsDriveAuth?> = MutableStateFlow(null)
    val authorizeDrive: StateFlow<AutoBackupSettingsDriveAuth?> = _authorizeDrive.asStateFlow()

    fun toggleAutoBackupSetting(context: Context): Boolean {
        val isAutoBackupEnabled = settings.toggleAutoBackupSettings()
        if (isAutoBackupEnabled) {
            AutoBackupSchedulerWorker.start(context = context, synchronizeCloudFirst = false)
        } else {
            AutoBackupSchedulerWorker.cancel(context)
        }
        return isAutoBackupEnabled
    }

    fun setAutoBackupFrequency(context: Context, frequency: AutoBackupFrequency) {
        settings.setAutoBackupFrequency(frequency.repeat)
        AutoBackupSchedulerWorker.start(context = context, synchronizeCloudFirst = false)
    }

    fun setupCloudBackupAndSync(accountName: String, context: Context) {
        viewModelScope.launch {
            cloudBackupEngine.setupAccount(accountName)
            cloudBackupRepository.refreshBackupList().collect { result ->
                Timber.d(result.toString())
                when (result) {
                    is LBFlowResult.Loading -> {
                    }
                    is LBFlowResult.Failure -> {
                        val error = result.throwable as? OSDriveError
                        if (error?.code == OSDriveError.Code.AUTHENTICATION_REQUIRED) {
                            val authIntent = GoogleDriveHelper.getAuthorizationIntent(error)
                            if (authIntent == null) {
                                // TODO <AutoBackup> show error
                            } else {
                                _authorizeDrive.value = AutoBackupSettingsDriveAuth(
                                    authorizeIntent = authIntent,
                                    onAuthorize = { isAuthorized ->
                                        if (isAuthorized) {
                                            viewModelScope.launch {
                                                finalizeCloudBackupEnable(context)
                                            }
                                        }
                                    },
                                )
                            }
                        } else {
                            // TODO <AutoBackup> show error
                        }
                    }
                    is LBFlowResult.Success -> {
                        finalizeCloudBackupEnable(context)
                    }
                }
            }
        }
    }

    private suspend fun finalizeCloudBackupEnable(context: Context) {
        settings.setCloudBackupSettings(true)
        AutoBackupSchedulerWorker.start(context = context, synchronizeCloudFirst = true)
    }

    fun disableCloudBackupSettings() {
        viewModelScope.launch {
            settings.setCloudBackupSettings(false)
        }
    }
}
