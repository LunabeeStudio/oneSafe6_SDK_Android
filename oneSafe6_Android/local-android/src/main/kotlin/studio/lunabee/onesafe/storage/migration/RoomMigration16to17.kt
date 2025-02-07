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
 * Created by Lunabee Studio / Date - 9/9/2024 - for the oneSafe6 SDK.
 * Last modified 9/9/24, 6:23 PM
 */

package studio.lunabee.onesafe.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import javax.inject.Inject

class RoomMigration16to17 @Inject constructor() : Migration(16, 17) {

    /**
     * Fix Safe table might have shift its columns during migration 15 to 16
     */
    override fun migrate(db: SupportSQLiteDatabase) {
        try {
            val cursor = db.query("SELECT setting_camera_system FROM Safe LIMIT 1")
            if (cursor.moveToFirst()) {
                val cameraSystemRaw = cursor.getString(0)
                CameraSystem.valueOf(cameraSystemRaw)
            } else {
                return // no safe, no-op
            }
        } catch (e: IllegalArgumentException) {
            fixColumns(db)
        }
    }

    private fun fixColumns(db: SupportSQLiteDatabase) {
        val query = """
            UPDATE Safe
            SET (
            `setting_bubbles_preview`,
            `setting_camera_system`,
            `setting_auto_lock_osk_hidden_delay`,
            `setting_verify_password_interval`,
            `setting_last_password_verification`,
            `setting_auto_lock_inactivity_delay`,
            `setting_auto_lock_app_change_delay`,
            `setting_clipboard_delay`,
            `setting_bubbles_resend_message_delay`,
            `setting_auto_lock_osk_inactivity_delay`,
            `setting_auto_backup_enabled`,
            `setting_auto_backup_frequency`,
            `setting_auto_backup_max_number`,
            `setting_cloud_backup_enabled`,
            `setting_keep_local_backup_enabled`,
            `setting_item_ordering`,
            `setting_items_layout_setting`,
            `setting_bubbles_home_card_cta_state`,
            `setting_bubbles_home_card_cta_timestamp`,
            `setting_drive_selected_account`,
            `setting_drive_folder_id`,
            `setting_drive_folder_url`,
            `setting_enable_auto_backup_cta_state`,
            `setting_enable_auto_backup_cta_timestamp`,
            `setting_independent_safe_info_cta_state`,
            `setting_independent_safe_info_cta_timestamp`,
            `app_visit_has_finish_one_safe_k_on_boarding`,
            `app_visit_has_done_on_boarding_bubbles`,
            `app_visit_has_hidden_camera_tips`,
            `app_visit_has_seen_item_edition_url_tool_tip`,
            `app_visit_has_seen_item_edition_emoji_tool_tip`,
            `app_visit_has_seen_item_read_edit_tool_tip`,
            `setting_shake_to_lock`,
            `open_order`
            ) = ( SELECT 
                `setting_shake_to_lock`,
                `setting_bubbles_preview`,
                `setting_camera_system`,
                `setting_auto_lock_osk_hidden_delay`,
                `setting_verify_password_interval`,
                `setting_last_password_verification`,
                `setting_auto_lock_inactivity_delay`,
                `setting_auto_lock_app_change_delay`,
                `setting_clipboard_delay`,
                `setting_bubbles_resend_message_delay`,
                `setting_auto_lock_osk_inactivity_delay`,
                `setting_auto_backup_enabled`,
                `setting_auto_backup_frequency`,
                `setting_auto_backup_max_number`,
                `setting_cloud_backup_enabled`,
                `setting_keep_local_backup_enabled`,
                `setting_item_ordering`,
                `setting_items_layout_setting`,
                `setting_bubbles_home_card_cta_state`,
                `setting_bubbles_home_card_cta_timestamp`,
                `setting_drive_selected_account`,
                `setting_drive_folder_id`,
                `setting_drive_folder_url`,
                `setting_enable_auto_backup_cta_state`,
                `setting_enable_auto_backup_cta_timestamp`,
                `setting_independent_safe_info_cta_state`,
                `setting_independent_safe_info_cta_timestamp`,
                `app_visit_has_finish_one_safe_k_on_boarding`,
                `app_visit_has_done_on_boarding_bubbles`,
                `app_visit_has_hidden_camera_tips`,
                `app_visit_has_seen_item_edition_url_tool_tip`,
                `app_visit_has_seen_item_edition_emoji_tool_tip`,
                `app_visit_has_seen_item_read_edit_tool_tip`,
                `open_order`
                FROM Safe as BadSafe
                WHERE Safe.id = BadSafe.id )
        """.trimIndent()
        db.execSQL(query)
    }
}
