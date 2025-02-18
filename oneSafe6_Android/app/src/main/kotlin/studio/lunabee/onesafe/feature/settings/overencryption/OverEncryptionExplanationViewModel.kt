package studio.lunabee.onesafe.feature.settings.overencryption

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.usecase.GetAutoBackupModeUseCase
import javax.inject.Inject

@HiltViewModel
class OverEncryptionExplanationViewModel @Inject constructor(
    getAutoBackupModeUseCase: GetAutoBackupModeUseCase,
) : ViewModel() {
    val isBackupEnabled: StateFlow<Boolean> = getAutoBackupModeUseCase.flow()
        .map { it != AutoBackupMode.Disabled }
        .stateIn(
            viewModelScope,
            CommonUiConstants.Flow.DefaultSharingStarted,
            false,
        )
}
