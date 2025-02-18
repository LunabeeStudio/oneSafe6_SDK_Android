package studio.lunabee.onesafe.feature.itemform.bottomsheet.passwordgenerator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.domain.model.password.GeneratedPassword
import studio.lunabee.onesafe.domain.repository.PasswordGeneratorConfigRepository
import studio.lunabee.onesafe.domain.usecase.GeneratePasswordUseCase
import javax.inject.Inject

@HiltViewModel
class PasswordGeneratorViewModel @Inject constructor(
    private val generatePasswordUseCase: GeneratePasswordUseCase,
    private val passwordGeneratorConfigRepository: PasswordGeneratorConfigRepository,
) : ViewModel() {

    private val _password = MutableStateFlow(GeneratedPassword.default())
    val password: StateFlow<GeneratedPassword> = _password.asStateFlow()

    private val passwordConfig = passwordGeneratorConfigRepository.getConfig().stateIn(
        viewModelScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        null,
    )

    val passwordGeneratorUiState: StateFlow<PasswordGeneratorUiState> = passwordConfig
        .filterNotNull()
        .map { config ->
            PasswordGeneratorUiState.Data(config)
        }.stateIn(
            viewModelScope,
            CommonUiConstants.Flow.DefaultSharingStarted,
            PasswordGeneratorUiState.Initializing,
        )

    fun generatePassword(
        passwordGeneratorUiState: PasswordGeneratorUiState.Data,
    ) {
        viewModelScope.launch {
            val passwordConfig = passwordGeneratorUiState.config()
            // Refresh UI
            _password.value = generatePasswordUseCase(passwordConfig)
            // Store config if changed
            if (passwordConfig != this@PasswordGeneratorViewModel.passwordConfig.value) {
                passwordGeneratorConfigRepository.setConfig(passwordConfig)
            }
        }
    }
}
