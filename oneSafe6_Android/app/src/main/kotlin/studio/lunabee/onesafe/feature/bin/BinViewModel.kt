package studio.lunabee.onesafe.feature.bin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.common.model.item.PlainItemData
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.snackbar.MessageSnackBarState
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState
import studio.lunabee.onesafe.domain.usecase.item.CountSafeItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.RemoveAllDeletedItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.RestoreItemUseCase
import studio.lunabee.onesafe.feature.bin.model.BinGlobalAction
import studio.lunabee.onesafe.feature.dialog.RemoveAllItemsDialogState
import studio.lunabee.onesafe.feature.dialog.RestoreAllItemDialogState
import studio.lunabee.onesafe.feature.itemactions.GetSafeItemActionHelper
import studio.lunabee.onesafe.feature.itemactions.GetSafeItemActionUiStateDelegate
import javax.inject.Inject

@HiltViewModel
class BinViewModel @Inject constructor(
    countSafeItemInParentUseCase: CountSafeItemUseCase,
    private val restoreItemUseCase: RestoreItemUseCase,
    private val removeAllDeletedItemUseCase: RemoveAllDeletedItemUseCase,
    private val getSafeItemActionHelper: GetSafeItemActionHelper,
    pagedPlainItemDataUseCase: BinPagedPlainItemDataUseCase,
) : ViewModel(),
    GetSafeItemActionUiStateDelegate by getSafeItemActionHelper {

    private val _dialogState = MutableStateFlow<DialogState?>(null)
    val dialogState: StateFlow<DialogState?> get() = _dialogState.asStateFlow()

    private val _snackbarState = MutableStateFlow<SnackbarState?>(null)
    val snackbarState: StateFlow<SnackbarState?> = _snackbarState.asStateFlow()

    var initialDeletedItemCount: Int = 0
        private set

    val items: Flow<PagingData<PlainItemData>> = pagedPlainItemDataUseCase()
        .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            initialDeletedItemCount = countSafeItemInParentUseCase.deleted(null).data ?: 0
        }
    }

    fun getActions(
        onGlobalActionSuccess: () -> Unit,
    ): LinkedHashSet<BinGlobalAction> {
        return linkedSetOf(
            BinGlobalAction.RestoreAll {
                _dialogState.value = RestoreAllItemDialogState(
                    actions = listOf(
                        DialogAction.commonCancel { _dialogState.value = null },
                        DialogAction(
                            text = LbcTextSpec.StringResource(OSString.common_restore),
                            type = DialogAction.Type.Normal,
                            onClick = {
                                _dialogState.value = null
                                restoreAll(onGlobalActionSuccess)
                                _snackbarState.value =
                                    MessageSnackBarState(LbcTextSpec.StringResource(OSString.bin_restoreAll_success_message))
                            },
                        ),
                    ),
                    dismiss = { _dialogState.value = null },
                )
            },
            BinGlobalAction.RemoveAll {
                _dialogState.value = RemoveAllItemsDialogState(
                    actions = listOf(
                        DialogAction.commonCancel { _dialogState.value = null },
                        DialogAction(
                            text = LbcTextSpec.StringResource(OSString.common_delete),
                            type = DialogAction.Type.Dangerous,
                            onClick = {
                                _dialogState.value = null
                                removeAll(onGlobalActionSuccess)
                                _snackbarState.value =
                                    MessageSnackBarState(LbcTextSpec.StringResource(OSString.bin_deleteAll_success_message))
                            },
                        ),
                    ),
                    dismiss = { _dialogState.value = null },
                )
            },
        )
    }

    private fun restoreAll(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = restoreItemUseCase(null)
            if (result is LBResult.Success) {
                onSuccess()
            }
        }
    }

    private fun removeAll(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = removeAllDeletedItemUseCase()
            if (result is LBResult.Success) {
                onSuccess()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        getSafeItemActionHelper.close()
    }
}
