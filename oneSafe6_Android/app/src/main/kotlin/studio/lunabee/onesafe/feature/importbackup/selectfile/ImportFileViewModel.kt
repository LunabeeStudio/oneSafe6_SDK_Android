package studio.lunabee.onesafe.feature.importbackup.selectfile

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.helper.LBLoadingVisibilityDelayDelegate
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.common.extensions.isAutoBackup
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.ErrorDialogState
import studio.lunabee.onesafe.commonui.error.description
import studio.lunabee.onesafe.commonui.utils.FileDetails
import studio.lunabee.onesafe.domain.model.importexport.ImportMetadata
import studio.lunabee.onesafe.domain.qualifier.ArchiveCacheDir
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.error.OSAppError
import studio.lunabee.onesafe.feature.dialog.WrongExtensionDialogState
import studio.lunabee.onesafe.importexport.usecase.GetAutoBackupStreamUseCase
import studio.lunabee.onesafe.importexport.usecase.GetMetadataFromArchiveUseCase
import studio.lunabee.onesafe.importexport.utils.isOsFile
import java.io.File
import java.io.InputStream
import javax.inject.Inject
import kotlin.time.Duration

private val logger = LBLogger.get<ImportFileViewModel>()

@HiltViewModel
class ImportFileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @FileDispatcher private val fileDispatcher: CoroutineDispatcher,
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.Import) private val archiveExtractedDirectory: File,
    private val getMetadataFromArchiveUseCase: GetMetadataFromArchiveUseCase,
    private val getAutoBackupStreamUseCase: GetAutoBackupStreamUseCase,
) : ViewModel() {

    private val loadingDelegate: LBLoadingVisibilityDelayDelegate =
        LBLoadingVisibilityDelayDelegate(delayBeforeShow = Duration.ZERO)

    private val _importMetadataState: MutableStateFlow<MetadataReadState> =
        MutableStateFlow(value = MetadataReadState.WaitingForExtract)
    val importMetadataState: StateFlow<MetadataReadState> = _importMetadataState.asStateFlow()

    // File uri or backup id was passed as a navigation param
    private val data: Uri? = savedStateHandle.get<String>(ImportFileDestination.dataArg)?.let(Uri::parse)
    private val deleteOnComplete: Boolean = savedStateHandle.get<Boolean>(ImportFileDestination.deleteFileArg) ?: false

    init {
        when {
            data == null -> {
                /* no-op */
            }
            data.isAutoBackup() -> startAutoBackupImport(data)
            else -> {
                _importMetadataState.value = MetadataReadState.StartExtract { context ->
                    unzipFileAndGetMetadata(data, context)
                }
            }
        }
    }

    private fun startAutoBackupImport(data: Uri) {
        val backupId = data.pathSegments.firstOrNull()
        if (backupId != null) {
            viewModelScope.launch {
                getAutoBackupStreamUseCase(backupId).collect { backupStreamResult ->
                    when (backupStreamResult) {
                        // TODO import progress
                        is LBFlowResult.Loading -> _importMetadataState.value = MetadataReadState.ExtractInProgress(0f)
                        is LBFlowResult.Failure -> _importMetadataState.value = MetadataReadState.ExitWithError(
                            backupStreamResult.throwable.description(),
                        )
                        is LBFlowResult.Success -> readArchiveMetadata(backupStreamResult.successData)
                    }
                }
            }
        } else {
            _importMetadataState.value = MetadataReadState.ExitWithError(
                LbcTextSpec.StringResource(OSString.import_selectFile_error_missingBackupId),
            )
        }
    }

    fun unzipFileAndGetMetadata(uri: Uri, context: Context) {
        try {
            if (FileDetails.fromUri(uri = uri, context = context).isOsFile()) {
                readArchiveMetadata(context, uri)
            } else {
                handleNotOSFile()
            }
        } catch (e: Exception) {
            logger.e(e)
            emitError(e)
        }
    }

    private fun readArchiveMetadata(context: Context, uri: Uri) {
        context.contentResolver.openInputStream(uri)?.let { inputStream ->
            viewModelScope.launch {
                try {
                    readArchiveMetadata(inputStream)
                } finally {
                    if (deleteOnComplete) {
                        runCatching { uri.toFile().delete() }
                    }
                }
            }
        } ?: emitError(error = OSAppError(OSAppError.Code.URI_INVALID))
    }

    private suspend fun readArchiveMetadata(inputStream: InputStream) {
        getMetadataFromArchive(inputStream)
            .collect { metadataResult ->
                when (metadataResult) {
                    is LBFlowResult.Failure -> {
                        loadingDelegate.delayHideLoading {
                            archiveExtractedDirectory.deleteRecursively()
                            emitError(error = metadataResult.throwable)
                        }
                    }
                    is LBFlowResult.Success -> loadingDelegate.delayHideLoading {
                        if (metadataResult.successData.archiveVersion > LatestSupportedArchiveVersion) {
                            _importMetadataState.value = MetadataReadState.NotFullySupported
                        } else {
                            _importMetadataState.value = MetadataReadState.Success
                        }
                    }
                    is LBFlowResult.Loading ->
                        loadingDelegate.delayShowLoading {
                            _importMetadataState.value =
                                MetadataReadState.ExtractInProgress(progress = metadataResult.progress ?: 0f)
                        }
                }
            }
    }

    private suspend fun getMetadataFromArchive(inputStream: InputStream): Flow<LBFlowResult<ImportMetadata>> = try {
        getMetadataFromArchiveUseCase(
            inputStream = inputStream,
            archiveExtractedDirectory = archiveExtractedDirectory,
        ).onCompletion {
            closeInputStream(inputStream)
        }
    } catch (e: Throwable) {
        closeInputStream(inputStream)
        throw e
    }

    private suspend fun closeInputStream(inputStream: InputStream) {
        withContext(fileDispatcher) {
            @Suppress("BlockingMethodInNonBlockingContext")
            inputStream.close()
        }
    }

    private fun handleNotOSFile() {
        // The user opened oneSafe with an invalid file.
        if (data != null) {
            _importMetadataState.value = MetadataReadState.ExitWithError(
                LbcTextSpec.StringResource(OSString.import_selectFile_error_wrongFileSelected),
            )
        } else {
            _importMetadataState.value = MetadataReadState.Error(
                dialogState = WrongExtensionDialogState(
                    retry = { _importMetadataState.value = MetadataReadState.LaunchPicker(::resetState) },
                    dismiss = ::resetState,
                ),
            )
        }
    }

    private fun resetState() {
        _importMetadataState.value = MetadataReadState.WaitingForExtract
    }

    fun setMetadataReadStateToInitialValue() {
        _importMetadataState.value = MetadataReadState.WaitingForExtract
    }

    private fun emitError(error: Throwable?) {
        _importMetadataState.value = MetadataReadState.Error(
            dialogState = ErrorDialogState(
                error = error,
                actions = listOf(
                    DialogAction.commonOk { _importMetadataState.value = MetadataReadState.WaitingForExtract },
                ),
                dismiss = { _importMetadataState.value = MetadataReadState.WaitingForExtract },
            ),
        )
    }

    companion object {
        const val LatestSupportedArchiveVersion: Int = 2
    }
}
