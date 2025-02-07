package studio.lunabee.onesafe.feature.settings.overencryption

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbloading.LoadingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.domain.usecase.authentication.StartSetupDatabaseEncryptionUseCase
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.usecase.GetAutoBackupModeUseCase
import studio.lunabee.onesafe.importexport.usecase.BackupAllSafesUseCase
import javax.inject.Inject

@HiltViewModel
class OverEncryptionEnabledViewModel @Inject constructor(
    private val loadingManager: LoadingManager,
    private val backupAllSafesUseCase: BackupAllSafesUseCase,
    private val startSetupDatabaseEncryptionUseCase: StartSetupDatabaseEncryptionUseCase,
    getAutoBackupModeUseCase: GetAutoBackupModeUseCase,
) : ViewModel() {

    private val _uiState: MutableStateFlow<OverEncryptionEnabledUiState> = MutableStateFlow(OverEncryptionEnabledUiState.Idle(false))
    val uiState: StateFlow<OverEncryptionEnabledUiState> = combine(
        _uiState,
        getAutoBackupModeUseCase.flow().map { it != AutoBackupMode.Disabled },
    ) { state, isBackupEnabled ->
        if (state is OverEncryptionEnabledUiState.Idle) {
            OverEncryptionEnabledUiState.Idle(isBackupEnabled)
        } else {
            state
        }
    }.stateIn(
        viewModelScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        OverEncryptionEnabledUiState.Idle(false),
    )

    fun disableOverEncryption(doBackup: Boolean) {
        viewModelScope.launch {
            loadingManager.withBlocking {
                if (doBackup) {
                    executeBackup {
                        startDecryptDatabase()
                    }
                } else {
                    startDecryptDatabase()
                }
            }
        }
    }

    private suspend fun executeBackup(onSuccess: suspend () -> Unit) {
        _uiState.value = OverEncryptionEnabledUiState.Loading(OverEncryptionEnabledUiState.Loading.Step.Backup)
        val backupResult = backupAllSafesUseCase().last().asResult()
        when (backupResult) {
            is LBResult.Failure -> _uiState.value = OverEncryptionEnabledUiState.Error(backupResult.throwable)
            is LBResult.Success -> onSuccess()
        }
    }

    private suspend fun startDecryptDatabase() {
        _uiState.value = OverEncryptionEnabledUiState.Loading(OverEncryptionEnabledUiState.Loading.Step.Decryption)
        val result = startSetupDatabaseEncryptionUseCase(key = null)
        when (result) {
            is LBResult.Success -> _uiState.value = OverEncryptionEnabledUiState.Done
            is LBResult.Failure -> _uiState.value = OverEncryptionEnabledUiState.Error(result.throwable)
        }
    }
}
