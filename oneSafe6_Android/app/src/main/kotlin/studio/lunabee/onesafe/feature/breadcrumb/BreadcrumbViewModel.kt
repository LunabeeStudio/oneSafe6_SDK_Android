package studio.lunabee.onesafe.feature.breadcrumb

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.ErrorNameProvider
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.domain.usecase.item.GetItemWithAncestorsUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.feature.dialog.CreateItemCorruptedDialogState
import studio.lunabee.onesafe.feature.itemactions.GetSafeItemActionHelper
import studio.lunabee.onesafe.feature.itemactions.GetSafeItemActionUiStateDelegate
import studio.lunabee.onesafe.feature.itemform.manager.ItemEditionFileFieldManager
import studio.lunabee.onesafe.feature.search.delegate.SearchLogicDelegate
import studio.lunabee.onesafe.feature.search.delegate.SearchLogicDelegateImpl
import studio.lunabee.onesafe.ui.extensions.toColor
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class BreadcrumbViewModel @Inject constructor(
    private val searchLogicDelegateImpl: SearchLogicDelegateImpl,
    private val getItemWithAncestorsUseCase: GetItemWithAncestorsUseCase,
    private val decryptUseCase: ItemDecryptUseCase,
    private val getSafeItemActionHelper: GetSafeItemActionHelper,
    itemEditionFileManager: ItemEditionFileFieldManager,
) : ViewModel(),
    SearchLogicDelegate by searchLogicDelegateImpl,
    GetSafeItemActionUiStateDelegate by getSafeItemActionHelper {

    private val breadcrumbItems: MutableStateFlow<ImmutableList<BreadcrumbUiDataSpec>> = MutableStateFlow(
        persistentListOf(
            RouteBreadcrumbUiData.home(),
        ),
    )
    private val userColor: MutableStateFlow<Color?> = MutableStateFlow(null)
    private val dialogState = MutableStateFlow<DialogState?>(null)

    private var updateJob: Job? = null
    private var searchInitialized: AtomicBoolean = AtomicBoolean(false)

    val uiState: StateFlow<BreadcrumbUiState> = combine(
        breadcrumbItems,
        userColor,
        itemEditionFileManager.prepareDataForFieldImageCapture(),
        dialogState,
    ) { breadcrumbItems, userColor, cameraForField, dialogState ->
        BreadcrumbUiState.Idle(
            breadcrumbItems = breadcrumbItems,
            userColor = userColor,
            cameraForField = cameraForField,
            dialogState = dialogState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = CommonUiConstants.Flow.DefaultSharingStarted,
        initialValue = BreadcrumbUiState.Initializing,
    )

    fun initSearch() {
        if (!searchInitialized.getAndSet(true)) {
            viewModelScope.launch {
                searchLogicDelegateImpl.initSearch()
            }
        }
    }

    fun onBreadcrumbMainClick() {
        when (breadcrumbItems.value.lastOrNull()?.mainAction) {
            BreadcrumbMainAction.Corrupted -> {
                dialogState.value = CreateItemCorruptedDialogState(
                    actions = listOf(DialogAction.commonOk(::dismissDialog)),
                    dismiss = ::dismissDialog,
                )
            }
            // AddItem -> Compose bottom sheet
            BreadcrumbMainAction.AddItem, BreadcrumbMainAction.None, null -> {
                /* no-op */
            }
        }
    }

    fun updateBreadcrumb(breadcrumbDestination: BreadcrumbDestinationSpec) {
        updateJob?.cancel("Multi call")
        updateJob = viewModelScope.launch {
            when (breadcrumbDestination) {
                is ItemBreadcrumbDestination -> {
                    val breadcrumbPath = mutableListOf<BreadcrumbUiDataSpec>(RouteBreadcrumbUiData.home())
                    yield()
                    val items = getItemWithAncestorsUseCase(breadcrumbDestination.itemId).data

                    if (items?.firstOrNull()?.deletedAt != null) {
                        breadcrumbPath += RouteBreadcrumbUiData.bin()
                    }

                    var error = false
                    items?.mapIndexedTo(breadcrumbPath) { idx, item ->
                        val nameResult = item.encName?.let { encName ->
                            decryptUseCase(encName, item.id, String::class)
                        }

                        if (idx == items.lastIndex) {
                            error = nameResult is LBResult.Failure
                        }

                        val itemNameProvider = if (nameResult is LBResult.Failure) {
                            ErrorNameProvider
                        } else {
                            DefaultNameProvider(nameResult?.data)
                        }

                        val mainAction = when {
                            item.deletedAt != null -> BreadcrumbMainAction.None
                            nameResult is LBResult.Failure -> BreadcrumbMainAction.Corrupted
                            else -> BreadcrumbMainAction.AddItem
                        }

                        ItemBreadcrumbUiData(
                            itemId = item.id,
                            nameProvider = itemNameProvider,
                            mainAction = mainAction,
                        )
                    }

                    userColor.value = if (error) {
                        AppConstants.Ui.Item.ErrorColor
                    } else {
                        val lastItem = items?.lastOrNull()
                        lastItem?.encColor?.let { decryptUseCase(it, lastItem.id, String::class).data }?.toColor()
                    }
                    breadcrumbItems.value = breadcrumbPath.toImmutableList()
                }
                is HardBreadcrumbDestination -> {
                    userColor.value = null
                    breadcrumbItems.value = breadcrumbDestination.breadcrumbUiDataPath
                }
            }

            updateJob = null
        }
    }

    private fun dismissDialog() {
        dialogState.value = null
    }

    override fun onCleared() {
        super.onCleared()
        searchLogicDelegateImpl.close()
        getSafeItemActionHelper.close()
    }
}
