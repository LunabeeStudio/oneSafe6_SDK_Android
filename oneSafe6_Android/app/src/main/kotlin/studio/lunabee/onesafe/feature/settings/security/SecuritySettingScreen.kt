package studio.lunabee.onesafe.feature.settings.security

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.common.extensions.hasBiometric
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.biometric.DisplayBiometricLabels
import studio.lunabee.onesafe.commonui.biometric.biometricPrompt
import studio.lunabee.onesafe.commonui.extension.biometricHardware
import studio.lunabee.onesafe.commonui.extension.findFragmentActivity
import studio.lunabee.onesafe.commonui.snackbar.ErrorSnackbarState
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.error.OSAppError
import studio.lunabee.onesafe.feature.autolock.composable.AutoLockAppChangeDelayBottomSheet
import studio.lunabee.onesafe.feature.autolock.composable.AutoLockInactivityDelayBottomSheet
import studio.lunabee.onesafe.feature.clipboard.composable.ClipboardClearDelayBottomSheet
import studio.lunabee.onesafe.feature.enterpassword.EnterPasswordBottomSheet
import studio.lunabee.onesafe.feature.settings.personalization.CameraSystemBottomSheet
import studio.lunabee.onesafe.feature.settings.settingcard.impl.AutoDestructionSettingsCard
import studio.lunabee.onesafe.feature.settings.settingcard.impl.AutoLockSettingCard
import studio.lunabee.onesafe.feature.settings.settingcard.impl.BiometricSettingCard
import studio.lunabee.onesafe.feature.settings.settingcard.impl.ClipboardClearSettingCard
import studio.lunabee.onesafe.feature.settings.settingcard.impl.OtherOptionsSettingCard
import studio.lunabee.onesafe.feature.settings.settingcard.impl.ShakeToLockSettingCard
import studio.lunabee.onesafe.feature.settings.settingcard.impl.VerifyPasswordSettingCard
import studio.lunabee.onesafe.feature.verifypassword.VerifyPasswordIntervalBottomSheet
import studio.lunabee.onesafe.molecule.ElevatedTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.extensions.topAppBarElevation
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

context(SecuritySettingNavigation)
@Composable
fun SecuritySettingRoute(
    showSnackBar: (visuals: SnackbarVisuals) -> Unit,
    viewModel: SecuritySettingViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val settingData by viewModel.settingData.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var isClipboardClearDelayBottomSheetVisible by rememberSaveable { mutableStateOf(value = false) }
    var isAutoLockInactivityDelayBottomSheetVisible by rememberSaveable { mutableStateOf(value = false) }
    var isAutoLockAppChangeDelayBottomSheetVisible by rememberSaveable { mutableStateOf(value = false) }
    var isVerifyPasswordIntervalBottomSheetVisible by rememberSaveable { mutableStateOf(value = false) }
    var isEnterPasswordBottomSheetVisible by rememberSaveable { mutableStateOf(value = false) }

    (uiState.screenResult as? SecuritySettingUiState.ScreenResult.Error)?.let {
        val snackbarVisualsError = ErrorSnackbarState(
            error = it.osError,
            onClick = {},
        ).snackbarVisuals
        showSnackBar(snackbarVisualsError)
        viewModel.resetError()
    }

    val snackbarVisualsFailedBiometric = ErrorSnackbarState(
        error = OSAppError(OSAppError.Code.BIOMETRIC_ERROR),
        onClick = {},
    ).snackbarVisuals

    val authenticate: suspend () -> Unit = biometricPrompt(
        labels = DisplayBiometricLabels.SignUp(context.biometricHardware()),
        getCipher = viewModel::getBiometricCipher,
        onSuccess = viewModel::enableBiometric,
        onUserCancel = viewModel::disableBiometric,
        onFailure = {
            viewModel.disableBiometric()
            showSnackBar(snackbarVisualsFailedBiometric)
        },
    )

    val selectedCameraSystem by viewModel.cameraSystem.collectAsStateWithLifecycle()
    var isCameraSystemBottomSheetVisible by rememberSaveable { mutableStateOf(value = false) }

    CameraSystemBottomSheet(
        isVisible = isCameraSystemBottomSheetVisible,
        onBottomSheetClosed = { isCameraSystemBottomSheetVisible = false },
        onSelect = viewModel::setCameraSystem,
        selectedCameraSystem = selectedCameraSystem,
    )

    ClipboardClearDelayBottomSheet(
        isVisible = isClipboardClearDelayBottomSheetVisible,
        onSelect = { clipboardClearDelay ->
            isClipboardClearDelayBottomSheetVisible = false
            viewModel.setClipBoardClearDelay(clipboardClearDelay)
        },
        onBottomSheetClosed = {
            isClipboardClearDelayBottomSheetVisible = false
        },
        selectedClipboardClearDelay = settingData.copyBoardClearDelay,
    )

    AutoLockInactivityDelayBottomSheet(
        isVisible = isAutoLockInactivityDelayBottomSheetVisible,
        onSelect = viewModel::setAutoLockInactivityDelay,
        onBottomSheetClosed = { isAutoLockInactivityDelayBottomSheetVisible = false },
        selectedAutoLockInactivityDelay = settingData.autoLockInactivityDelay,
    )

    AutoLockAppChangeDelayBottomSheet(
        isVisible = isAutoLockAppChangeDelayBottomSheetVisible,
        onSelect = viewModel::setAutoLockAppChangeDelay,
        onBottomSheetClosed = { isAutoLockAppChangeDelayBottomSheetVisible = false },
        selectedAutoLockAppChangeDelay = settingData.autoLockAppChangeDelay,
    )

    VerifyPasswordIntervalBottomSheet(
        isVisible = isVerifyPasswordIntervalBottomSheetVisible,
        onBottomSheetClosed = { isVerifyPasswordIntervalBottomSheetVisible = false },
        selectedInterval = settingData.verifyPasswordInterval,
        onSelectInterval = viewModel::setVerifyPasswordInterval,
    )

    EnterPasswordBottomSheet(
        isVisible = isEnterPasswordBottomSheetVisible,
        onBottomSheetClosed = { isEnterPasswordBottomSheetVisible = false },
        onConfirm = {
            isEnterPasswordBottomSheetVisible = false
            coroutineScope.launch {
                authenticate.invoke()
            }
        },
    )
    SecuritySettingScreen(
        navigateBack = navigateBack,
        toggleBiometric = { value ->
            if (value) {
                isEnterPasswordBottomSheetVisible = true
            } else {
                viewModel.disableBiometric()
            }
        },
        settingData = settingData,
        openClipboardClearDelayBottomSheet = { isClipboardClearDelayBottomSheetVisible = true },
        openAutoLockInactivityDelayBottomSheet = { isAutoLockInactivityDelayBottomSheetVisible = true },
        openAutoLockAppChangeDelayBottomSheet = { isAutoLockAppChangeDelayBottomSheetVisible = true },
        toggleAllowScreenshot = { viewModel.toggleScreenshotAllowedSetting(context.findFragmentActivity()) },
        toggleShakeToLock = viewModel::toggleShakeToLockSetting,
        openVerifyPasswordIntervalBottomSheet = { isVerifyPasswordIntervalBottomSheetVisible = true },
        onSetCameraSystemClick = { isCameraSystemBottomSheetVisible = true },
        selectedCameraSystem = selectedCameraSystem,
        shakeIsAvailable = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null,
        navigateToAutoDestructionSetting = navigateToAutoDestructionSetting,
    )
}

@Composable
fun SecuritySettingScreen(
    navigateBack: () -> Unit,
    toggleBiometric: (Boolean) -> Unit,
    settingData: SecuritySettingData,
    openAutoLockInactivityDelayBottomSheet: () -> Unit,
    openAutoLockAppChangeDelayBottomSheet: () -> Unit,
    openClipboardClearDelayBottomSheet: () -> Unit,
    openVerifyPasswordIntervalBottomSheet: () -> Unit,
    navigateToAutoDestructionSetting: () -> Unit,
    toggleAllowScreenshot: () -> Unit,
    toggleShakeToLock: () -> Unit,
    onSetCameraSystemClick: () -> Unit,
    selectedCameraSystem: CameraSystem,
    shakeIsAvailable: Boolean,
    context: Context = LocalContext.current,
) {
    val lazyListState: LazyListState = rememberLazyListState()

    OSScreen(
        testTag = UiConstants.TestTag.Screen.Settings,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(top = OSDimens.ItemTopBar.Height)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = OSDimens.SystemSpacing.Regular,
                vertical = OSDimens.SystemSpacing.ExtraLarge,
            ),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
        ) {
            if (context.hasBiometric()) {
                item(
                    key = OSString.settings_security_section_fastId_title,
                ) {
                    BiometricSettingCard(
                        modifier = Modifier.fillMaxWidth(),
                        isBiometricActivated = settingData.isBiometricEnabled,
                        toggleBiometric = toggleBiometric,
                    )
                }
            }

            if (settingData.isBiometricEnabled) {
                item(
                    key = OSString.settings_security_section_verifyPassword_title,
                ) {
                    VerifyPasswordSettingCard(
                        interval = settingData.verifyPasswordInterval,
                        openIntervalBottomSheet = openVerifyPasswordIntervalBottomSheet,
                    )
                }
            }

            item(
                key = OSString.settings_security_section_autodestruction_title,
            ) {
                AutoDestructionSettingsCard(
                    isAutoDestructionEnabled = settingData.isAutoDestructionEnabled,
                    onActionClick = navigateToAutoDestructionSetting,
                )
            }

            item(
                key = OSString.settings_security_section_autolock_title,
            ) {
                AutoLockSettingCard(
                    appChangeDelay = settingData.autoLockAppChangeDelay,
                    inactivityDelay = settingData.autoLockInactivityDelay,
                    showAppChangeDelayBottomSheet = openAutoLockAppChangeDelayBottomSheet,
                    showInactivityDelayBottomSheet = openAutoLockInactivityDelayBottomSheet,
                )
            }

            if (shakeIsAvailable) {
                item(
                    key = OSString.settings_security_section_shake_title,
                ) {
                    ShakeToLockSettingCard(
                        shakeToLockEnabled = settingData.shakeToLockEnabled,
                        toggleShakeToLock = toggleShakeToLock,
                    )
                }
            }

            item(
                key = OSString.settings_security_section_clipboard_title,
            ) {
                ClipboardClearSettingCard(settingData.copyBoardClearDelay, openClipboardClearDelayBottomSheet)
            }

            item(
                key = OSString.settings_security_section_other_options,
            ) {
                OtherOptionsSettingCard(
                    isScreenshotAllowed = settingData.isScreenshotAllowed,
                    toggleAllowScreenshot = toggleAllowScreenshot,
                    onSetCameraClick = onSetCameraSystemClick,
                    selectedCameraSystem = selectedCameraSystem,
                )
            }
        }
        ElevatedTopAppBar(
            title = LbcTextSpec.StringResource(OSString.settings_security_title),
            options = listOf(topAppBarOptionNavBack(navigateBack)),
            elevation = lazyListState.topAppBarElevation,
        )
    }
}

@Composable
@OsDefaultPreview
private fun SecuritySettingPreview() {
    OSPreviewOnSurfaceTheme {
        SecuritySettingScreen(
            navigateBack = {},
            toggleBiometric = {},
            settingData = SecuritySettingData.init(),
            openAutoLockInactivityDelayBottomSheet = {},
            openAutoLockAppChangeDelayBottomSheet = {},
            openClipboardClearDelayBottomSheet = {},
            openVerifyPasswordIntervalBottomSheet = {},
            toggleAllowScreenshot = {},
            toggleShakeToLock = {},
            onSetCameraSystemClick = {},
            selectedCameraSystem = CameraSystem.External,
            shakeIsAvailable = true,
            navigateToAutoDestructionSetting = {},
        )
    }
}
