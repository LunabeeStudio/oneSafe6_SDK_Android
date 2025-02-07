package studio.lunabee.onesafe.feature.move.movehost

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.ErrorNameProvider
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.domain.usecase.item.GetItemWithAncestorsUseCase
import studio.lunabee.onesafe.domain.usecase.move.MoveItemUseCase
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.feature.dialog.MoveActionDialogState
import studio.lunabee.onesafe.feature.move.MoveActionState
import studio.lunabee.onesafe.feature.move.MoveDestinationUiData
import studio.lunabee.onesafe.feature.move.selectdestination.SelectMoveDestination
import studio.lunabee.onesafe.feature.snackbar.MoveSucceedSnackbarState
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MoveHostViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getItemWithAncestorsUseCase: GetItemWithAncestorsUseCase,
    private val decryptUseCase: ItemDecryptUseCase,
    private val safeItemRepository: SafeItemRepository,
    private val moveItemUseCase: MoveItemUseCase,
) : ViewModel() {

    private val itemId: UUID = savedStateHandle.get<String>(MoveHostDestination.ItemIdArgument)!!.let(UUID::fromString)

    private val _uiState: MutableStateFlow<MoveHostUiState> = MutableStateFlow(MoveHostUiState())
    val uiState: StateFlow<MoveHostUiState> = _uiState.asStateFlow()

    private val _moveDestinationItems: MutableStateFlow<ImmutableList<MoveDestinationUiData>> =
        MutableStateFlow(persistentListOf())
    val moveDestinationItems: StateFlow<ImmutableList<MoveDestinationUiData>> = _moveDestinationItems.asStateFlow()

    private val _moveDialogState = MutableStateFlow<DialogState?>(null)
    val moveDialogState: StateFlow<DialogState?> get() = _moveDialogState.asStateFlow()

    private val _moveActionState: MutableStateFlow<MoveActionState> = MutableStateFlow(MoveActionState.Idle)
    val moveActionState: StateFlow<MoveActionState> = _moveActionState

    private var updateJob: Job? = null
    private var initialParentId: UUID? = null

    init {
        viewModelScope.launch {
            initialParentId = safeItemRepository.getSafeItem(itemId).parentId
            var itemToMoveName = ""
            safeItemRepository.getSafeItem(itemId).encName?.let { encName ->
                itemToMoveName = decryptUseCase(encName, itemId, String::class).data.orEmpty()
            }

            _uiState.value = MoveHostUiState(
                itemToMoveName = itemToMoveName,
                initialParentId = initialParentId,
                itemToMoveId = itemId,
            )
        }
    }

    fun updateBreadcrumb(route: String?, arguments: Bundle?) {
        updateJob?.cancel("Multi call")
        updateJob = viewModelScope.launch {
            if (route == SelectMoveDestination.route) {
                val destinationId = arguments?.getString(SelectMoveDestination.DestinationItemIdArgument)?.let(UUID::fromString)
                val path = mutableListOf(MoveDestinationUiData.home())
                if (destinationId != null) {
                    getItemWithAncestorsUseCase(destinationId).data?.mapTo(path) { safeItem ->
                        val itemNameProvider = safeItem.encName?.let {
                            val result = decryptUseCase(it, safeItem.id, String::class)
                            when (result) {
                                is LBResult.Success -> DefaultNameProvider(result.data)
                                is LBResult.Failure -> ErrorNameProvider
                            }
                        } ?: ErrorNameProvider
                        MoveDestinationUiData(safeItem.id, itemNameProvider.name)
                    }
                }
                _moveDestinationItems.value = path.toImmutableList()
            }
            updateJob = null
        }
    }

    fun moveItem(destinationUUID: UUID?) {
        viewModelScope.launch {
            val itemToMove = safeItemRepository.getSafeItem(itemId)
            val destinationName = if (destinationUUID != null) {
                safeItemRepository.getSafeItem(destinationUUID).encName?.let {
                    decryptUseCase(it, destinationUUID, String::class).data
                }
            } else {
                null
            }

            _moveDialogState.value = MoveActionDialogState(
                destinationName = destinationName,
                dismiss = ::dismissDialog,
                confirmAction = {
                    viewModelScope.launch {
                        val result = moveItemUseCase(itemToMove.id, destinationUUID)
                        dismissDialog()
                        when (result) {
                            is LBResult.Success -> {
                                _moveActionState.value =
                                    MoveActionState.NavigateToItem(
                                        itemId = initialParentId,
                                        snackbarState = MoveSucceedSnackbarState(
                                            movedItemId = itemId,
                                            onDismiss = {},
                                        ),
                                    )
                            }
                            is LBResult.Failure -> {
                                _moveActionState.value = MoveActionState.Error(result.throwable)
                            }
                        }
                    }
                },
            )
        }
    }

    private fun dismissDialog() {
        _moveDialogState.value = null
    }

    fun consumeMoveActionState() {
        _moveActionState.value = MoveActionState.Idle
    }
}
