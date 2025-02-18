package studio.lunabee.onesafe.feature.share.file

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.common.extensions.getFileSharingIntent
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.domain.usecase.settings.GetAppSettingUseCase
import studio.lunabee.onesafe.domain.usecase.settings.SetAppSettingUseCase
import studio.lunabee.onesafe.feature.clipboard.ClipboardDelegate
import studio.lunabee.onesafe.feature.clipboard.ClipboardDelegateImpl
import studio.lunabee.onesafe.feature.dialog.sharing.ShareFileDialogState
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ShareFileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    clipboardDelegate: ClipboardDelegateImpl,
    private val getAppSettingUseCase: GetAppSettingUseCase,
    private val setAppSettingUseCase: SetAppSettingUseCase,
) : ViewModel(),
    ClipboardDelegate by clipboardDelegate {

    private val _uiState = MutableStateFlow<ShareFileUiState>(ShareFileUiState.Idle)
    val uiState: StateFlow<ShareFileUiState> get() = _uiState.asStateFlow()

    private val _dialogState = MutableStateFlow<DialogState?>(null)
    val dialogState: StateFlow<DialogState?> get() = _dialogState.asStateFlow()

    init {
        _uiState.value = ShareFileUiState.Data(
            password = savedStateHandle.get<String>(ShareFileDestination.PasswordArg)?.let { Uri.decode(it) }.orEmpty(),
            file = savedStateHandle.get<String>(ShareFileDestination.FilePathArgs)?.let { File(Uri.decode(it)) },
            itemsNbr = savedStateHandle.get<Int>(ShareFileDestination.ItemsNbrArg) ?: 0,
        )
    }

    fun shareFile(
        context: Context,
        file: File,
    ) {
        viewModelScope.launch {
            if (getAppSettingUseCase.displayShareWarning().data == true) {
                _dialogState.value = ShareFileDialogState(
                    dismiss = { hideDialog() },
                    onCLickOnAcknowledge = {
                        launchSharingIntent(context, file)
                        hideDialog()
                    },
                    onClickOnDoNotRemind = {
                        launchSharingIntent(context, file)
                        disableShareWarningDisplay()
                        hideDialog()
                    },
                )
            } else {
                launchSharingIntent(context, file)
            }
        }
    }

    private fun launchSharingIntent(context: Context, file: File) {
        context.startActivity(
            context.getFileSharingIntent(
                fileToShare = file,
                mimeType = AppConstants.FileProvider.MimeTypeZip,
            ),
        )
    }

    private fun disableShareWarningDisplay() {
        viewModelScope.launch {
            setAppSettingUseCase.disableShareWarningDisplay()
        }
    }

    private fun hideDialog() {
        _dialogState.value = null
    }
}
