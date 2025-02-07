package studio.lunabee.onesafe.feature.settings.security

import androidx.compose.runtime.Stable
import studio.lunabee.onesafe.commonui.settings.AutoLockBackgroundDelay
import studio.lunabee.onesafe.commonui.settings.AutoLockInactivityDelay
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.feature.clipboard.model.ClipboardClearDelay

@Stable
class SecuritySettingData(
    val isBiometricEnabled: Boolean,
    val copyBoardClearDelay: ClipboardClearDelay,
    val autoLockInactivityDelay: AutoLockInactivityDelay,
    val autoLockAppChangeDelay: AutoLockBackgroundDelay,
    val isScreenshotAllowed: Boolean,
    val shakeToLockEnabled: Boolean,
    val verifyPasswordInterval: VerifyPasswordInterval,
    val isAutoDestructionEnabled: Boolean,
) {
    companion object {
        fun init(): SecuritySettingData = SecuritySettingData(
            isBiometricEnabled = false,
            copyBoardClearDelay = ClipboardClearDelay.THIRTY_SECONDS,
            autoLockInactivityDelay = AutoLockInactivityDelay.THIRTY_SECONDS,
            autoLockAppChangeDelay = AutoLockBackgroundDelay.TEN_SECONDS,
            isScreenshotAllowed = false,
            shakeToLockEnabled = false,
            verifyPasswordInterval = VerifyPasswordInterval.EVERY_TWO_MONTHS,
            isAutoDestructionEnabled = false,
        )
    }
}
