package studio.lunabee.onesafe.feature.itemform.viewmodel

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbloading.LoadingManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.dialog.ErrorDialogState
import studio.lunabee.onesafe.commonui.error.description
import studio.lunabee.onesafe.commonui.extension.isValidUrl
import studio.lunabee.onesafe.commonui.snackbar.ErrorSnackbarState
import studio.lunabee.onesafe.commonui.snackbar.MessageSnackBarState
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState
import studio.lunabee.onesafe.domain.common.FieldIdProvider
import studio.lunabee.onesafe.domain.model.safeitem.ItemFieldData
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.error.OSAppError
import studio.lunabee.onesafe.feature.camera.model.CameraData
import studio.lunabee.onesafe.feature.camera.model.OSMediaType
import studio.lunabee.onesafe.feature.itemform.manager.ItemEditionDataManagerDefault
import studio.lunabee.onesafe.feature.itemform.manager.ItemEditionFileFieldManager
import studio.lunabee.onesafe.feature.itemform.manager.UrlMetadataManager
import studio.lunabee.onesafe.feature.itemform.model.EnterUrlForIconDialogState
import studio.lunabee.onesafe.feature.itemform.model.StandardItemFieldType
import studio.lunabee.onesafe.feature.itemform.model.uifield.FieldObserver
import studio.lunabee.onesafe.feature.itemform.model.uifield.ObservableField
import studio.lunabee.onesafe.feature.itemform.model.uifield.UiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.file.FileUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.file.ThumbnailState
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl.NameTextUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl.UrlTextUiField
import studio.lunabee.onesafe.feature.itemform.screen.CaptureAnotherSnackbarState
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.ItemFormInitialInfo
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.PopulateScreenDelegate
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.SaveItemAndFieldDelegate
import studio.lunabee.onesafe.feature.itemform.viewmodel.state.ItemFormState
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.model.OSActionState
import java.util.UUID

// TODO
//  • Extract field edition to manager (like ItemEditionDataManager and ItemEditionFileFieldManager)
//  • Use Kotlin delegate for ItemEditionFileFieldManager (like ItemEditionDataManager)

/**
 * Manage item data edition (i.e item icon, name, color)
 */
interface ItemEditionDataManager {
    val nameField: NameTextUiField
    val colorPreview: StateFlow<Color?>
    val itemIcon: StateFlow<OSImageSpec?>
    val emojiAsItemIcon: StateFlow<String?>
    fun removeItemIconSelected()
    fun onItemIconPickedByUser(image: OSImageSpec?)
    fun setColorSelected(color: Color?)
    fun saveSelectedColor()
    fun onThumbnailSelected(thumbnail: OSImageSpec)
}

private sealed interface ItemFormInternalState {

    data class Idle(
        val onCaptureAnother: (() -> Unit)?,
    ) : ItemFormInternalState

    data object ReOrderField : ItemFormInternalState

    data class Exit(
        val safeItemIdCreated: UUID?,
    ) : ItemFormInternalState
}

abstract class ItemFormViewModel(
    private val saveItemAndFieldDelegate: SaveItemAndFieldDelegate,
    private val populateScreenDelegate: PopulateScreenDelegate,
    private val urlMetadataManager: UrlMetadataManager,
    private val itemEditionDataManager: ItemEditionDataManagerDefault,
    private val itemEditionFileManager: ItemEditionFileFieldManager,
    private val fieldIdProvider: FieldIdProvider,
    private val urlMetadataFieldObserver: FieldObserver,
    private val loadingManager: LoadingManager,
) : ViewModel(),
    SaveItemAndFieldDelegate by saveItemAndFieldDelegate,
    PopulateScreenDelegate by populateScreenDelegate,
    ItemEditionDataManager by itemEditionDataManager {

    private val internalState = MutableStateFlow<ItemFormInternalState>(ItemFormInternalState.Idle(null))

    val itemFormState: StateFlow<ItemFormState> = combine(
        internalState,
        itemEditionFileManager.prepareDataForFieldImageCapture(),
        itemEditionDataManager.prepareDataForItemIconCapture(),
    ) { internalState, cameraForField, cameraForIcon ->
        when (internalState) {
            is ItemFormInternalState.Exit -> ItemFormState.Exit(internalState.safeItemIdCreated)
            is ItemFormInternalState.Idle -> ItemFormState.Idle(
                cameraDataForField = cameraForField,
                cameraDataForIcon = cameraForIcon,
                onCaptureAnother = internalState.onCaptureAnother,
            )
            ItemFormInternalState.ReOrderField -> ItemFormState.ReOrderField
        }
    }.stateIn(
        viewModelScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        ItemFormState.Initializing,
    )

    private val viewModelDialogState = MutableStateFlow<DialogState?>(value = null)
    val dialogState: Flow<DialogState?> = combine(
        viewModelDialogState,
        itemEditionDataManager.dialogState,
        itemEditionFileManager.dialogState,
    ) { vmDialogState, editionDataDialogState, editionFileDialogState ->
        vmDialogState ?: editionDataDialogState ?: editionFileDialogState
    }
    val itemIconLoading: StateFlow<Float?> = urlMetadataManager.isLoading

    private val _snackbarData: MutableStateFlow<SnackbarState?> = MutableStateFlow(null)
    val snackbarData: StateFlow<SnackbarState?> = _snackbarData.asStateFlow()

    private val _uiFields: MutableStateFlow<List<UiField>> = MutableStateFlow(listOf())
    val uiFields: StateFlow<List<UiField>> = _uiFields.onEach { fields ->
        refreshObservers(fields)
    }.stateIn(
        viewModelScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        _uiFields.value,
    )

    private val saveMtx = Mutex()

    init {
        viewModelScope.launch {
            val initialInfoResult = getInitialInfo()
            initialInfoResult.data?.let { loadInitialInfo(it) }
            setInitialInfo(initialInfoResult)
            internalState.value = ItemFormInternalState.Idle(null)
        }
        listenForUrlMetadata()
    }

    private fun refreshObservers(fields: List<UiField>) {
        val urlTextUiFields = fields.filterIsInstance<UrlTextUiField>()
        urlTextUiFields.forEach { it.removeObserver() }
        urlTextUiFields.firstOrNull()?.setObserver(urlMetadataFieldObserver)
        checkTipsToDisplay(fields)
    }

    abstract fun isSaveEnabled(): OSActionState

    abstract fun checkTipsToDisplay(fields: List<UiField>)

    fun isReOrderSaveEnabled(
        initialList: List<UiField>,
        newList: List<UiField>,
    ): Boolean {
        return initialList.any { initialList.indexOf(it) != newList.indexOf(it) }
    }

    fun updateFieldsOrder(newList: List<UiField>) {
        val currentList = _uiFields.value.toMutableList()
        newList.forEach { newField ->
            currentList.removeIf { newField.id == it.id }
        }
        currentList.addAll(newList)
        _uiFields.value = currentList
    }

    fun canFetchFromUrl(): Boolean = uiFields.value.firstOrNull { it is UrlTextUiField }?.getDisplayedValue().isValidUrl()

    fun forceFetchDataFromUrl() {
        uiFields.value.firstOrNull { it is UrlTextUiField }?.getDisplayedValue()?.let(urlMetadataManager::forceFetchUrlMetadata)
    }

    fun displayEnterUrlForIconDialog() {
        viewModelDialogState.value = EnterUrlForIconDialogState(
            fetchIconFromUrl = {
                viewModelDialogState.value = null
                urlMetadataManager.fetchItemIconFromUrl(it)
            },
            dismiss = { viewModelDialogState.value = null },
        )
    }

    protected fun isAnyFieldInError(): Boolean {
        return itemEditionDataManager.nameField.isInError() || uiFields.value.any { it.isInError() }
    }

    protected open fun setInitialInfo(initialInfoResult: LBResult<ItemFormInitialInfo>) {
        when (initialInfoResult) {
            is LBResult.Failure -> _snackbarData.value = ErrorSnackbarState(initialInfoResult.throwable, ::consumeSnackBar)
            is LBResult.Success -> {
                if (initialInfoResult.successData.isFromCamera) {
                    setCaptureAnotherSnackbar()
                }
            }
        }
        initialInfoResult.data?.let { initialInfo ->
            _uiFields.value += initialInfo.fields
            itemEditionDataManager.nameField.onValueChanged(initialInfo.name)
        }
    }

    private fun listenForUrlMetadata() {
        viewModelScope.launch {
            urlMetadataManager.urlMetadata.collect {
                when (val result = it) {
                    is LBFlowResult.Success -> itemEditionDataManager.setItemIconFromMetadataIfNeeded(result.successData)
                    is LBFlowResult.Failure -> _snackbarData.value = MessageSnackBarState(
                        message = result.throwable.description(),
                    )
                    else -> {}
                }
            }
        }
    }

    private fun setErrorDialogState(throwable: Throwable?) {
        viewModelDialogState.value = ErrorDialogState(
            error = throwable,
            actions = listOf(
                DialogAction(
                    text = LbcTextSpec.StringResource(id = OSString.common_close),
                    clickLabel = LbcTextSpec.StringResource(id = OSString.common_accessibility_popup_dismiss),
                    onClick = { viewModelDialogState.value = null },
                ),
            ),
            dismiss = { viewModelDialogState.value = null },
        )
    }

    protected abstract suspend fun save(
        name: String,
        icon: OSImageSpec?,
        color: Color?,
        itemFieldsData: List<ItemFieldData>,
    ): LBResult<UUID>

    fun tryToSaveItem(fields: List<ItemFieldData>) {
        viewModelScope.launch {
            saveMtx.withLock {
                if (internalState.value !is ItemFormInternalState.Exit) {
                    when { // If name is empty -> Display error on nameLabel
                        isAnyFieldInError() -> {
                            itemEditionDataManager.nameField.displayErrorOnFieldIfNeeded()
                            uiFields.value.forEach(UiField::displayErrorOnFieldIfNeeded)
                        } // No change on item, we navigateBack instead of saving
                        isSaveEnabled() == OSActionState.DisabledWithAction -> {
                            internalState.value = ItemFormInternalState.Exit(null)
                        }
                        else -> {
                            loadingManager.withLoading {
                                val result = save(
                                    name = itemEditionDataManager.nameField.getDisplayedValue(),
                                    icon = itemEditionDataManager.itemIcon.value,
                                    color = itemEditionDataManager.colorCandidate.value,
                                    itemFieldsData = fields,
                                )
                                when (result) {
                                    is LBResult.Failure -> {
                                        internalState.value = ItemFormInternalState.Idle(null)
                                        setErrorDialogState(result.throwable)
                                    }
                                    is LBResult.Success -> internalState.value = ItemFormInternalState.Exit(result.data)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun toggleIdentifier(uiField: UiField, onIdentifierChanged: (feedbackField: UiField) -> Unit) {
        val fields = _uiFields.value
        uiField.isIdentifier = !uiField.isIdentifier
        fields.firstOrNull { it !== uiField && it.isIdentifier }?.isIdentifier = false
        _uiFields.value = fields
        onIdentifierChanged(uiField)
    }

    fun renameField(id: UUID, currentFieldName: String, onRenamed: (oldName: String, newName: String) -> Unit) {
        itemEditionDataManager.showRenameFieldDialog(
            onConfirm = { newName ->
                uiFields.value.first { it.id == id }.apply {
                    fieldDescription.value = LbcTextSpec.Raw(newName)
                    placeholder = LbcTextSpec.Raw(newName)
                }
                onRenamed(currentFieldName, newName)
            },
            currentFieldName = currentFieldName,
        )
    }

    fun removeField(uiField: UiField) {
        _uiFields.value = uiFields.value.minusElement(uiField)
        if (uiField is FileUiField) {
            uiField.deletePlainCache()
        }
        if (uiField is ObservableField) {
            uiField.removeObserver()
        }
    }

    fun addField(fieldType: StandardItemFieldType) {
        val newField = fieldType.instantiateUiField(fieldIdProvider)
        _uiFields.value += newField
    }

    fun addFileField(uriList: List<Uri>) {
        uriList.map { uri ->
            itemEditionFileManager.createItemFileField(uri)?.let(::addFileUiField)
        }
    }

    fun manageMediaCaptureFromCameraForField(cameraData: CameraData) {
        viewModelScope.launch {
            val safeItemFieldKind = if ((cameraData as? CameraData.InApp)?.photoCapture?.mediaType == OSMediaType.VIDEO) {
                SafeItemFieldKind.Video
            } else {
                SafeItemFieldKind.Photo
            }
            val numberOfMedia = uiFields.value.filterIsInstance<FileUiField>().filter {
                it.safeItemFieldKind == safeItemFieldKind
            }.size
            val captureFileUiFieldResult = itemEditionFileManager.manageMediaCaptured(
                numberOfImage = numberOfMedia,
                cameraData = cameraData,
            )
            when (captureFileUiFieldResult) {
                is LBResult.Success -> {
                    addFileUiField(captureFileUiFieldResult.successData)
                    setCaptureAnotherSnackbar()
                }
                is LBResult.Failure -> {
                    _snackbarData.value = ErrorSnackbarState(
                        error = captureFileUiFieldResult.throwable,
                        onClick = ::consumeSnackBar,
                    )
                }
            }
        }
    }

    private fun setCaptureAnotherSnackbar() {
        val state = ItemFormInternalState.Idle(
            onCaptureAnother = {
                viewModelScope.launch {
                    internalState.value = ItemFormInternalState.Idle(null)
                }
            },
        )
        _snackbarData.value = CaptureAnotherSnackbarState {
            internalState.value = state
        }
    }

    private fun addFileUiField(fileUiField: FileUiField) {
        if (uiFields.value.none { it.getDisplayedValue().isNotEmpty() }) {
            viewModelScope.launch {
                val thumbnail = fileUiField.thumbnailFlow.filterIsInstance<ThumbnailState.Finished>().first()
                itemEditionDataManager.setNameAndIconFromFileField(
                    name = fileUiField.fieldDescription.value,
                    thumbnail = thumbnail.thumbnail,
                )
            }
        }
        _uiFields.value += fileUiField
    }

    fun consumeSnackBar() {
        _snackbarData.value = null
    }

    fun enterReOrderMode() {
        internalState.value = ItemFormInternalState.ReOrderField
    }

    fun exitReOrderMode() {
        viewModelScope.launch {
            internalState.value = ItemFormInternalState.Idle(null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        urlMetadataManager.close()
        itemEditionDataManager.close()
        itemEditionFileManager.close()
    }

    fun setUiFields(fields: List<UiField>) {
        _uiFields.value = fields
    }

    /**
     * Camera external -> passthrough to [ItemEditionDataManagerDefault.onImageCaptureFromSystemCamera]
     * Camera in-app   -> copy the thumbnail to the expected file (imageTakenByUserFromCamera) and continue
     */
    fun onItemIconCaptureFromCamera(cameraData: CameraData) {
        val succeed = when (cameraData) {
            is CameraData.InApp -> {
                val thumbnailFile = cameraData.photoCapture.plainThumbnailFile?.takeIf { it.exists() }

                if (thumbnailFile != null) {
                    itemEditionDataManager.onImageCaptureFromInAppCamera(thumbnailFile)
                } else {
                    LBResult.Failure(OSAppError.Code.IMAGE_CAPTURED_NOT_FOUND.get())
                }
            }
            is CameraData.External -> {
                LBResult.Success(
                    itemEditionDataManager.onImageCaptureFromSystemCamera(
                        OSImageSpec.Uri(
                            uri = cameraData.photoCapture.value.file.toUri(),
                            key = System.currentTimeMillis().toString(), // Add key to allow refresh even the uri does not change
                        ),
                    ),
                )
            }
        }

        if (succeed is LBResult.Failure) {
            _snackbarData.value = ErrorSnackbarState(succeed.throwable, ::consumeSnackBar)
        }
    }
}
