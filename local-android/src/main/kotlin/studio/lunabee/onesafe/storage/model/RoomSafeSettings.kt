/*
 * Copyright (c) 2024 Lunabee Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Lunabee Studio / Date - 6/19/2024 - for the oneSafe6 SDK.
 * Last modified 6/19/24, 10:54 AM
 */

package studio.lunabee.onesafe.storage.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safe.SafeSettings
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.importexport.model.GoogleDriveSettings
import java.time.Instant
import kotlin.time.Duration

data class RoomSafeSettings(
    @ColumnInfo(name = "material_you")
    val materialYou: Boolean,
    @ColumnInfo(name = "automation")
    val automation: Boolean,
    @ColumnInfo(name = "display_share_warning")
    val displayShareWarning: Boolean,
    @ColumnInfo(name = "allow_screenshot")
    val allowScreenshot: Boolean,
    @ColumnInfo(name = "shake_to_lock")
    val shakeToLock: Boolean,
    @ColumnInfo(name = "bubbles_preview")
    val bubblesPreview: Boolean,
    @ColumnInfo(name = "camera_system")
    val cameraSystem: CameraSystem,
    @ColumnInfo(name = "auto_lock_osk_hidden_delay")
    val autoLockOSKHiddenDelay: Duration,
    @ColumnInfo(name = "verify_password_interval")
    val verifyPasswordInterval: VerifyPasswordInterval,
    @ColumnInfo(name = "last_password_verification")
    val lastPasswordVerification: Instant,
    @Embedded(prefix = "bubbles_home_card_cta_")
    val bubblesHomeCardCtaState: RoomCtaState,
    @ColumnInfo(name = "auto_lock_inactivity_delay")
    val autoLockInactivityDelay: Duration,
    @ColumnInfo(name = "auto_lock_app_change_delay")
    val autoLockAppChangeDelay: Duration,
    @ColumnInfo(name = "clipboard_delay")
    val clipboardDelay: Duration,
    @ColumnInfo(name = "bubbles_resend_message_delay")
    val bubblesResendMessageDelay: Duration,
    @ColumnInfo(name = "auto_lock_osk_inactivity_delay")
    val autoLockOSKInactivityDelay: Duration,
    @ColumnInfo(name = "auto_backup_enabled")
    val autoBackupEnabled: Boolean,
    @ColumnInfo(name = "auto_backup_frequency")
    val autoBackupFrequency: Duration,
    @ColumnInfo(name = "auto_backup_max_number")
    val autoBackupMaxNumber: Int,
    @ColumnInfo(name = "cloud_backup_enabled")
    val cloudBackupEnabled: Boolean,
    @ColumnInfo(name = "keep_local_backup_enabled")
    val keepLocalBackupEnabled: Boolean,
    @ColumnInfo(name = "item_ordering")
    val itemOrdering: ItemOrder,
    @ColumnInfo(name = "items_layout_setting")
    val itemLayout: ItemLayout,
    @Embedded(prefix = "drive_")
    val driveSettings: RoomDriveSettings,
    @Embedded(prefix = "enable_auto_backup_cta_")
    val enableAutoBackupCtaState: RoomCtaState,
    @Embedded(prefix = "independent_safe_info_cta_")
    val independentSafeInfoCtaState: RoomCtaState,
) {

    companion object {
        fun fromSafeSettings(safeSettings: SafeSettings, driveSettings: GoogleDriveSettings): RoomSafeSettings = RoomSafeSettings(
            materialYou = safeSettings.materialYou,
            automation = safeSettings.automation,
            displayShareWarning = safeSettings.displayShareWarning,
            allowScreenshot = safeSettings.allowScreenshot,
            shakeToLock = safeSettings.shakeToLock,
            bubblesPreview = safeSettings.bubblesPreview,
            cameraSystem = safeSettings.cameraSystem,
            autoLockOSKHiddenDelay = safeSettings.autoLockOSKHiddenDelay,
            verifyPasswordInterval = safeSettings.verifyPasswordInterval,
            bubblesHomeCardCtaState = RoomCtaState.fromCtaState(safeSettings.bubblesHomeCardCtaState),
            autoLockInactivityDelay = safeSettings.autoLockInactivityDelay,
            autoLockAppChangeDelay = safeSettings.autoLockAppChangeDelay,
            clipboardDelay = safeSettings.clipboardDelay,
            bubblesResendMessageDelay = safeSettings.bubblesResendMessageDelay,
            autoLockOSKInactivityDelay = safeSettings.autoLockOSKInactivityDelay,
            autoBackupEnabled = safeSettings.autoBackupEnabled,
            autoBackupFrequency = safeSettings.autoBackupFrequency,
            autoBackupMaxNumber = safeSettings.autoBackupMaxNumber,
            cloudBackupEnabled = safeSettings.cloudBackupEnabled,
            keepLocalBackupEnabled = safeSettings.keepLocalBackupEnabled,
            itemOrdering = safeSettings.itemOrdering,
            itemLayout = safeSettings.itemLayout,
            enableAutoBackupCtaState = RoomCtaState.fromCtaState(safeSettings.enableAutoBackupCtaState),
            driveSettings = RoomDriveSettings.fromDriveSettings(driveSettings),
            lastPasswordVerification = safeSettings.lastPasswordVerification,
            independentSafeInfoCtaState = RoomCtaState.fromCtaState(safeSettings.independentSafeInfoCtaState),
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomSafeSettings

        if (materialYou != other.materialYou) return false
        if (automation != other.automation) return false
        if (displayShareWarning != other.displayShareWarning) return false
        if (allowScreenshot != other.allowScreenshot) return false
        if (shakeToLock != other.shakeToLock) return false
        if (bubblesPreview != other.bubblesPreview) return false
        if (cameraSystem != other.cameraSystem) return false
        if (autoLockOSKHiddenDelay != other.autoLockOSKHiddenDelay) return false
        if (verifyPasswordInterval != other.verifyPasswordInterval) return false
        if (lastPasswordVerification != other.lastPasswordVerification) return false
        if (bubblesHomeCardCtaState != other.bubblesHomeCardCtaState) return false
        if (autoLockInactivityDelay != other.autoLockInactivityDelay) return false
        if (autoLockAppChangeDelay != other.autoLockAppChangeDelay) return false
        if (clipboardDelay != other.clipboardDelay) return false
        if (bubblesResendMessageDelay != other.bubblesResendMessageDelay) return false
        if (autoLockOSKInactivityDelay != other.autoLockOSKInactivityDelay) return false
        if (autoBackupEnabled != other.autoBackupEnabled) return false
        if (autoBackupFrequency != other.autoBackupFrequency) return false
        if (autoBackupMaxNumber != other.autoBackupMaxNumber) return false
        if (cloudBackupEnabled != other.cloudBackupEnabled) return false
        if (keepLocalBackupEnabled != other.keepLocalBackupEnabled) return false
        if (itemOrdering != other.itemOrdering) return false
        if (itemLayout != other.itemLayout) return false
        if (driveSettings != other.driveSettings) return false
        if (enableAutoBackupCtaState != other.enableAutoBackupCtaState) return false
        if (independentSafeInfoCtaState != other.independentSafeInfoCtaState) return false

        return true
    }

    override fun hashCode(): Int {
        var result = materialYou.hashCode()
        result = 31 * result + automation.hashCode()
        result = 31 * result + displayShareWarning.hashCode()
        result = 31 * result + allowScreenshot.hashCode()
        result = 31 * result + shakeToLock.hashCode()
        result = 31 * result + bubblesPreview.hashCode()
        result = 31 * result + cameraSystem.hashCode()
        result = 31 * result + autoLockOSKHiddenDelay.hashCode()
        result = 31 * result + verifyPasswordInterval.hashCode()
        result = 31 * result + lastPasswordVerification.hashCode()
        result = 31 * result + bubblesHomeCardCtaState.hashCode()
        result = 31 * result + autoLockInactivityDelay.hashCode()
        result = 31 * result + autoLockAppChangeDelay.hashCode()
        result = 31 * result + clipboardDelay.hashCode()
        result = 31 * result + bubblesResendMessageDelay.hashCode()
        result = 31 * result + autoLockOSKInactivityDelay.hashCode()
        result = 31 * result + autoBackupEnabled.hashCode()
        result = 31 * result + autoBackupFrequency.hashCode()
        result = 31 * result + autoBackupMaxNumber
        result = 31 * result + cloudBackupEnabled.hashCode()
        result = 31 * result + keepLocalBackupEnabled.hashCode()
        result = 31 * result + itemOrdering.hashCode()
        result = 31 * result + itemLayout.hashCode()
        result = 31 * result + driveSettings.hashCode()
        result = 31 * result + enableAutoBackupCtaState.hashCode()
        result = 31 * result + independentSafeInfoCtaState.hashCode()
        return result
    }
}
