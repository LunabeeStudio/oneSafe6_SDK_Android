package studio.lunabee.onesafe.feature.itemform.manager

import android.content.Context
import com.lunabee.lbcore.helper.LBLoadingVisibilityDelayDelegate
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.common.extensions.createTempFile
import studio.lunabee.onesafe.commonui.extension.isValidUrl
import studio.lunabee.onesafe.commonui.utils.CloseableCoroutineScope
import studio.lunabee.onesafe.commonui.utils.CloseableMainCoroutineScope
import studio.lunabee.onesafe.domain.Constant
import studio.lunabee.onesafe.domain.model.common.UrlMetadata
import studio.lunabee.onesafe.domain.usecase.GetUrlMetadataUseCase
import studio.lunabee.onesafe.domain.usecase.item.DownloadItemIconFromUrlUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetAppSettingUseCase
import studio.lunabee.onesafe.error.OSAppError
import studio.lunabee.onesafe.feature.itemform.model.canAutoOverride
import studio.lunabee.onesafe.qualifier.ImageCacheDirectory
import java.io.File
import javax.inject.Inject

/**
 * Manage everything related to metadata downloading (i.e fetching title and icon) from remote sources
 */
@ViewModelScoped
class UrlMetadataManager @Inject constructor(
    private val getUrlMetadataUseCase: GetUrlMetadataUseCase,
    @ImageCacheDirectory private val imageCacheDir: File,
    @ApplicationContext private val context: Context,
    private val itemEditionDataManager: ItemEditionDataManagerDefault,
    private val getAppSettingUseCase: GetAppSettingUseCase,
    private val downloadItemIconFromUrlUseCase: DownloadItemIconFromUrlUseCase,
) : CloseableCoroutineScope by CloseableMainCoroutineScope() {
    private val loadingDelegate: LBLoadingVisibilityDelayDelegate = LBLoadingVisibilityDelayDelegate()
    private val _urlMetadata: MutableSharedFlow<LBFlowResult<UrlMetadata>?> = MutableSharedFlow()
    val urlMetadata: SharedFlow<LBFlowResult<UrlMetadata>?> = _urlMetadata.asSharedFlow()

    private val _isLoading: MutableStateFlow<Float?> = MutableStateFlow(null)
    val isLoading: StateFlow<Float?> = _isLoading.asStateFlow()

    private var fetchJob: Job? = null
    fun fetchUrlMetadataIfNeeded(url: String?) {
        coroutineScope.launch {
            if (getAppSettingUseCase.automation().data == true) {
                fetchJob?.cancel()
                if (url.isValidUrl()) {
                    if (url != null) {
                        fetchJob = coroutineScope.launch {
                            delay(duration = AppConstants.Ui.DelayedLoading.DelayBeforeFetchingMetadata)
                            val isTitleRequested = itemEditionDataManager.nameField.isValueOverridable()
                            val isIconRequested = itemEditionDataManager.itemIconData.value.canAutoOverride
                            val requestedData = when {
                                isTitleRequested && isIconRequested -> GetUrlMetadataUseCase.RequestedData.All
                                isTitleRequested -> GetUrlMetadataUseCase.RequestedData.Title
                                isIconRequested -> GetUrlMetadataUseCase.RequestedData.Image
                                else -> null
                            }
                            requestedData?.let {
                                asyncFetchUrlMetadata(
                                    url = url,
                                    requestedData = requestedData,
                                    force = false,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun forceFetchUrlMetadata(
        url: String,
    ) {
        fetchJob?.cancel()
        fetchJob = coroutineScope.launch {
            val isTitleRequested = itemEditionDataManager.nameField.isValueOverridable()
            val requestedData = when {
                isTitleRequested -> GetUrlMetadataUseCase.RequestedData.All
                else -> GetUrlMetadataUseCase.RequestedData.Image
            }
            asyncFetchUrlMetadata(
                url = url,
                requestedData = requestedData,
                force = true,
            )
        }
    }

    fun fetchItemIconFromUrl(
        url: String,
    ) {
        fetchJob?.cancel()
        fetchJob = coroutineScope.launch {
            doFetchItemIconUrl(url = url)
        }
    }

    private suspend fun asyncFetchUrlMetadata(
        url: String,
        requestedData: GetUrlMetadataUseCase.RequestedData = GetUrlMetadataUseCase.RequestedData.All,
        force: Boolean,
    ) {
        loadingDelegate.delayShowLoading { _isLoading.value = Constant.IndeterminateProgress }
        val iconFile = context.createTempFile(
            fileName = imageFileName,
            directory = imageCacheDir,
        )
        val result = getUrlMetadataUseCase(
            url = url,
            iconFile = iconFile,
            force = force,
            requestedData = requestedData,
        )
        when (result) {
            is LBResult.Success -> {
                loadingDelegate.delayHideLoading {
                    _isLoading.value = null
                    coroutineScope.launch {
                        _urlMetadata.emit(result.asFlowResult())
                    }
                }
            }
            is LBResult.Failure -> {
                loadingDelegate.delayHideLoading { _isLoading.value = null }
                // Fail silently if not forced
                if (force) {
                    _urlMetadata.emit(LBFlowResult.Failure(OSAppError(OSAppError.Code.URL_DATA_FETCHING_FAIL)))
                }
                iconFile.delete()
            }
        }
    }

    private suspend fun doFetchItemIconUrl(url: String) {
        val iconFile = context.createTempFile(
            fileName = imageFileName,
            directory = imageCacheDir,
        )
        downloadItemIconFromUrlUseCase(
            url = url,
            iconFile = iconFile,
        )
            .catch { iconFile.delete() }
            .collect { result ->
                when (result) {
                    is LBFlowResult.Failure -> {
                        iconFile.delete()
                        loadingDelegate.delayHideLoading {
                            _isLoading.value = null
                            _urlMetadata.emit(LBFlowResult.Failure(OSAppError(OSAppError.Code.URL_DATA_FETCHING_FAIL)))
                        }
                    }
                    is LBFlowResult.Loading -> {
                        loadingDelegate.delayShowLoading(updateLoading = { _isLoading.value = result.progress }) {}
                    }
                    is LBFlowResult.Success -> {
                        loadingDelegate.delayHideLoading {
                            _isLoading.value = null
                            _urlMetadata.emit(result)
                        }
                    }
                }
            }
    }

    override fun close() {
        coroutineScope.cancel()
        imageCacheDir.deleteRecursively()
    }

    fun cancelFetch() {
        fetchJob?.cancel()
        fetchJob = null
    }

    companion object {
        private const val imageFileName: String = "icon_fetch"
    }
}
