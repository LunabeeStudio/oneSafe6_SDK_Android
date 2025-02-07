package studio.lunabee.onesafe.feature.congratulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.dialog.ErrorDialogState
import studio.lunabee.onesafe.domain.usecase.authentication.IsSafeReadyUseCase
import studio.lunabee.onesafe.domain.usecase.autolock.LockAppUseCase
import studio.lunabee.onesafe.domain.usecase.onboarding.FinishSafeCreationUseCase
import javax.inject.Inject

@HiltViewModel
class CongratulationViewModel @Inject constructor(
    private val finishSafeCreationUseCase: FinishSafeCreationUseCase,
    private val lockAppUseCase: LockAppUseCase,
    private val isSafeReadyUseCase: IsSafeReadyUseCase,
) : ViewModel() {

    private val _dialogState = MutableStateFlow<DialogState?>(value = null)
    val dialogState: StateFlow<DialogState?> get() = _dialogState.asStateFlow()

    private val _uiState = MutableStateFlow<CongratulationUiState>(CongratulationUiState.Finishing)
    val uiState: StateFlow<CongratulationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val result = finishSafeCreationUseCase()
            when (result) {
                is LBResult.Success -> _uiState.value = CongratulationUiState.Idle(isSafeReadyUseCase())
                is LBResult.Failure -> _dialogState.value = ErrorDialogState(
                    result.throwable,
                    actions = listOf(DialogAction.commonOk { _dialogState.value = null }),
                )
            }
        }
    }

    fun lock() {
        viewModelScope.launch {
            lockAppUseCase(false)
        }
    }
}
