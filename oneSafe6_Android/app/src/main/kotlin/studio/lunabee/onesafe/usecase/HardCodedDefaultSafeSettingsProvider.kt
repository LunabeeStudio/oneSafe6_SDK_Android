package studio.lunabee.onesafe.usecase

import studio.lunabee.onesafe.SettingsDefaults
import studio.lunabee.onesafe.domain.model.safe.SafeSettings
import studio.lunabee.onesafe.domain.usecase.settings.DefaultSafeSettingsProvider
import studio.lunabee.onesafe.migration.MigrationConstant
import java.time.Clock
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class HardCodedDefaultSafeSettingsProvider @Inject constructor(
    private val clock: Clock,
) : DefaultSafeSettingsProvider {
    override fun invoke(): SafeSettings {
        return SafeSettings(
            version = MigrationConstant.LastVersion,
            materialYou = SettingsDefaults.MaterialYouSettingDefault,
            automation = SettingsDefaults.AutomationSettingDefault,
            displayShareWarning = SettingsDefaults.DisplayShareWarningDefault,
            allowScreenshot = SettingsDefaults.AllowScreenshotSettingDefault,
            shakeToLock = SettingsDefaults.ShakeToLockSettingDefault,
            bubblesPreview = SettingsDefaults.BubblesPreviewDefault,
            cameraSystem = SettingsDefaults.CameraSystemDefault,
            autoLockOSKHiddenDelay = SettingsDefaults.AutoLockInactivityDelayMsDefault.milliseconds,
            verifyPasswordInterval = SettingsDefaults.VerifyPasswordIntervalDefault,
            bubblesHomeCardCtaState = SettingsDefaults.BubblesPreviewCardDefault,
            autoLockInactivityDelay = SettingsDefaults.AutoLockInactivityDelayMsDefault.milliseconds,
            autoLockAppChangeDelay = SettingsDefaults.AutoLockAppChangeDelayMsDefault.milliseconds,
            clipboardDelay = SettingsDefaults.ClipboardClearDelayMsDefault.milliseconds,
            bubblesResendMessageDelay = SettingsDefaults.BubblesResendMessageDelayMsDefault.milliseconds,
            autoLockOSKInactivityDelay = SettingsDefaults.AutoLockInactivityDelayMsDefault.milliseconds,
            autoBackupEnabled = SettingsDefaults.AutoBackupEnabledDefault,
            autoBackupFrequency = SettingsDefaults.AutoBackupFrequencyMsDefault.milliseconds,
            autoBackupMaxNumber = SettingsDefaults.AutoBackupMaxNumberDefault,
            cloudBackupEnabled = SettingsDefaults.CloudBackupEnabledDefault,
            keepLocalBackupEnabled = SettingsDefaults.KeepLocalBackupEnabledDefault,
            itemOrdering = SettingsDefaults.ItemOrderingDefault,
            itemLayout = SettingsDefaults.ItemLayoutDefault,
            enableAutoBackupCtaState = SettingsDefaults.EnableAutoBackupCtaState,
            lastPasswordVerification = SettingsDefaults.lastPasswordVerificationDefault(clock),
            independentSafeInfoCtaState = SettingsDefaults.independentSafeInfoCtaState(clock),
            preventionWarningCtaState = SettingsDefaults.preventionWarningCtaState(clock),
            lastExportDate = null,
        )
    }
}
