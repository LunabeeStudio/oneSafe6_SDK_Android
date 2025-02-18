package studio.lunabee.onesafe.feature.itemdetails

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.common.model.item.PlainItemData
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.ErrorNameProvider
import studio.lunabee.onesafe.commonui.OSItemIllustrationHelper
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.OSPlurals
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.snackbar.ErrorSnackbarState
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState
import studio.lunabee.onesafe.domain.LoadFileCancelAllUseCase
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.usecase.GetIconUseCase
import studio.lunabee.onesafe.domain.usecase.item.CountSafeItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.domain.usecase.item.SecureGetItemFieldUseCase
import studio.lunabee.onesafe.domain.usecase.item.SecureGetItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.SetSafeItemAsConsultedAtNowUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetAppVisitUseCase
import studio.lunabee.onesafe.domain.usecase.settings.SetAppVisitUseCase
import studio.lunabee.onesafe.error.OSAppError
import studio.lunabee.onesafe.feature.clipboard.ClipboardDelegate
import studio.lunabee.onesafe.feature.clipboard.ClipboardDelegateImpl
import studio.lunabee.onesafe.feature.fileviewer.loadfile.LoadFileUseCase
import studio.lunabee.onesafe.feature.itemactions.GetSafeItemActionHelper
import studio.lunabee.onesafe.feature.itemactions.GetSafeItemActionUiStateDelegate
import studio.lunabee.onesafe.feature.itemactions.ItemDetailsData
import studio.lunabee.onesafe.feature.itemdetails.model.ItemDetailsDeletedCardData
import studio.lunabee.onesafe.feature.itemdetails.model.ItemDetailsScreenUiState
import studio.lunabee.onesafe.feature.itemdetails.model.ItemDetailsTab
import studio.lunabee.onesafe.feature.itemdetails.model.informationtabentry.InformationTabEntry
import studio.lunabee.onesafe.qualifier.AppScope
import studio.lunabee.onesafe.ui.extensions.toColor
import studio.lunabee.onesafe.usecase.AndroidGetCachedThumbnailUseCase
import java.io.File
import java.util.UUID
import javax.inject.Inject

private val logger = LBLogger.get<ItemDetailsViewModel>()

@HiltViewModel
class ItemDetailsViewModel @DelicateCoroutinesApi
@Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val decryptUseCase: ItemDecryptUseCase,
    private val getIconUseCase: GetIconUseCase,
    private val countSafeItemInParentUseCase: CountSafeItemUseCase,
    private val setSafeItemAsConsultedAtNowUseCase: SetSafeItemAsConsultedAtNowUseCase,
    getSafeItemActionHelper: GetSafeItemActionHelper,
    private val loadFileCancelAllUseCase: LoadFileCancelAllUseCase,
    private val getThumbnailFromFileUseCase: AndroidGetCachedThumbnailUseCase,
    private val loadFileUseCase: LoadFileUseCase,
    secureGetItemUseCase: SecureGetItemUseCase,
    secureGetItemFieldUseCase: SecureGetItemFieldUseCase,
    clipboardDelegate: ClipboardDelegateImpl,
    pagedPlainItemDataUseCaseFactory: ItemDetailsPagedPlainItemDataUseCaseFactory,
    getAppVisitUseCase: GetAppVisitUseCase,
    private val setAppVisitUseCase: SetAppVisitUseCase,
    @AppScope private val appScope: CoroutineScope,
) : ViewModel(getSafeItemActionHelper),
    ClipboardDelegate by clipboardDelegate,
    GetSafeItemActionUiStateDelegate by getSafeItemActionHelper {
    val itemId: UUID = savedStateHandle.get<String>(ItemDetailsDestination.itemIdArgument)!!.let(UUID::fromString)

    val uiState: StateFlow<ItemDetailsScreenUiState>

    private val _snackbarState = MutableSharedFlow<SnackbarState?>()
    val snackbarState: SharedFlow<SnackbarState?> = _snackbarState.asSharedFlow()

    private val safeItemFieldsFlow: SharedFlow<List<SafeItemField>> =
        secureGetItemFieldUseCase(
            itemId,
        ).distinctUntilChanged { old, new ->
            SafeItemField.equalsForThumbnails(old, new)
        }.map { fields -> fields.filter { field -> field.encValue != null } }
            .shareIn(viewModelScope, CommonUiConstants.Flow.DefaultSharingStarted, 1)

    private val safeItemFlow = secureGetItemUseCase(itemId)
        .shareIn(viewModelScope, CommonUiConstants.Flow.DefaultSharingStarted, 1)

    init {
        viewModelScope.launch { setSafeItemAsConsultedAtNowUseCase(itemId) }
        val metadataFlow: Flow<ItemDetailsData?> = safeItemFlow.map { item ->
            item?.let { ItemDetailsData.fromSafeItem(it, decryptUseCase, getIconUseCase) }
        }

        val infoTabEntries: Flow<List<InformationTabEntry>> = safeItemFieldsFlow.map { fields ->
            fields.mapNotNull { field ->
                InformationTabEntry.fromSafeItemField(
                    field = field,
                    decryptUseCase = decryptUseCase,
                    coroutineScope = viewModelScope,
                    getThumbnailFromFileUseCase = getThumbnailFromFileUseCase,
                    isFullScreen = fields.size == 1,
                    loadFileUseCase = loadFileUseCase,
                )
            }
        }

        val distinctItemByDeleted = safeItemFlow.filterNotNull().distinctUntilChangedBy { it.isDeleted }

        @OptIn(ExperimentalCoroutinesApi::class)
        val childrenCountFlow: Flow<Int> = distinctItemByDeleted.flatMapLatest { item ->
            countSafeItemInParentUseCase.flow(item)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        val childrenFlow: Flow<PagingData<PlainItemData>> = distinctItemByDeleted.flatMapLatest { item ->
            pagedPlainItemDataUseCaseFactory.create(item.id, item.isDeleted)()
                .cachedIn(viewModelScope)
        }.onStart { emit(PagingData.empty()) }

        uiState = combineTransform(
            metadataFlow,
            infoTabEntries,
            childrenCountFlow,
            getAppVisitUseCase.hasSeenItemReadEditToolTip().take(1),
        ) { data, infoEntries, childrenCount, hasSeenEditTips ->
            when {
                getSafeItemActionHelper.isDeleting.get() -> {
                    /* no-op */
                }
                data == null -> {
                    val error = OSAppError(OSAppError.Code.NO_ITEM_FOUND_FOR_ID, "No item found with id $itemId")
                    logger.e(error)
                }
                else -> {
                    val tabs = linkedSetOf(ItemDetailsTab.Information, ItemDetailsTab.Elements, ItemDetailsTab.More)
                    val moreTabEntries = data.moreTabEntries

                    val initialTab: ItemDetailsTab = if (infoEntries.isEmpty() && childrenCount != 0) {
                        ItemDetailsTab.Elements
                    } else {
                        ItemDetailsTab.Information
                    }

                    val itemNameProvider = if (data.nameResult is LBResult.Failure) {
                        ErrorNameProvider
                    } else {
                        OSNameProvider.fromName(
                            name = data.nameResult?.data,
                            hasIcon = data.iconResult?.data != null,
                        )
                    }

                    val color = if (data.hasCorruptedData) {
                        AppConstants.Ui.Item.ErrorColor
                    } else {
                        data.colorResult?.data?.toColor()
                    }
                    val notFullySupportedFieldKindList: List<SafeItemFieldKind.Unknown>? = infoEntries
                        .mapNotNull { it.kind as? SafeItemFieldKind.Unknown }
                        .takeIf { it.isNotEmpty() }

                    val iconData = data.iconResult?.data
                    val illustration = OSItemIllustrationHelper.get(itemNameProvider, iconData, color)
                    val dataDefault = ItemDetailsScreenUiState.Data.Default(
                        itemNameProvider = itemNameProvider,
                        icon = illustration,
                        tabs = tabs,
                        informationTab = infoEntries,
                        moreTab = moreTabEntries,
                        children = childrenFlow,
                        childrenCount = childrenCount,
                        actions = getSafeItemActionHelper.getQuickActions(itemId, true).invoke(),
                        color = color,
                        initialTab = initialTab,
                        isCorrupted = data.hasCorruptedData,
                        notSupportedKindsList = notFullySupportedFieldKindList,
                        shouldShowEditTips = !hasSeenEditTips && infoEntries.isNotEmpty(),
                    )
                    if (!hasSeenEditTips) {
                        setAppVisitUseCase.setHasSeenItemReadEditToolTip()
                    }
                    val dataState = when (data) {
                        is ItemDetailsData.Default -> dataDefault
                        is ItemDetailsData.Deleted -> {
                            val deletedCardData = ItemDetailsDeletedCardData(
                                message = LbcTextSpec.PluralsResource(
                                    OSPlurals.safeItemDetail_deletedCard_message,
                                    data.daysBeforeRemove,
                                    data.daysBeforeRemove,
                                ),
                                action = LbcTextSpec.StringResource(OSString.safeItemDetail_deletedCard_action),
                            ) {
                                getSafeItemActionHelper.getRestoreAction(itemId, isItemDetail = true).onClick()
                            }

                            ItemDetailsScreenUiState.Data.Deleted(
                                defaultData = dataDefault,
                                deletedCardData = deletedCardData,
                            )
                        }
                    }
                    emit(dataState)
                }
            }
        }.stateIn(
            viewModelScope,
            CommonUiConstants.Flow.DefaultSharingStarted,
            ItemDetailsScreenUiState.Initializing,
        )
    }

    fun saveFile(
        uri: Uri,
        file: File,
        context: Context,
    ) {
        viewModelScope.launch {
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

    override fun onCleared() {
        super.onCleared()
        appScope.launch {
            loadFileCancelAllUseCase(itemId)
        }
    }
}
