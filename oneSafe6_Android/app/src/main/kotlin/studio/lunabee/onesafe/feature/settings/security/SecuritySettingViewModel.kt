package studio.lunabee.onesafe.feature.settings.security

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.settings.AutoLockBackgroundDelay
import studio.lunabee.onesafe.commonui.settings.AutoLockInactivityDelay
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.domain.usecase.authentication.DisableBiometricUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.EnableBiometricUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.GetBiometricCipherUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsCurrentSafeBiometricEnabledUseCase
import studio.lunabee.onesafe.domain.usecase.autodestruction.IsAutoDestructionEnabledUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetAppSettingUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetSecuritySettingUseCase
import studio.lunabee.onesafe.domain.usecase.settings.SetAppSettingUseCase
import studio.lunabee.onesafe.domain.usecase.settings.SetSecuritySettingUseCase
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.feature.clipboard.model.ClipboardClearDelay
import studio.lunabee.onesafe.feature.screenshot.ToggleScreenshotSettingUseCase
import studio.lunabee.onesafe.usecase.ShakeToLockUseCase
import javax.crypto.Cipher
import javax.inject.Inject
import kotlin.time.Duration

@HiltViewModel
class SecuritySettingViewModel @Inject constructor(
    isCurrentSafeBiometricEnabledUseCase: IsCurrentSafeBiometricEnabledUseCase,
    private val getBiometricCipherUseCase: GetBiometricCipherUseCase,
    private val disableBiometricUseCase: DisableBiometricUseCase,
    private val enableBiometricUseCase: EnableBiometricUseCase,
    getSecuritySettingUseCase: GetSecuritySettingUseCase,
    private val setSecuritySettingUseCase: SetSecuritySettingUseCase,
    getAppSettingUseCase: GetAppSettingUseCase,
    private val setAppSettingUseCase: SetAppSettingUseCase,
    private val toggleScreenshotSettingUseCase: ToggleScreenshotSettingUseCase,
    private val shakeToLockUseCase: ShakeToLockUseCase,
    private val isAutoDestructionEnabledUseCase: IsAutoDestructionEnabledUseCase,
) : ViewModel() {

    val cameraSystem: StateFlow<CameraSystem> = getAppSettingUseCase.cameraSystemFlow().stateIn(
        viewModelScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        CameraSystem.InApp,
    )

    val settingData: StateFlow<SecuritySettingData> = combine(
        isCurrentSafeBiometricEnabledUseCase.flow(),
        getSecuritySettingUseCase.clipboardDelayFlow(),
        getSecuritySettingUseCase.autoLockInactivityDelayFlow(),
        getSecuritySettingUseCase.autoLockAppChangeDelayFlow(),
        getAppSettingUseCase.allowScreenshotFlow(),
        getSecuritySettingUseCase.verifyPasswordIntervalFlow(),
        getSecuritySettingUseCase.shakeToLockFlow(),
        isAutoDestructionEnabledUseCase.flow(),
    ) { combinedFlow ->
        SecuritySettingData(
            isBiometricEnabled = combinedFlow[0] as Boolean,
            copyBoardClearDelay = ClipboardClearDelay.valueForDuration(combinedFlow[1] as Duration),
            autoLockInactivityDelay = AutoLockInactivityDelay.valueForDuration(combinedFlow[2] as Duration),
            autoLockAppChangeDelay = AutoLockBackgroundDelay.valueForDuration(combinedFlow[3] as Duration),
            isScreenshotAllowed = combinedFlow[4] as Boolean,
            verifyPasswordInterval = combinedFlow[5] as VerifyPasswordInterval,
            shakeToLockEnabled = combinedFlow[6] as Boolean,
            isAutoDestructionEnabled = combinedFlow[7] as Boolean,
        )
    }.stateIn(
        viewModelScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        SecuritySettingData.init(),
    )

    fun toggleShakeToLockSetting() {
        viewModelScope.launch {
            val toggle = !settingData.value.shakeToLockEnabled
            setSecuritySettingUseCase.toggleShakeToLock()
            shakeToLockUseCase(toggle)
        }
    }

    fun toggleScreenshotAllowedSetting(activity: Activity) {
        viewModelScope.launch {
            toggleScreenshotSettingUseCase(activity)
        }
    }

    private val _uiState: MutableStateFlow<SecuritySettingUiState> = MutableStateFlow(SecuritySettingUiState())
    val uiState: StateFlow<SecuritySettingUiState> = _uiState.asStateFlow()

    fun getBiometricCipher(): Cipher? = getBiometricCipherUseCase.forCreate().data

    fun disableBiometric() {
        viewModelScope.launch {
            disableBiometricUseCase()
        }
    }

    fun enableBiometric(cipher: Cipher) {
        viewModelScope.launch {
            val result = enableBiometricUseCase(cipher)
            if (result is LBResult.Failure) {
                (result.throwable as? OSError)?.let {
                    _uiState.value = uiState.value.copy(
                        screenResult = SecuritySettingUiState.ScreenResult.Error(it),
                    )
                }
            }
        }
    }

    fun resetError() {
        _uiState.value = uiState.value.copy(screenResult = SecuritySettingUiState.ScreenResult.Idle)
    }

    fun setClipBoardClearDelay(clearDelay: ClipboardClearDelay) {
        viewModelScope.launch {
            setSecuritySettingUseCase.setClipboardClearDelay(clearDelay.value)
        }
    }

    fun setAutoLockInactivityDelay(inactivityDelay: AutoLockInactivityDelay) {
        viewModelScope.launch {
            setSecuritySettingUseCase.setAutoLockInactivityDelay(inactivityDelay.value)
        }
    }

    fun setAutoLockAppChangeDelay(appChangeDelay: AutoLockBackgroundDelay) {
        viewModelScope.launch {
            setSecuritySettingUseCase.setAutoLockAppChangeDelay(appChangeDelay.value)
        }
    }

    fun setVerifyPasswordInterval(passwordInterval: VerifyPasswordInterval) {
        viewModelScope.launch {
            setSecuritySettingUseCase.setVerifyPasswordInterval(passwordInterval)
        }
    }

    fun setCameraSystem(value: CameraSystem) {
        viewModelScope.launch {
            setAppSettingUseCase.setCameraSystem(value)
        }
    }
}
