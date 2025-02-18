package studio.lunabee.onesafe.feature.settings

import android.content.Context
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.common.extensions.hasBiometric
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.settings.AutoLockBackgroundDelay
import studio.lunabee.onesafe.commonui.settings.AutoLockInactivityDelay
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.feature.clipboard.model.ClipboardClearDelay
import studio.lunabee.onesafe.feature.settings.security.SecuritySettingData
import studio.lunabee.onesafe.feature.settings.security.SecuritySettingScreen

@OptIn(ExperimentalTestApi::class)
class SecuritySettingScreenTest : LbcComposeTest() {

    private val navigateBack: () -> Unit = spyk({ })
    private val toggleBiometry: (Boolean) -> Unit = spyk({ })
    private val toggleShakeToLock: () -> Unit = spyk({ })

    @Test
    fun biometry_not_available_test() {
        // Order is important. Mock context with hasBiometric method.
        mockkStatic(Context::hasBiometric)
        val context: Context = mockk<Context>().apply {
            every { hasBiometric() } returns false
        }
        invoke {
            setContent {
                SecuritySettingScreen(
                    navigateBack = navigateBack,
                    toggleBiometric = toggleBiometry,
                    settingData = SecuritySettingData.init(),
                    openClipboardClearDelayBottomSheet = {},
                    openAutoLockInactivityDelayBottomSheet = {},
                    openAutoLockAppChangeDelayBottomSheet = {},
                    toggleAllowScreenshot = {},
                    openVerifyPasswordIntervalBottomSheet = {},
                    onSetCameraSystemClick = {},
                    selectedCameraSystem = CameraSystem.InApp,
                    context = context,
                    toggleShakeToLock = toggleShakeToLock,
                    shakeIsAvailable = true,
                    navigateToAutoDestructionSetting = {},
                )
            }
            onNodeWithText(text = getString(OSString.settings_security_section_fastId_title))
                .assertDoesNotExist()

            onNodeWithText(getString(OSString.settings_security_section_verifyPassword_title)).assertDoesNotExist()
        }
    }

    @Test
    fun biometry_available_disable_to_enable_test() {
        // Order is important. Mock context with hasBiometric method.
        mockkStatic(Context::hasBiometric)
        val context: Context = mockk<Context>().apply {
            every { hasBiometric() } returns true
        }
        invoke {
            setContent {
                SecuritySettingScreen(
                    navigateBack = navigateBack,
                    toggleBiometric = toggleBiometry,
                    settingData = SecuritySettingData.init(),
                    openClipboardClearDelayBottomSheet = {},
                    context = context,
                    openAutoLockInactivityDelayBottomSheet = {},
                    openAutoLockAppChangeDelayBottomSheet = {},
                    toggleAllowScreenshot = {},
                    openVerifyPasswordIntervalBottomSheet = {},
                    onSetCameraSystemClick = {},
                    selectedCameraSystem = CameraSystem.InApp,
                    toggleShakeToLock = toggleShakeToLock,
                    shakeIsAvailable = true,
                    navigateToAutoDestructionSetting = {},
                )
            }

            onNodeWithText(text = getString(OSString.settings_security_section_fastId_useBiometric_label))
                .assertIsDisplayed()
                .performClick()

            verify(exactly = 1) { toggleBiometry(true) }
        }
    }

    @Test
    fun biometry_available_enable_to_disable_test() {
        // Order is important. Mock context with hasBiometric method.
        mockkStatic(Context::hasBiometric)
        val context: Context = mockk<Context>().apply {
            every { hasBiometric() } returns true
        }
        invoke {
            setContent {
                SecuritySettingScreen(
                    navigateBack = navigateBack,
                    toggleBiometric = toggleBiometry,
                    settingData = SecuritySettingData(
                        isBiometricEnabled = true,
                        copyBoardClearDelay = ClipboardClearDelay.THIRTY_SECONDS,
                        autoLockInactivityDelay = AutoLockInactivityDelay.THIRTY_SECONDS,
                        autoLockAppChangeDelay = AutoLockBackgroundDelay.TEN_SECONDS,
                        isScreenshotAllowed = false,
                        shakeToLockEnabled = false,
                        verifyPasswordInterval = VerifyPasswordInterval.EVERY_MONTH,
                        isAutoDestructionEnabled = false,
                    ),
                    openClipboardClearDelayBottomSheet = {},
                    context = context,
                    openAutoLockInactivityDelayBottomSheet = {},
                    openAutoLockAppChangeDelayBottomSheet = {},
                    toggleAllowScreenshot = {},
                    toggleShakeToLock = toggleShakeToLock,
                    openVerifyPasswordIntervalBottomSheet = {},
                    onSetCameraSystemClick = {},
                    selectedCameraSystem = CameraSystem.InApp,
                    shakeIsAvailable = true,
                    navigateToAutoDestructionSetting = {},
                )
            }

            onNodeWithText(getString(OSString.settings_security_section_verifyPassword_title)).assertIsDisplayed()

            onNodeWithText(text = getString(OSString.settings_security_section_fastId_useBiometric_label))
                .assertIsDisplayed()
                .performClick()

            verify(exactly = 1) { toggleBiometry(false) }
        }
    }

    @Test
    fun shake_is_not_available() {
        // Order is important. Mock context with hasBiometric method.
        mockkStatic(Context::hasBiometric)
        val context: Context = mockk<Context>().apply {
            every { hasBiometric() } returns false
        }
        invoke {
            setContent {
                SecuritySettingScreen(
                    navigateBack = navigateBack,
                    toggleBiometric = toggleBiometry,
                    settingData = SecuritySettingData(
                        isBiometricEnabled = true,
                        copyBoardClearDelay = ClipboardClearDelay.THIRTY_SECONDS,
                        autoLockInactivityDelay = AutoLockInactivityDelay.THIRTY_SECONDS,
                        autoLockAppChangeDelay = AutoLockBackgroundDelay.TEN_SECONDS,
                        isScreenshotAllowed = false,
                        shakeToLockEnabled = false,
                        verifyPasswordInterval = VerifyPasswordInterval.EVERY_MONTH,
                        isAutoDestructionEnabled = false,
                    ),
                    openClipboardClearDelayBottomSheet = {},
                    context = context,
                    openAutoLockInactivityDelayBottomSheet = {},
                    openAutoLockAppChangeDelayBottomSheet = {},
                    toggleAllowScreenshot = {},
                    toggleShakeToLock = toggleShakeToLock,
                    openVerifyPasswordIntervalBottomSheet = {},
                    onSetCameraSystemClick = {},
                    selectedCameraSystem = CameraSystem.InApp,
                    shakeIsAvailable = false,
                    navigateToAutoDestructionSetting = {},
                )
            }

            onNodeWithText(getString(OSString.settings_security_section_shake_title)).assertIsNotDisplayed()
        }
    }

    @Test
    fun shake_is_available() {
        // Order is important. Mock context with hasBiometric method.
        mockkStatic(Context::hasBiometric)
        val context: Context = mockk<Context>().apply {
            every { hasBiometric() } returns false
        }
        invoke {
            setContent {
                SecuritySettingScreen(
                    navigateBack = navigateBack,
                    toggleBiometric = toggleBiometry,
                    settingData = SecuritySettingData(
                        isBiometricEnabled = true,
                        copyBoardClearDelay = ClipboardClearDelay.THIRTY_SECONDS,
                        autoLockInactivityDelay = AutoLockInactivityDelay.THIRTY_SECONDS,
                        autoLockAppChangeDelay = AutoLockBackgroundDelay.TEN_SECONDS,
                        isScreenshotAllowed = false,
                        shakeToLockEnabled = false,
                        verifyPasswordInterval = VerifyPasswordInterval.EVERY_MONTH,
                        isAutoDestructionEnabled = false,
                    ),
                    openClipboardClearDelayBottomSheet = {},
                    context = context,
                    openAutoLockInactivityDelayBottomSheet = {},
                    openAutoLockAppChangeDelayBottomSheet = {},
                    toggleAllowScreenshot = {},
                    openVerifyPasswordIntervalBottomSheet = {},
                    onSetCameraSystemClick = {},
                    selectedCameraSystem = CameraSystem.InApp,
                    toggleShakeToLock = toggleShakeToLock,
                    shakeIsAvailable = true,
                    navigateToAutoDestructionSetting = {},
                )
            }

            onNodeWithText(getString(OSString.settings_security_section_shake_title)).assertIsDisplayed()

            onNodeWithText(text = getString(OSString.settings_security_section_shake_lock))
                .assertIsDisplayed()
                .performClick()

            verify(exactly = 1) { toggleShakeToLock() }
        }
    }

    @Test
    fun auto_destruction_enabled_test() {
        // Order is important. Mock context with hasBiometric method.
        mockkStatic(Context::hasBiometric)
        val context: Context = mockk<Context>().apply {
            every { hasBiometric() } returns false
        }
        invoke {
            setContent {
                SecuritySettingScreen(
                    navigateBack = navigateBack,
                    toggleBiometric = toggleBiometry,
                    settingData = SecuritySettingData(
                        isBiometricEnabled = true,
                        copyBoardClearDelay = ClipboardClearDelay.THIRTY_SECONDS,
                        autoLockInactivityDelay = AutoLockInactivityDelay.THIRTY_SECONDS,
                        autoLockAppChangeDelay = AutoLockBackgroundDelay.TEN_SECONDS,
                        isScreenshotAllowed = false,
                        shakeToLockEnabled = false,
                        verifyPasswordInterval = VerifyPasswordInterval.EVERY_MONTH,
                        isAutoDestructionEnabled = true,
                    ),
                    openClipboardClearDelayBottomSheet = {},
                    context = context,
                    openAutoLockInactivityDelayBottomSheet = {},
                    openAutoLockAppChangeDelayBottomSheet = {},
                    toggleAllowScreenshot = {},
                    openVerifyPasswordIntervalBottomSheet = {},
                    onSetCameraSystemClick = {},
                    selectedCameraSystem = CameraSystem.InApp,
                    toggleShakeToLock = toggleShakeToLock,
                    shakeIsAvailable = true,
                    navigateToAutoDestructionSetting = {},
                )
            }
            hasText(getString(OSString.settings_security_section_autodestruction_enabled))
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .assertIsDisplayed()
        }

        invoke {
            setContent {
                SecuritySettingScreen(
                    navigateBack = navigateBack,
                    toggleBiometric = toggleBiometry,
                    settingData = SecuritySettingData(
                        isBiometricEnabled = true,
                        copyBoardClearDelay = ClipboardClearDelay.THIRTY_SECONDS,
                        autoLockInactivityDelay = AutoLockInactivityDelay.THIRTY_SECONDS,
                        autoLockAppChangeDelay = AutoLockBackgroundDelay.TEN_SECONDS,
                        isScreenshotAllowed = false,
                        shakeToLockEnabled = false,
                        verifyPasswordInterval = VerifyPasswordInterval.EVERY_MONTH,
                        isAutoDestructionEnabled = false,
                    ),
                    openClipboardClearDelayBottomSheet = {},
                    context = context,
                    openAutoLockInactivityDelayBottomSheet = {},
                    openAutoLockAppChangeDelayBottomSheet = {},
                    toggleAllowScreenshot = {},
                    openVerifyPasswordIntervalBottomSheet = {},
                    onSetCameraSystemClick = {},
                    selectedCameraSystem = CameraSystem.InApp,
                    toggleShakeToLock = toggleShakeToLock,
                    shakeIsAvailable = true,
                    navigateToAutoDestructionSetting = {},
                )
            }

            hasText(getString(OSString.settings_security_section_autodestruction))
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .assertIsDisplayed()
        }
    }
}
