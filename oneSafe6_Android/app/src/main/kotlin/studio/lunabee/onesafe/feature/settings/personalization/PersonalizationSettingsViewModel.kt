package studio.lunabee.onesafe.feature.settings.personalization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.usecase.settings.GetAppSettingUseCase
import studio.lunabee.onesafe.domain.usecase.settings.SetAppSettingUseCase
import javax.inject.Inject

@HiltViewModel
class PersonalizationSettingsViewModel @Inject constructor(
    private val setAppSettingUseCase: SetAppSettingUseCase,
    getAppSettingUseCase: GetAppSettingUseCase,
) : ViewModel() {

    val isMaterialYouEnabled: StateFlow<Boolean> = getAppSettingUseCase.materialYou().stateIn(
        viewModelScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        false,
    )

    val isAutomationEnabled: StateFlow<Boolean> = getAppSettingUseCase.automationFlow().stateIn(
        viewModelScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        false,
    )

    val cameraSystem: StateFlow<CameraSystem> = getAppSettingUseCase.cameraSystemFlow().stateIn(
        viewModelScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        CameraSystem.InApp,
    )

    fun toggleMaterialYouSetting() {
        viewModelScope.launch { setAppSettingUseCase.toggleMaterialYou() }
    }

    fun toggleAutomationSetting() {
        viewModelScope.launch { setAppSettingUseCase.toggleAutomation() }
    }
}
