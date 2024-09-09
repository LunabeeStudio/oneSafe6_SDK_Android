package studio.lunabee.onesafe.help.lostkey

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbloading.LoadingManager
import com.lunabee.lbloading.withLoading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.snackbar.ErrorSnackbarState
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState
import studio.lunabee.onesafe.importexport.usecase.GetCloudInfoUseCase
import studio.lunabee.onesafe.importexport.usecase.OpenAndroidInternalBackupStorageUseCase
import studio.lunabee.onesafe.importexport.usecase.StoreExternalBackupUseCase
import java.net.URI
import javax.inject.Inject

@HiltViewModel
class LostKeyViewModel @Inject constructor(
    getCloudInfoUseCase: GetCloudInfoUseCase,
    private val storeExternalBackupUseCase: StoreExternalBackupUseCase,
    private val loadingManager: LoadingManager,
    private val openInternalBackupStorageUseCase: OpenAndroidInternalBackupStorageUseCase,
) : ViewModel() {

    private val _uiState: MutableStateFlow<LostKeyUiState> = MutableStateFlow(LostKeyUiState.Idle)
    val uiState: StateFlow<LostKeyUiState> = _uiState.asStateFlow()

    private val _snackbarState = MutableSharedFlow<SnackbarState?>()
    val snackbarState: SharedFlow<SnackbarState?> = _snackbarState.asSharedFlow()

    val folderUri: StateFlow<URI?> = flow { emit(getCloudInfoUseCase.getFirstFolderAvailable()) }
        .stateIn(
            viewModelScope,
            CommonUiConstants.Flow.DefaultSharingStarted,
            null,
        )

    fun cacheBackupFile(uri: Uri) {
        viewModelScope.launch {
            val result = storeExternalBackupUseCase(uri).withLoading(loadingManager).last().asResult()
            when (result) {
                is LBResult.Failure -> showSnackbar(ErrorSnackbarState(error = result.throwable, onClick = {}))
                is LBResult.Success -> {
                    _uiState.value = LostKeyUiState.ExitToMain(result.successData.file.toUri())
                }
            }
        }
    }

    fun showSnackbar(errorSnackbarState: ErrorSnackbarState) {
        viewModelScope.launch {
            _snackbarState.emit(errorSnackbarState)
        }
    }

    fun openInternalBackupStorage(context: Context) {
        if (!openInternalBackupStorageUseCase(context)) {
            showSnackbar(ErrorSnackbarState(LbcTextSpec.StringResource(OSString.common_error_noFileManager), {}))
        }
    }
}
