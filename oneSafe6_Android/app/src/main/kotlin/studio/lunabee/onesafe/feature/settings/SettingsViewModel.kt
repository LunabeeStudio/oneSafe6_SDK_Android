package studio.lunabee.onesafe.feature.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.domain.usecase.authentication.DeleteSafeUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.GetBiometricCipherUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.HasDatabaseKeyUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsCurrentSafeBiometricEnabledUseCase
import studio.lunabee.onesafe.domain.usecase.panicmode.IsPanicDestructionEnabledUseCase
import studio.lunabee.onesafe.domain.usecase.panicmode.IsPanicWidgetInstalledUseCase
import studio.lunabee.onesafe.domain.usecase.settings.DismissIndependentSafeInfoCardUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetAppSettingUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetPreventionWarningCtaStateUseCase
import studio.lunabee.onesafe.domain.usecase.support.HasRatedOSUseCase
import studio.lunabee.onesafe.feature.settings.personalization.ChangeIconUseCase
import studio.lunabee.onesafe.feature.settings.personalization.GetCurrentAliasUseCase
import studio.lunabee.onesafe.feature.settings.prevention.UiPreventionSettingsWarning
import studio.lunabee.onesafe.feature.settings.tabs.SettingsData
import studio.lunabee.onesafe.importexport.usecase.DismissPreventionWarningCtaUseCase
import studio.lunabee.onesafe.model.AppIcon
import studio.lunabee.onesafe.qualifier.AppScope
import javax.inject.Inject

@OptIn(DelicateCoroutinesApi::class)
@HiltViewModel
class SettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getBiometricCipherUseCase: GetBiometricCipherUseCase,
    private val isCurrentSafeBiometricEnabledUseCase: IsCurrentSafeBiometricEnabledUseCase,
    private val hasRatedOSUseCase: HasRatedOSUseCase,
    private val changeIconUseCase: ChangeIconUseCase,
    getCurrentAliasUseCase: GetCurrentAliasUseCase,
    getAppSettingUseCase: GetAppSettingUseCase,
    private val dismissIndependentSafeInfoCard: DismissIndependentSafeInfoCardUseCase,
    private val dismissPreventionWarningCtaUseCase: DismissPreventionWarningCtaUseCase,
    private val getPreventionWarningCtaStateUseCase: GetPreventionWarningCtaStateUseCase,
    private val deleteSafeUseCase: DeleteSafeUseCase,
    featureFlags: FeatureFlags,
    hasDatabaseKeyUseCase: HasDatabaseKeyUseCase,
    @AppScope private val appScope: CoroutineScope,
    private val isPanicWidgetInstalledUseCase: IsPanicWidgetInstalledUseCase,
    isPanicDestructionEnabledUseCase: IsPanicDestructionEnabledUseCase,
) : ViewModel() {
    val currentAliasSelected: AppIcon = getCurrentAliasUseCase()

    private val _uiState: MutableStateFlow<SettingsUiState> = MutableStateFlow(SettingsUiState.Initializing)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val shouldStartChangePasswordFlow: Boolean? = savedStateHandle.get<Boolean>(SettingsDestination.StartChangePasswordFlowArgs)

    private val isPanicWidgetInstalled: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val settingsData: StateFlow<SettingsData> = combine(
        getAppSettingUseCase.independentSafeInfoCtaState()
            .map { it is CtaState.VisibleSince },
        /**
         * @property isOverEncryptionEnabled null means feature flag disabled
         */
        hasDatabaseKeyUseCase().map { hasKey ->
            when {
                hasKey -> true
                !hasKey && featureFlags.sqlcipher() -> false
                else -> null
            }
        },
        isPanicDestructionEnabledUseCase(),
        isPanicWidgetInstalled,
        getPreventionWarningCtaStateUseCase(),
    ) { showIndependentSafeCard, isOverEncryptionEnabled, isPanicDestructionEnabled, isPanicWidgetEnabled, preventionWarnings ->
        SettingsData(
            showIndependentSafeCard = showIndependentSafeCard,
            isOverEncryptionEnabled = isOverEncryptionEnabled,
            isPanicDestructionEnabled = isPanicDestructionEnabled,
            isWidgetEnabled = isPanicWidgetEnabled,
            preventionWarning = preventionWarnings?.let { UiPreventionSettingsWarning.valueOf(preventionWarnings.name) },
        ).also {
            _uiState.value = SettingsUiState.Idle
        }
    }.stateIn(
        viewModelScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        SettingsData.init(),
    )

    init {
        if (shouldStartChangePasswordFlow == true) {
            startChangePasswordFlow()
        }
    }

    fun startChangePasswordFlow() {
        viewModelScope.launch {
            val isBiometricEnabled = isCurrentSafeBiometricEnabledUseCase()
            if (isBiometricEnabled) {
                val cipher = getBiometricCipherUseCase.forVerify().data
                if (cipher == null) {
                    // Redirect to the password authentication in case of error
                    // In case of password change flow, the biometric will be override (or disabled) at the end of the flow
                    _uiState.value = SettingsUiState.ShowPasswordAuthentication
                } else {
                    _uiState.value = SettingsUiState.ShowBiometricAuthentication(cipher)
                }
            } else {
                _uiState.value = SettingsUiState.ShowPasswordAuthentication
            }
        }
    }

    fun onPasswordConfirmed() {
        _uiState.value = SettingsUiState.NavigateChangePassword {
            _uiState.value = SettingsUiState.Idle
        }
    }

    fun resetState() {
        _uiState.value = SettingsUiState.Idle
    }

    fun hasRatedOS() {
        viewModelScope.launch {
            hasRatedOSUseCase()
        }
    }

    fun hideIndependentSafeCard() {
        viewModelScope.launch {
            dismissIndependentSafeInfoCard()
        }
    }

    fun hidePreventionWarningCard() {
        viewModelScope.launch {
            dismissPreventionWarningCtaUseCase()
        }
    }

    fun changeIcon(newAlias: AppIcon) {
        changeIconUseCase(newAlias)
    }

    fun deleteSafe() {
        appScope.launch {
            deleteSafeUseCase()
        }
    }

    fun updatePanicEnabledWidgetState() {
        viewModelScope.launch {
            isPanicWidgetInstalled.value = isPanicWidgetInstalledUseCase()
        }
    }
}
