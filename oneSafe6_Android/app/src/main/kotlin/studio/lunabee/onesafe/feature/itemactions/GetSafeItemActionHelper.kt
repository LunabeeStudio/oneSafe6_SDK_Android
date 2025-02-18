package studio.lunabee.onesafe.feature.itemactions

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.ErrorNameProvider
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.dialog.ErrorDialogState
import studio.lunabee.onesafe.commonui.snackbar.MessageSnackBarState
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState
import studio.lunabee.onesafe.commonui.utils.CloseableCoroutineScope
import studio.lunabee.onesafe.commonui.utils.CloseableMainCoroutineScope
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.DuplicateItemUseCase
import studio.lunabee.onesafe.domain.usecase.GetIconUseCase
import studio.lunabee.onesafe.domain.usecase.item.CountSafeItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemToggleFavoriteUseCase
import studio.lunabee.onesafe.domain.usecase.item.MoveToBinItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.RemoveDeletedItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.RestoreItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.SecureGetItemUseCase
import studio.lunabee.onesafe.error.OSAppError
import studio.lunabee.onesafe.feature.dialog.DeleteItemDialogState
import studio.lunabee.onesafe.feature.dialog.DuplicateItemDialogState
import studio.lunabee.onesafe.feature.dialog.RemoveItemDialogState
import studio.lunabee.onesafe.feature.dialog.sharing.ShareItemWithChildrenDialogState
import studio.lunabee.onesafe.feature.itemdetails.model.SafeItemAction
import studio.lunabee.onesafe.feature.snackbar.AddToFavoriteSuccessSnackbarState
import studio.lunabee.onesafe.feature.snackbar.DuplicateSucceedSnackbarState
import studio.lunabee.onesafe.feature.snackbar.RemoveFavoriteSuccessSnackbarState
import studio.lunabee.onesafe.feature.snackbar.RestoreSucceedSnackbarState
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

private val logger = LBLogger.get<GetSafeItemActionHelper>()

interface GetSafeItemActionUiStateDelegate {
    val itemActionSnackbarState: Flow<SnackbarState?>
    val itemActionDialogState: StateFlow<DialogState?>
    val navigationAction: Flow<QuickActionNavigation?>
}

@ViewModelScoped
class GetSafeItemActionHelper @Inject constructor(
    private val safeItemRepository: SafeItemRepository,
    private val decryptUseCase: ItemDecryptUseCase,
    private val getIconUseCase: GetIconUseCase,
    private val moveToBinItemUseCase: MoveToBinItemUseCase,
    private val duplicateItemUseCase: DuplicateItemUseCase,
    private val itemToggleFavoriteUseCase: ItemToggleFavoriteUseCase,
    private val removeDeletedItemUseCase: RemoveDeletedItemUseCase,
    private val restoreItemUseCase: RestoreItemUseCase,
    private val getItemUseCase: SecureGetItemUseCase,
    private val countSafeItemInParentUseCase: CountSafeItemUseCase,
    private val featureFlags: FeatureFlags,
) : GetSafeItemActionUiStateDelegate, CloseableCoroutineScope by CloseableMainCoroutineScope() {

    private val _navigationAction: MutableStateFlow<QuickActionNavigation?> = MutableStateFlow(null)
    override val navigationAction: Flow<QuickActionNavigation?> = _navigationAction

    private val _dialogState = MutableStateFlow<DialogState?>(null)
    override val itemActionDialogState: StateFlow<DialogState?> get() = _dialogState.asStateFlow()

    // SharedFlow and not StateFlow to prevent the snackbar to be collected and displayed multiple times
    private val _snackbarState = MutableSharedFlow<SnackbarState?>()
    override val itemActionSnackbarState: Flow<SnackbarState?> get() = _snackbarState

    var isDeleting: AtomicBoolean = AtomicBoolean(false)
        private set

    fun getQuickActions(
        itemId: UUID,
        isItemDetail: Boolean = false,
    ): suspend () -> List<SafeItemAction> {
        return {
            val item = getItemUseCase(itemId).first()
            if (item == null) {
                listOf()
            } else {
                val childrenCount: Int = countSafeItemInParentUseCase.flow(item).first()
                val data = ItemDetailsData.fromSafeItem(item, decryptUseCase, getIconUseCase)
                val itemNameProvider = if (data.nameResult is LBResult.Failure) {
                    ErrorNameProvider
                } else {
                    OSNameProvider.fromName(
                        name = data.nameResult?.data,
                        hasIcon = data.iconResult?.data != null,
                    )
                }
                val itemDescendantsCount = safeItemRepository.findByIdWithChildren(item.id).size - 1
                when (data) {
                    is ItemDetailsData.Default -> {
                        val deleteAction = getDeleteAction(childrenCount, itemNameProvider, data.hasCorruptedData, item, isItemDetail)
                        val favoriteAction = getFavoriteAction(data.isFavorite, item.id, isItemDetail, itemNameProvider)
                        if (data.hasCorruptedData) {
                            listOf(favoriteAction, deleteAction)
                        } else {
                            val duplicateAction = getDuplicateAction(itemNameProvider, item)
                            val shareAction = getShareAction(itemDescendantsCount, item.id)
                            val sendViaBubbles = if (featureFlags.bubbles()) {
                                getSendViaBubbles(itemDescendantsCount, item.id)
                            } else {
                                null
                            }
                            val moveAction = getMoveAction(item.id)
                            listOfNotNull(favoriteAction, duplicateAction, shareAction, sendViaBubbles, moveAction, deleteAction)
                        }
                    }
                    is ItemDetailsData.Deleted -> listOf(
                        getRestoreAction(item.id, isItemDetail),
                        getRemoveAction(childrenCount, itemNameProvider, data.hasCorruptedData, item, isItemDetail),
                    )
                }
            }
        }
    }

    private fun getShareAction(descendantsCount: Int, safeItemId: UUID) = if (descendantsCount > 0) {
        SafeItemAction.Share {
            _dialogState.value = ShareItemWithChildrenDialogState(
                dismiss = ::dismissDialog,
                onClickOnIncludeChildren = {
                    _navigationAction.value = QuickActionNavigation.Share(
                        safeItemId = safeItemId,
                        includeChildren = true,
                        consumeState = { _navigationAction.value = null },
                    )
                    dismissDialog()
                },
                onClickOnThisItemOnly = {
                    _navigationAction.value =
                        QuickActionNavigation.Share(
                            safeItemId = safeItemId,
                            includeChildren = false,
                            consumeState = { _navigationAction.value = null },
                        )
                    dismissDialog()
                },
                subItemCount = descendantsCount,
            )
        }
    } else {
        SafeItemAction.Share {
            _navigationAction.value = QuickActionNavigation.Share(
                safeItemId = safeItemId,
                includeChildren = false,
                consumeState = { _navigationAction.value = null },
            )
        }
    }

    private fun getSendViaBubbles(descendantsCount: Int, safeItemId: UUID) = if (descendantsCount > 0) {
        SafeItemAction.SendViaBubbles {
            _dialogState.value = ShareItemWithChildrenDialogState(
                dismiss = ::dismissDialog,
                onClickOnIncludeChildren = {
                    _navigationAction.value = QuickActionNavigation.SendViaBubbles(
                        safeItemId = safeItemId,
                        includeChildren = true,
                        consumeState = { _navigationAction.value = null },
                    )
                    dismissDialog()
                },
                onClickOnThisItemOnly = {
                    _navigationAction.value =
                        QuickActionNavigation.SendViaBubbles(
                            safeItemId = safeItemId,
                            includeChildren = false,
                            consumeState = { _navigationAction.value = null },
                        )
                    dismissDialog()
                },
                subItemCount = descendantsCount,
            )
        }
    } else {
        SafeItemAction.SendViaBubbles {
            _navigationAction.value = QuickActionNavigation.SendViaBubbles(
                safeItemId = safeItemId,
                includeChildren = false,
                consumeState = { _navigationAction.value = null },
            )
        }
    }

    private fun getDeleteAction(
        childrenCount: Int,
        itemNameProvider: OSNameProvider,
        hasCorruptedData: Boolean,
        item: SafeItem,
        isItemDetail: Boolean,
    ) = SafeItemAction.Delete {
        _dialogState.value = DeleteItemDialogState(
            itemNameProvider = itemNameProvider,
            deleteAction = { deleteUsingUseCase(deleteUseCase = { moveToBinItemUseCase(item) }, item = item, isItemDetail = isItemDetail) },
            dismiss = ::dismissDialog,
            isCorrupted = hasCorruptedData,
            childrenCount = childrenCount,
        )
    }

    private fun getRemoveAction(
        childrenCount: Int,
        itemNameProvider: OSNameProvider,
        hasCorruptedData: Boolean,
        item: SafeItem,
        isItemDetail: Boolean,
    ) = SafeItemAction.Remove {
        _dialogState.value = RemoveItemDialogState(
            itemNameProvider = itemNameProvider,
            removeAction = {
                deleteUsingUseCase(
                    deleteUseCase = removeDeletedItemUseCase::invoke,
                    isPermanent = true,
                    item = item,
                    isItemDetail = isItemDetail,
                )
            },
            dismiss = ::dismissDialog,
            isCorrupted = hasCorruptedData,
            childrenCount = childrenCount,
        )
    }

    private fun getFavoriteAction(
        isFavorite: Boolean,
        itemId: UUID,
        isItemDetail: Boolean,
        itemNameProvider: OSNameProvider,
    ): SafeItemAction {
        val snackbarState = when {
            isItemDetail -> null
            isFavorite -> RemoveFavoriteSuccessSnackbarState(itemNameProvider)
            else -> AddToFavoriteSuccessSnackbarState(itemNameProvider)
        }
        val toggleFavorite: () -> Unit = {
            coroutineScope.launch {
                _snackbarState.emit(snackbarState)
                itemToggleFavoriteUseCase(itemId)
            }
        }
        return if (isFavorite) {
            SafeItemAction.RemoveFromFavorites(toggleFavorite)
        } else {
            SafeItemAction.AddToFavorites(toggleFavorite)
        }
    }

    fun getRestoreAction(itemId: UUID, isItemDetail: Boolean): SafeItemAction = SafeItemAction.Restore {
        coroutineScope.launch {
            val result = restoreItemUseCase(itemId)
            if (result is LBResult.Success) {
                _snackbarState.emit(RestoreSucceedSnackbarState(itemId) {})
                if (isItemDetail) _navigationAction.value = QuickActionNavigation.NavigateBack
            } else if (result is LBResult.Failure) {
                _dialogState.value = ErrorDialogState(
                    error = result.throwable,
                    actions = listOf(DialogAction.commonOk(::dismissDialog)),
                )
            }
        }
    }

    private fun getDuplicateAction(itemNameProvider: OSNameProvider, item: SafeItem) = SafeItemAction.Duplicate {
        coroutineScope.launch {
            val parentItemName = getParentItemName(item)
            _dialogState.value = DuplicateItemDialogState(
                itemName = itemNameProvider,
                parentName = parentItemName,
                duplicate = {
                    dismissDialog()
                    coroutineScope.launch {
                        val duplicatedItem = duplicateItemUseCase(item.id)
                        when (duplicatedItem) {
                            is LBResult.Failure -> _dialogState.value = ErrorDialogState(
                                duplicatedItem.throwable,
                                actions = listOf(DialogAction.commonOk(::dismissDialog)),
                            )
                            is LBResult.Success -> {
                                _snackbarState.emit(
                                    DuplicateSucceedSnackbarState(duplicatedItem.successData.id) {
                                        coroutineScope.launch { _snackbarState.emit(null) }
                                    },
                                )
                            }
                        }
                    }
                },
                dismiss = ::dismissDialog,
            )
        }
    }

    private suspend fun getParentItemName(item: SafeItem): OSNameProvider? {
        return item.parentId?.let { parentId ->
            val encParentName = safeItemRepository.getSafeItemName(id = parentId)
            if (encParentName != null) {
                val result = decryptUseCase(encParentName, parentId, String::class)
                when (result) {
                    is LBResult.Failure -> null // Error logged in usecase
                    is LBResult.Success -> DefaultNameProvider(result.successData)
                }
            } else {
                val error = OSAppError(OSAppError.Code.NO_ITEM_FOUND_FOR_ID, "No parent item found with id $parentId")
                logger.e(error)
                null
            }
        }
    }

    private fun deleteUsingUseCase(
        deleteUseCase: suspend (SafeItem) -> LBResult<Unit>,
        isPermanent: Boolean = false,
        item: SafeItem,
        isItemDetail: Boolean,
    ) {
        dismissDialog()
        coroutineScope.launch {
            isDeleting.set(true)
            val result = deleteUseCase(item)
            if (result is LBResult.Success) {
                val messageState = if (isPermanent) {
                    MessageSnackBarState(LbcTextSpec.StringResource(OSString.safeItemDetail_delete_success_message))
                } else {
                    MessageSnackBarState(LbcTextSpec.StringResource(OSString.safeItemDetail_moveIntoBin_success_message))
                }
                _snackbarState.emit(messageState)
                if (isItemDetail) {
                    _navigationAction.value = QuickActionNavigation.NavigateBack
                }
            } else if (result is LBResult.Failure) {
                _dialogState.value = dialogErrorState(result)
            }
            isDeleting.set(false)
        }
    }

    private fun getMoveAction(itemId: UUID) = SafeItemAction.Move {
        _navigationAction.value = QuickActionNavigation.Move(
            safeItemId = itemId,
            consumeState = { _navigationAction.value = null },
        )
    }

    private fun dismissDialog() {
        _dialogState.value = null
    }

    private fun dialogErrorState(result: LBResult.Failure<Unit>) = ErrorDialogState(
        result.throwable,
        actions = listOf(DialogAction.commonOk(::dismissDialog)),
    )
}

sealed interface QuickActionNavigation {

    class Move(
        val safeItemId: UUID,
        val consumeState: () -> Unit,
    ) : QuickActionNavigation

    class Share(
        val safeItemId: UUID,
        val includeChildren: Boolean,
        val consumeState: () -> Unit,
    ) : QuickActionNavigation

    class SendViaBubbles(
        val safeItemId: UUID,
        val includeChildren: Boolean,
        val consumeState: () -> Unit,
    ) : QuickActionNavigation

    data object NavigateBack : QuickActionNavigation
}
