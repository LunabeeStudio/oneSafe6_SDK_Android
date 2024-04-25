package studio.lunabee.onesafe.help.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.domain.usecase.EraseMainStorageUseCase
import javax.inject.Inject

@HiltViewModel
class HelpViewModel @Inject constructor(
    private val eraseMainStorageUseCase: EraseMainStorageUseCase,
) : ViewModel() {

    private val _uiState: MutableStateFlow<HelpScreenUiState> = MutableStateFlow(HelpScreenUiState.Idle)
    val uiState: StateFlow<HelpScreenUiState> = _uiState.asStateFlow()

    fun eraseMainStorage() {
        viewModelScope.launch {
            eraseMainStorageUseCase()
            _uiState.value = HelpScreenUiState.Restart
        }
    }
}
