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
 * Created by Lunabee Studio / Date - 6/12/2024 - for the oneSafe6 SDK.
 * Last modified 6/12/24, 9:38 AM
 */

package studio.lunabee.onesafe.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import studio.lunabee.onesafe.storage.model.RoomSafe
import javax.inject.Inject

class RoomMigration15to16 @Inject constructor() : Migration(15, 16) {

    /**
     * Add column [RoomSafe.openOrder] initialized with arbitrary order
     */
    override fun migrate(db: SupportSQLiteDatabase) {
        // Switch off foreign key constraint during migration
        db.execSQL("PRAGMA foreign_keys = OFF")
        // Switch on legacy alter to preserve foreign key constraints during migration
        // https://www.sqlite.org/pragma.html#pragma_legacy_alter_table
        db.execSQL(" PRAGMA legacy_alter_table = ON")

        // Rename Safe table & delete indices to re-create them with the non-null/default open_order column
        db.execSQL("ALTER TABLE Safe RENAME TO Safe_Old")

        db.execSQL(
            "CREATE TABLE `Safe` (`id` BLOB NOT NULL, " +
                "`version` INTEGER NOT NULL, " +
                "`crypto_master_salt` BLOB NOT NULL, " +
                "`crypto_enc_test` BLOB NOT NULL, " +
                "`crypto_enc_index_key` BLOB NOT NULL, " +
                "`crypto_enc_bubbles_key` BLOB, " +
                "`crypto_enc_item_edition_key` BLOB NOT NULL, " +
                "`crypto_biometric_crypto_material` BLOB, " +
                "`setting_material_you` INTEGER NOT NULL, " +
                "`setting_automation` INTEGER NOT NULL, " +
                "`setting_display_share_warning` INTEGER NOT NULL, " +
                "`setting_allow_screenshot` INTEGER NOT NULL, " +
                "`setting_shake_to_lock` INTEGER NOT NULL, " +
                "`setting_bubbles_preview` INTEGER NOT NULL, " +
                "`setting_camera_system` TEXT NOT NULL, " +
                "`setting_auto_lock_osk_hidden_delay` INTEGER NOT NULL, " +
                "`setting_verify_password_interval` TEXT NOT NULL, " +
                "`setting_last_password_verification` INTEGER NOT NULL, " +
                "`setting_auto_lock_inactivity_delay` INTEGER NOT NULL, " +
                "`setting_auto_lock_app_change_delay` INTEGER NOT NULL, " +
                "`setting_clipboard_delay` INTEGER NOT NULL, " +
                "`setting_bubbles_resend_message_delay` INTEGER NOT NULL, " +
                "`setting_auto_lock_osk_inactivity_delay` INTEGER NOT NULL, " +
                "`setting_auto_backup_enabled` INTEGER NOT NULL, " +
                "`setting_auto_backup_frequency` INTEGER NOT NULL, " +
                "`setting_auto_backup_max_number` INTEGER NOT NULL, " +
                "`setting_cloud_backup_enabled` INTEGER NOT NULL, " +
                "`setting_keep_local_backup_enabled` INTEGER NOT NULL, " +
                "`setting_item_ordering` TEXT NOT NULL, " +
                "`setting_items_layout_setting` TEXT NOT NULL, " +
                "`setting_bubbles_home_card_cta_state` TEXT NOT NULL, " +
                "`setting_bubbles_home_card_cta_timestamp` INTEGER, " +
                "`setting_drive_selected_account` TEXT, " +
                "`setting_drive_folder_id` TEXT, " +
                "`setting_drive_folder_url` TEXT, " +
                "`setting_enable_auto_backup_cta_state` TEXT NOT NULL, " +
                "`setting_enable_auto_backup_cta_timestamp` INTEGER, " +
                "`setting_independent_safe_info_cta_state` TEXT NOT NULL, " +
                "`setting_independent_safe_info_cta_timestamp` INTEGER, " +
                "`app_visit_has_finish_one_safe_k_on_boarding` INTEGER NOT NULL, " +
                "`app_visit_has_done_on_boarding_bubbles` INTEGER NOT NULL, " +
                "`app_visit_has_hidden_camera_tips` INTEGER NOT NULL, " +
                "`app_visit_has_seen_item_edition_url_tool_tip` INTEGER NOT NULL, " +
                "`app_visit_has_seen_item_edition_emoji_tool_tip` INTEGER NOT NULL, " +
                "`app_visit_has_seen_item_read_edit_tool_tip` INTEGER NOT NULL, " +
                "`open_order` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`))",
        )

        // Add index
        db.execSQL("CREATE UNIQUE INDEX `index_Safe_open_order` ON `Safe` (`open_order`)")

        // Insert open_order value in Safe_Old table
        db.execSQL("ALTER TABLE Safe_Old ADD open_order INTEGER")

        val incrementalValueUpdate = """
            WITH numbered_rows AS (
                SELECT id, ROW_NUMBER() OVER (ORDER BY ROWID) - 1 AS new_open_order
                FROM Safe_Old
            )
            UPDATE Safe_Old
            SET open_order = (SELECT new_open_order FROM numbered_rows WHERE numbered_rows.id = Safe_Old.id)
        """.trimIndent()
        db.execSQL(incrementalValueUpdate)

        // Copy back data from Safe_Old table to new Safe table
        db.execSQL("INSERT INTO Safe SELECT * FROM Safe_Old")

        // Drop temp table
        db.execSQL("DROP TABLE Safe_Old")

        // Re-disable legacy_alter_table
        db.execSQL("PRAGMA legacy_alter_table = OFF")
        // Re-enable foreign key checks
        db.execSQL("PRAGMA foreign_keys = ON")
    }
}
