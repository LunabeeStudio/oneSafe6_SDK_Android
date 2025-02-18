package studio.lunabee.onesafe.feature.settings.overencryption

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbloading.LoadingManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.domain.model.crypto.DatabaseKey
import studio.lunabee.onesafe.domain.usecase.authentication.StartSetupDatabaseEncryptionUseCase
import studio.lunabee.onesafe.feature.clipboard.ClipboardDelegate
import studio.lunabee.onesafe.feature.clipboard.ClipboardDelegateImpl
import studio.lunabee.onesafe.importexport.usecase.BackupAllSafesUseCase
import studio.lunabee.onesafe.jvm.use

@AssistedFactory
interface OverEncryptionKeyViewModelFactory {
    fun create(
        // FIXME Dagger hilt does not support value class (DatabaseKey) https://github.com/google/dagger/issues/2930
        databaseKey: ByteArray,
    ): OverEncryptionKeyViewModel
}

@HiltViewModel(assistedFactory = OverEncryptionKeyViewModelFactory::class)
class OverEncryptionKeyViewModel @AssistedInject constructor(
    private val startSetupDatabaseEncryptionUseCase: StartSetupDatabaseEncryptionUseCase,
    clipboardDelegate: ClipboardDelegateImpl,
    private val loadingManager: LoadingManager,
    private val backupAllSafesUseCase: BackupAllSafesUseCase,
    savedStateHandle: SavedStateHandle,
    @Assisted rawKey: ByteArray,
) : ViewModel(), ClipboardDelegate by clipboardDelegate {
    private val doBackup: Boolean = savedStateHandle.get<Boolean>(OverEncryptionKeyDestination.doBackupArg)!!
    private val databaseKey: DatabaseKey = DatabaseKey(rawKey)

    private val key: String
        get() = databaseKey.asCharArray().use { key ->
            DatabaseKey.hexPrefix + key.joinToString("")
        }

    private val _uiState: MutableStateFlow<OverEncryptionKeyUiState> = MutableStateFlow(OverEncryptionKeyUiState.Idle(key = key))
    val uiState: StateFlow<OverEncryptionKeyUiState> = _uiState.asStateFlow()

    fun enabledOverEncryption() {
        viewModelScope.launch {
            loadingManager.withBlocking {
                if (doBackup) {
                    executeBackup {
                        startEncryptDatabase()
                    }
                } else {
                    startEncryptDatabase()
                }
            }
        }
    }

    private suspend fun executeBackup(onSuccess: suspend () -> Unit) {
        _uiState.value = OverEncryptionKeyUiState.Loading(OverEncryptionKeyUiState.Loading.Step.Backup)
        val backupResult = backupAllSafesUseCase().last().asResult()
        when (backupResult) {
            is LBResult.Failure -> _uiState.value = OverEncryptionKeyUiState.Error(backupResult.throwable)
            is LBResult.Success -> onSuccess()
        }
    }

    private suspend fun startEncryptDatabase() {
        _uiState.value = OverEncryptionKeyUiState.Loading(OverEncryptionKeyUiState.Loading.Step.Encryption)
        val result = startSetupDatabaseEncryptionUseCase(key = databaseKey)
        when (result) {
            is LBResult.Success -> _uiState.value = OverEncryptionKeyUiState.Done
            is LBResult.Failure -> _uiState.value = OverEncryptionKeyUiState.Error(result.throwable)
        }
    }
}
