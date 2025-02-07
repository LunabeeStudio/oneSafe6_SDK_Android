package studio.lunabee.onesafe.feature.settings.bubbles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.settings.AutoLockBackgroundDelay
import studio.lunabee.onesafe.commonui.settings.AutoLockInactivityDelay
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.domain.usecase.settings.GetAppSettingUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetAppVisitUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetSecuritySettingUseCase
import studio.lunabee.onesafe.domain.usecase.settings.SetAppSettingUseCase
import studio.lunabee.onesafe.domain.usecase.settings.SetSecuritySettingUseCase
import studio.lunabee.onesafe.feature.settings.bubbles.model.BubblesResendMessageDelay
import javax.inject.Inject

@HiltViewModel
class BubblesSettingsViewModel @Inject constructor(
    getAppVisitUseCase: GetAppVisitUseCase,
    getAppSettingUseCase: GetAppSettingUseCase,
    private val setAppSettingUseCase: SetAppSettingUseCase,
    getSecuritySettingUseCase: GetSecuritySettingUseCase,
    private val setSecuritySettingUseCase: SetSecuritySettingUseCase,
    val featureFlags: FeatureFlags,
) : ViewModel() {

    private val bubblesResendMessageDelay: Flow<BubblesResendMessageDelay> = getSecuritySettingUseCase.bubblesResendMessageDelayFlow().map {
        BubblesResendMessageDelay.valueForDuration(it)
    }
    private val inactivityDelay: Flow<AutoLockInactivityDelay> = getSecuritySettingUseCase.autoLockOSKInactivityDelayFlow().map {
        AutoLockInactivityDelay.valueForDuration(it)
    }
    private val hiddenDelay: Flow<AutoLockBackgroundDelay> = getSecuritySettingUseCase.autoLockOSKHiddenDelayFlow().map {
        AutoLockBackgroundDelay.valueForDuration(it)
    }

    val uiState: StateFlow<BubblesSettingsUiState> = combine(
        getAppVisitUseCase.hasFinishOneSafeKOnBoarding(),
        getAppSettingUseCase.bubblesPreview(),
        bubblesResendMessageDelay,
        inactivityDelay,
        hiddenDelay,
    ) { hasFinishOneSafeKOnBoarding, bubblesPreview, resendMessageDelay, inactivityDelay, hiddenDelay ->
        BubblesSettingsUiState(
            hasFinishOneSafeKOnBoarding = hasFinishOneSafeKOnBoarding,
            isBubblesPreviewActivated = bubblesPreview,
            bubblesResendMessageDelay = resendMessageDelay,
            inactivityDelay = inactivityDelay,
            hiddenDelay = hiddenDelay,
        )
    }.stateIn(
        viewModelScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        BubblesSettingsUiState.default(),
    )

    fun setBubblesPreviewActivation(isBubblesActivated: Boolean) {
        viewModelScope.launch {
            setAppSettingUseCase.setBubblesPreview(isBubblesActivated)
        }
    }

    fun setBubblesResendMessageDelay(delay: BubblesResendMessageDelay) {
        viewModelScope.launch {
            setSecuritySettingUseCase.setBubblesResendMessageDelay(delay.value)
        }
    }

    fun setAutoLockInactivityDelay(delay: AutoLockInactivityDelay) {
        viewModelScope.launch {
            setSecuritySettingUseCase.setAutoLockOSKInactivityDelay(delay.value)
        }
    }

    fun setAutoLockHiddenDelay(delay: AutoLockBackgroundDelay) {
        viewModelScope.launch {
            setSecuritySettingUseCase.setAutoLockOSKHiddenDelay(delay.value)
        }
    }
}
