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
 * Created by Lunabee Studio / Date - 6/6/2024 - for the oneSafe6 SDK.
 * Last modified 6/6/24, 3:45 PM
 */

package studio.lunabee.onesafe.storage.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.importexport.model.SafeAutoBackupEnabled
import studio.lunabee.onesafe.storage.model.RoomCtaState
import studio.lunabee.onesafe.storage.model.RoomCtaState.State
import java.time.Instant
import kotlin.time.Duration

@Dao
interface SettingsDao {

    @Query("SELECT setting_auto_lock_osk_hidden_delay FROM Safe WHERE id IS :safeId")
    fun getAutoLockOSKHiddenDelayFlow(safeId: SafeId): Flow<Duration?>

    @Query("SELECT setting_verify_password_interval FROM Safe WHERE id IS :safeId")
    fun getVerifyPasswordIntervalFlow(safeId: SafeId): Flow<VerifyPasswordInterval?>

    @Query("SELECT setting_material_you FROM Safe WHERE id IS :safeId")
    fun getMaterialYou(safeId: SafeId): Flow<Boolean?>

    @Query("SELECT setting_automation FROM Safe WHERE id IS :safeId")
    fun getAutomationFlow(safeId: SafeId): Flow<Boolean?>

    @Query("SELECT setting_display_share_warning FROM Safe WHERE id IS :safeId")
    fun getDisplayShareWarningFlow(safeId: SafeId): Flow<Boolean?>

    @Query("SELECT setting_allow_screenshot FROM Safe WHERE id IS :safeId")
    fun getAllowScreenshotFlow(safeId: SafeId): Flow<Boolean?>

    @Query("SELECT setting_shake_to_lock FROM Safe WHERE id IS :safeId")
    fun getShakeToLockFlow(safeId: SafeId): Flow<Boolean>

    @Query("SELECT setting_bubbles_preview FROM Safe WHERE id IS :safeId")
    fun getBubblesPreview(safeId: SafeId): Flow<Boolean?>

    @Query(
        """
        SELECT
            setting_bubbles_home_card_cta_state as state,
            setting_bubbles_home_card_cta_timestamp as timestamp
        FROM Safe WHERE id IS :safeId
        """,
    )
    fun getBubblesHomeCardCtaState(safeId: SafeId): Flow<RoomCtaState?>

    @Query("SELECT setting_camera_system FROM Safe WHERE id IS :safeId")
    fun getCameraSystemFlow(safeId: SafeId): Flow<CameraSystem?>

    @Query("SELECT setting_auto_lock_inactivity_delay FROM Safe WHERE id IS :safeId")
    fun getAutoLockInactivityDelayFlow(safeId: SafeId): Flow<Duration?>

    @Query("SELECT setting_auto_lock_app_change_delay FROM Safe WHERE id IS :safeId")
    fun getAutoLockAppChangeDelayFlow(safeId: SafeId): Flow<Duration?>

    @Query("SELECT setting_clipboard_delay FROM Safe WHERE id IS :safeId")
    fun getClipboardDelayFlow(safeId: SafeId): Flow<Duration?>

    @Query("SELECT setting_bubbles_resend_message_delay FROM Safe WHERE id IS :safeId")
    suspend fun getBubblesResendMessageDelay(safeId: SafeId): Duration

    @Query("SELECT setting_bubbles_resend_message_delay FROM Safe WHERE id IS :safeId")
    fun getBubblesResendMessageDelayFlow(safeId: SafeId): Flow<Duration?>

    @Query("SELECT setting_auto_lock_osk_inactivity_delay FROM Safe WHERE id IS :safeId")
    fun getAutoLockOSKInactivityDelayFlow(safeId: SafeId): Flow<Duration?>

    @Query("SELECT setting_auto_backup_enabled FROM Safe WHERE id IS :safeId")
    fun getAutoBackupEnabledFlow(safeId: SafeId): Flow<Boolean?>

    @Query("SELECT setting_auto_backup_frequency FROM Safe WHERE id IS :safeId")
    fun getAutoBackupFrequencyFlow(safeId: SafeId): Flow<Duration?>

    @Query("SELECT setting_auto_backup_max_number FROM Safe WHERE id IS :safeId")
    fun getAutoBackupMaxNumberFlow(safeId: SafeId): Flow<Int?>

    @Query("SELECT setting_cloud_backup_enabled FROM Safe WHERE id IS :safeId")
    fun getCloudBackupEnabled(safeId: SafeId): Flow<Boolean?>

    @Query("SELECT setting_keep_local_backup_enabled FROM Safe WHERE id IS :safeId")
    fun getKeepLocalBackupEnabled(safeId: SafeId): Flow<Boolean?>

    @Query("SELECT setting_item_ordering FROM Safe WHERE id IS :safeId")
    fun getItemOrdering(safeId: SafeId): Flow<ItemOrder?>

    @Query("SELECT setting_items_layout_setting FROM Safe WHERE id IS :safeId")
    fun getItemLayout(safeId: SafeId): Flow<ItemLayout?>

    @Query("SELECT app_visit_has_finish_one_safe_k_on_boarding FROM Safe WHERE id IS :safeId")
    fun getHasFinishOneSafeKOnBoardingFlow(safeId: SafeId): Flow<Boolean?>

    @Query("SELECT app_visit_has_done_on_boarding_bubbles FROM Safe WHERE id IS :safeId")
    fun getHasDoneOnBoardingBubblesFlow(safeId: SafeId): Flow<Boolean?>

    @Query("SELECT app_visit_has_hidden_camera_tips FROM Safe WHERE id IS :safeId")
    fun getHasHiddenCameraTipsFlow(safeId: SafeId): Flow<Boolean?>

    @Query("SELECT app_visit_has_seen_item_edition_url_tool_tip FROM Safe WHERE id IS :safeId")
    fun getHasSeenItemEditionUrlToolTipFlow(safeId: SafeId): Flow<Boolean?>

    @Query("SELECT app_visit_has_seen_item_edition_url_tool_tip FROM Safe WHERE id IS :safeId")
    suspend fun getHasSeenItemEditionUrlToolTip(safeId: SafeId): Boolean

    @Query("SELECT app_visit_has_seen_item_edition_emoji_tool_tip FROM Safe WHERE id IS :safeId")
    fun getHasSeenItemEditionEmojiToolTipFlow(safeId: SafeId): Flow<Boolean?>

    @Query("SELECT app_visit_has_seen_item_edition_emoji_tool_tip FROM Safe WHERE id IS :safeId")
    suspend fun getHasSeenItemEditionEmojiToolTip(safeId: SafeId): Boolean

    @Query("SELECT app_visit_has_seen_item_read_edit_tool_tip FROM Safe WHERE id IS :safeId")
    fun getHasSeenItemReadEditToolTipFlow(safeId: SafeId): Flow<Boolean?>

    @Query(
        """
        SELECT
            setting_enable_auto_backup_cta_state as state,
            setting_enable_auto_backup_cta_timestamp as timestamp
        FROM Safe WHERE id IS :safeId
        """,
    )
    fun getEnableAutoBackupCtaState(safeId: SafeId): Flow<RoomCtaState?>

    @Query("SELECT setting_camera_system FROM Safe WHERE id IS :safeId")
    suspend fun getCameraSystem(safeId: SafeId): CameraSystem

    @Query("SELECT setting_allow_screenshot FROM Safe WHERE id IS :safeId")
    suspend fun getAllowScreenshot(safeId: SafeId): Boolean

    @Query("SELECT setting_shake_to_lock FROM Safe WHERE id IS :safeId")
    suspend fun getShakeToLock(safeId: SafeId): Boolean

    @Query("SELECT setting_auto_lock_inactivity_delay FROM Safe WHERE id = :safeId")
    suspend fun getAutoLockInactivityDelay(safeId: SafeId): Duration

    @Query("SELECT setting_auto_lock_app_change_delay FROM Safe WHERE id = :safeId")
    suspend fun autoLockAppChangeDelay(safeId: SafeId): Duration

    @Query("SELECT setting_clipboard_delay FROM Safe WHERE id = :safeId")
    suspend fun clipboardDelay(safeId: SafeId): Duration

    @Query("SELECT setting_verify_password_interval FROM Safe WHERE id = :safeId")
    suspend fun verifyPasswordInterval(safeId: SafeId): VerifyPasswordInterval

    @Query("SELECT setting_last_password_verification FROM Safe WHERE id = :safeId")
    suspend fun getLastPasswordVerification(safeId: SafeId): Instant

    @Query("SELECT setting_auto_lock_osk_inactivity_delay FROM Safe WHERE id = :safeId")
    suspend fun autoLockOSKInactivityDelay(safeId: SafeId): Duration

    @Query("SELECT setting_auto_lock_osk_hidden_delay FROM Safe WHERE id = :safeId")
    suspend fun autoLockOSKHiddenDelay(safeId: SafeId): Duration

    @Query("SELECT setting_auto_backup_enabled FROM Safe WHERE id IS :safeId")
    suspend fun autoBackupEnabled(safeId: SafeId): Boolean

    @Query("SELECT setting_auto_backup_frequency FROM Safe WHERE id = :safeId")
    suspend fun autoBackupFrequency(safeId: SafeId): Duration

    @Query("SELECT setting_auto_backup_max_number FROM Safe WHERE id = :safeId")
    suspend fun autoBackupMaxNumber(safeId: SafeId): Int

    @Query("UPDATE Safe SET setting_material_you = NOT setting_material_you WHERE id = :safeId")
    suspend fun toggleMaterialYou(safeId: SafeId)

    @Query("UPDATE Safe SET setting_automation = NOT setting_automation WHERE id = :safeId")
    suspend fun toggleAutomation(safeId: SafeId)

    @Query("UPDATE Safe SET setting_display_share_warning = 0 WHERE id IS :safeId")
    suspend fun disableShareWarningDisplay(safeId: SafeId)

    @Query("UPDATE Safe SET setting_allow_screenshot = NOT setting_allow_screenshot WHERE id = :safeId")
    suspend fun toggleAllowScreenshot(safeId: SafeId)

    @Query("UPDATE Safe SET setting_shake_to_lock = NOT setting_shake_to_lock WHERE id = :safeId")
    suspend fun toggleShakeToLock(safeId: SafeId)

    @Query("UPDATE Safe SET setting_bubbles_preview = :value WHERE id IS :safeId")
    suspend fun setBubblesPreview(safeId: SafeId, value: Boolean)

    @Query("UPDATE Safe SET setting_camera_system = :value WHERE id IS :safeId")
    suspend fun setCameraSystem(safeId: SafeId, value: CameraSystem)

    @Query(
        """
            UPDATE Safe SET 
            setting_bubbles_home_card_cta_state = :state, 
            setting_bubbles_home_card_cta_timestamp = :timestamp 
            WHERE id IS :safeId
            """,
    )
    suspend fun setBubblesHomeCardCtaState(safeId: SafeId, state: State, timestamp: Instant?)

    @Query("UPDATE Safe SET setting_auto_lock_inactivity_delay = :duration WHERE id IS :safeId")
    suspend fun setAutoLockInactivityDelay(safeId: SafeId, duration: Duration)

    @Query("UPDATE Safe SET setting_auto_lock_app_change_delay = :duration WHERE id IS :safeId")
    suspend fun setAutoLockAppChangeDelay(safeId: SafeId, duration: Duration)

    @Query("UPDATE Safe SET setting_bubbles_resend_message_delay = :delay WHERE id IS :safeId")
    suspend fun setBubblesResendMessageDelay(safeId: SafeId, delay: Duration)

    @Query("UPDATE Safe SET setting_auto_lock_osk_inactivity_delay = :duration WHERE id IS :safeId")
    suspend fun setAutoLockOSKInactivityDelay(safeId: SafeId, duration: Duration)

    @Query("UPDATE Safe SET setting_auto_lock_osk_hidden_delay = :duration WHERE id IS :safeId")
    suspend fun setAutoLockOSKHiddenDelay(safeId: SafeId, duration: Duration)

    @Query("UPDATE Safe SET setting_auto_backup_enabled = NOT setting_auto_backup_enabled WHERE id IS :safeId")
    suspend fun toggleAutoBackupSettings(safeId: SafeId)

    @Query("UPDATE Safe SET setting_auto_backup_frequency = :duration WHERE id IS :safeId")
    suspend fun setAutoBackupFrequency(safeId: SafeId, duration: Duration)

    @Query("UPDATE Safe SET setting_auto_backup_max_number = :maxNumber WHERE id IS :safeId")
    suspend fun updateAutoBackupMaxNumber(safeId: SafeId, maxNumber: Int)

    @Query("UPDATE Safe SET setting_cloud_backup_enabled = :enabled WHERE id IS :safeId")
    suspend fun setCloudBackupEnabled(safeId: SafeId, enabled: Boolean)

    @Query("UPDATE Safe SET setting_keep_local_backup_enabled = :enabled WHERE id IS :safeId")
    suspend fun setKeepLocalBackupSettings(safeId: SafeId, enabled: Boolean)

    @Query("UPDATE Safe SET setting_item_ordering = :order WHERE id IS :safeId")
    suspend fun setItemOrdering(safeId: SafeId, order: ItemOrder)

    @Query("UPDATE Safe SET setting_items_layout_setting = :style WHERE id IS :safeId")
    suspend fun setItemLayout(safeId: SafeId, style: ItemLayout)

    @Query(
        """
            UPDATE Safe SET 
            setting_enable_auto_backup_cta_state = :state, 
            setting_enable_auto_backup_cta_timestamp = :timestamp 
            WHERE id IS :safeId
            """,
    )
    suspend fun setEnableAutoBackupCtaState(safeId: SafeId, state: State, timestamp: Instant?)

    @Query("UPDATE Safe SET setting_clipboard_delay = :delay WHERE id IS :safeId")
    suspend fun setClipboardClearDelay(safeId: SafeId, delay: Duration)

    @Query("UPDATE Safe SET setting_verify_password_interval = :passwordInterval WHERE id IS :safeId")
    suspend fun setPasswordInterval(safeId: SafeId, passwordInterval: VerifyPasswordInterval)

    @Query("UPDATE Safe SET setting_last_password_verification = :instant WHERE id IS :safeId")
    suspend fun setLastPasswordVerification(safeId: SafeId, instant: Instant)

    @Query("SELECT setting_drive_selected_account FROM Safe WHERE id IS :safeId")
    fun selectedDriveAccountFlow(safeId: SafeId): Flow<String??>

    @Query("SELECT setting_drive_selected_account FROM Safe WHERE id IS :safeId")
    suspend fun selectedDriveAccount(safeId: SafeId): String?

    @Query("SELECT setting_drive_folder_id FROM Safe WHERE id IS :safeId")
    suspend fun folderId(safeId: SafeId): String?

    @Query("SELECT setting_drive_folder_id FROM Safe WHERE id IS :safeId")
    fun driveFolderId(safeId: SafeId): Flow<String??>

    @Query("SELECT setting_drive_folder_url FROM Safe WHERE id IS :safeId")
    fun driveFolderUrlFlow(safeId: SafeId): Flow<String??>

    @Query("SELECT setting_drive_folder_url FROM Safe WHERE id IS :safeId")
    suspend fun driveFolderUrl(safeId: SafeId): String?

    @Query("UPDATE Safe SET setting_drive_selected_account = :account WHERE id IS :safeId")
    suspend fun setSelectedDriveAccount(safeId: SafeId, account: String?)

    @Query("UPDATE Safe SET setting_drive_folder_id = :folderId WHERE id IS :safeId")
    suspend fun setDriveFolderId(safeId: SafeId, folderId: String?)

    @Query("UPDATE Safe SET setting_drive_folder_url = :url WHERE id IS :safeId")
    suspend fun setDriveFolderUrl(safeId: SafeId, url: String?)

    @Query("SELECT id as safeId, setting_cloud_backup_enabled as cloudAutoBackupEnabled FROM Safe WHERE setting_auto_backup_enabled IS 1")
    suspend fun getSafeAutoBackupEnabled(): List<SafeAutoBackupEnabled>

    @Query("SELECT setting_automation FROM Safe WHERE id IS :safeId")
    suspend fun getAutomation(safeId: SafeId): Boolean

    @Query("SELECT setting_display_share_warning FROM Safe WHERE id IS :safeId")
    suspend fun getDisplayShareWarning(safeId: SafeId): Boolean

    @Query(
        """
        SELECT
            setting_independent_safe_info_cta_state as state,
            setting_independent_safe_info_cta_timestamp as timestamp
        FROM Safe WHERE id IS :safeId
        """,
    )
    fun getIndependentSafeInfoCtaState(safeId: SafeId): Flow<RoomCtaState?>

    @Query(
        """
            UPDATE Safe SET 
            setting_independent_safe_info_cta_state = :state, 
            setting_independent_safe_info_cta_timestamp = :timestamp 
            WHERE id IS :safeId
            """,
    )
    suspend fun setIndependentSafeInfoCtaState(safeId: SafeId, state: State, timestamp: Instant?)

    @Query("UPDATE Safe SET app_visit_has_finish_one_safe_k_on_boarding = :value WHERE id IS :safeId")
    suspend fun setHasFinishOneSafeKOnBoarding(safeId: SafeId, value: Boolean)

    @Query("UPDATE Safe SET app_visit_has_done_on_boarding_bubbles = :value WHERE id IS :safeId")
    suspend fun setHasDoneOnBoardingBubbles(safeId: SafeId, value: Boolean)

    @Query("UPDATE Safe SET app_visit_has_hidden_camera_tips = :value WHERE id IS :safeId")
    suspend fun setHasHiddenCameraTips(safeId: SafeId, value: Boolean)

    @Query("UPDATE Safe SET app_visit_has_seen_item_edition_url_tool_tip = :value WHERE id IS :safeId")
    suspend fun setHasSeenItemEditionUrlToolTip(safeId: SafeId, value: Boolean)

    @Query("UPDATE Safe SET app_visit_has_seen_item_edition_emoji_tool_tip = :value WHERE id IS :safeId")
    suspend fun setHasSeenItemEditionEmojiToolTip(safeId: SafeId, value: Boolean)

    @Query("UPDATE Safe SET app_visit_has_seen_item_read_edit_tool_tip = :value WHERE id IS :safeId")
    suspend fun setHasSeenItemReadEditToolTip(safeId: SafeId, value: Boolean)
}
