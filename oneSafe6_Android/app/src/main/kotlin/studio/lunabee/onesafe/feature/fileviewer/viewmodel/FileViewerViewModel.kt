package studio.lunabee.onesafe.feature.fileviewer.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.error.description
import studio.lunabee.onesafe.commonui.extension.getMimeType
import studio.lunabee.onesafe.commonui.snackbar.ErrorSnackbarState
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.error.OSAppError
import studio.lunabee.onesafe.feature.fileviewer.FileViewerScreenDestination
import studio.lunabee.onesafe.feature.fileviewer.loadfile.LoadFileUseCase
import studio.lunabee.onesafe.feature.fileviewer.model.FileViewerUiState
import studio.lunabee.onesafe.feature.fileviewer.model.ViewerType
import java.util.UUID
import javax.inject.Inject

private val logger = LBLogger.get<FileViewerViewModel>()

@HiltViewModel
class FileViewerViewModel @Inject constructor(
    private val fieldRepository: SafeItemFieldRepository,
    private val decryptUseCase: ItemDecryptUseCase,
    private val loadFileUseCase: LoadFileUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val fieldId: UUID = savedStateHandle.get<String>(FileViewerScreenDestination.FieldId)?.let(UUID::fromString)
        ?: error("fieldId not provided")

    private val _uiState: MutableStateFlow<FileViewerUiState> = MutableStateFlow(FileViewerUiState.Loading())
    val uiState: StateFlow<FileViewerUiState> = _uiState.asStateFlow()

    private val _snackbarState: MutableSharedFlow<SnackbarState?> = MutableSharedFlow()
    val snackbarState: SharedFlow<SnackbarState?> = _snackbarState.asSharedFlow()

    fun loadFile(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            if ((uiState.value as? FileViewerUiState.Data)?.file?.exists() != true) {
                val field = fieldRepository.getSafeItemField(fieldId)
                val name = field.encName?.let { decryptUseCase(it, field.itemId, String::class) }?.data.orEmpty()

                _uiState.value = FileViewerUiState.Loading(name)
                loadFileUseCase(field).collect { result ->
                    when (result) {
                        is LBFlowResult.Loading -> {}
                        is LBFlowResult.Failure -> {
                            result.throwable?.let(logger::e)
                            _uiState.value = FileViewerUiState.Error(result.throwable.description(), name)
                        }
                        is LBFlowResult.Success -> {
                            val file = result.successData
                            val mimeType = Uri.fromFile(file).getMimeType(context)
                            _uiState.value = FileViewerUiState.Data(
                                viewerType = ViewerType.fromField(mimeType),
                                file = file,
                                name = name,
                                mimeType = mimeType ?: AppConstants.FileProvider.LauncherAllFileFilter,
                            )
                        }
                    }
                }
            }
        }
    }

    fun saveFile(uri: Uri, context: Context) {
        viewModelScope.launch {
            val file = (uiState.value as? FileViewerUiState.Data)?.file
            file?.let {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        file.inputStream().use { it.copyTo(outputStream) }
                    }
                    _snackbarState.emit(
                        object : SnackbarState() {
                            override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.itemDetails_fields_file_saveSuccess)
                        },
                    )
                } catch (exception: Exception) {
                    _snackbarState.emit(
                        ErrorSnackbarState(
                            error = OSAppError(OSAppError.Code.FILE_SAVING_ERROR, cause = exception),
                        ) {},
                    )
                }
            }
        }
    }
}
