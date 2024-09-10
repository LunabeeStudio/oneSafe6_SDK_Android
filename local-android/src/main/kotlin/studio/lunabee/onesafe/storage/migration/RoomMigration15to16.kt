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
            "CREATE TABLE `Safe` (" +
                "`$Id` BLOB NOT NULL, " +
                "`$Version` INTEGER NOT NULL, " +
                "`$CryptoMasterSalt` BLOB NOT NULL, " +
                "`$CryptoEncTest` BLOB NOT NULL, " +
                "`$CryptoEncIndexKey` BLOB NOT NULL, " +
                "`$CryptoEncBubblesKey` BLOB, " +
                "`$CryptoEncItemEditionKey` BLOB NOT NULL, " +
                "`$CryptoBiometricCryptoMaterial` BLOB, " +
                "`$SettingMaterialYou` INTEGER NOT NULL, " +
                "`$SettingAutomation` INTEGER NOT NULL, " +
                "`$SettingDisplayShareWarning` INTEGER NOT NULL, " +
                "`$SettingAllowScreenshot` INTEGER NOT NULL, " +
                "`$SettingShakeToLock` INTEGER NOT NULL, " +
                "`$SettingBubblesPreview` INTEGER NOT NULL, " +
                "`$SettingCameraSystem` TEXT NOT NULL, " +
                "`$SettingAutoLockOskHiddenDelay` INTEGER NOT NULL, " +
                "`$SettingVerifyPasswordInterval` TEXT NOT NULL, " +
                "`$SettingLastPasswordVerification` INTEGER NOT NULL, " +
                "`$SettingAutoLockInactivityDelay` INTEGER NOT NULL, " +
                "`$SettingAutoLockAppChangeDelay` INTEGER NOT NULL, " +
                "`$SettingClipboardDelay` INTEGER NOT NULL, " +
                "`$SettingBubblesResendMessageDelay` INTEGER NOT NULL, " +
                "`$SettingAutoLockOskInactivityDelay` INTEGER NOT NULL, " +
                "`$SettingAutoBackupEnabled` INTEGER NOT NULL, " +
                "`$SettingAutoBackupFrequency` INTEGER NOT NULL, " +
                "`$SettingAutoBackupMaxNumber` INTEGER NOT NULL, " +
                "`$SettingCloudBackupEnabled` INTEGER NOT NULL, " +
                "`$SettingKeepLocalBackupEnabled` INTEGER NOT NULL, " +
                "`$SettingItemOrdering` TEXT NOT NULL, " +
                "`$SettingItemsLayoutSetting` TEXT NOT NULL, " +
                "`$SettingBubblesHomeCardCtaState` TEXT NOT NULL, " +
                "`$SettingBubblesHomeCardCtaTimestamp` INTEGER, " +
                "`$SettingDriveSelectedAccount` TEXT, " +
                "`$SettingDriveFolderId` TEXT, " +
                "`$SettingDriveFolderUrl` TEXT, " +
                "`$SettingEnableAutoBackupCtaState` TEXT NOT NULL, " +
                "`$SettingEnableAutoBackupCtaTimestamp` INTEGER, " +
                "`$SettingIndependentSafeInfoCtaState` TEXT NOT NULL, " +
                "`$SettingIndependentSafeInfoCtaTimestamp` INTEGER, " +
                "`$AppVisitHasFinishOneSafeKOnBoarding` INTEGER NOT NULL, " +
                "`$AppVisitHasDoneOnBoardingBubbles` INTEGER NOT NULL, " +
                "`$AppVisitHasHiddenCameraTips` INTEGER NOT NULL, " +
                "`$AppVisitHasSeenItemEditionUrlToolTip` INTEGER NOT NULL, " +
                "`$AppVisitHasSeenItemEditionEmojiToolTip` INTEGER NOT NULL, " +
                "`$AppVisitHasSeenItemReadEditToolTip` INTEGER NOT NULL, " +
                "`$OpenOrder` INTEGER NOT NULL, " +
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

        val valueNames =
            "`$Id`," +
                "`$Version`," +
                "`$CryptoMasterSalt`," +
                "`$CryptoEncTest`," +
                "`$CryptoEncIndexKey`," +
                "`$CryptoEncBubblesKey`," +
                "`$CryptoEncItemEditionKey`," +
                "`$CryptoBiometricCryptoMaterial`," +
                "`$SettingMaterialYou`," +
                "`$SettingAutomation`," +
                "`$SettingDisplayShareWarning`," +
                "`$SettingAllowScreenshot`," +
                "`$SettingShakeToLock`," +
                "`$SettingBubblesPreview`," +
                "`$SettingCameraSystem`," +
                "`$SettingAutoLockOskHiddenDelay`," +
                "`$SettingVerifyPasswordInterval`," +
                "`$SettingLastPasswordVerification`," +
                "`$SettingAutoLockInactivityDelay`," +
                "`$SettingAutoLockAppChangeDelay`," +
                "`$SettingClipboardDelay`," +
                "`$SettingBubblesResendMessageDelay`," +
                "`$SettingAutoLockOskInactivityDelay`," +
                "`$SettingAutoBackupEnabled`," +
                "`$SettingAutoBackupFrequency`," +
                "`$SettingAutoBackupMaxNumber`," +
                "`$SettingCloudBackupEnabled`," +
                "`$SettingKeepLocalBackupEnabled`," +
                "`$SettingItemOrdering`," +
                "`$SettingItemsLayoutSetting`," +
                "`$SettingBubblesHomeCardCtaState`," +
                "`$SettingBubblesHomeCardCtaTimestamp`," +
                "`$SettingDriveSelectedAccount`," +
                "`$SettingDriveFolderId`," +
                "`$SettingDriveFolderUrl`," +
                "`$SettingEnableAutoBackupCtaState`," +
                "`$SettingEnableAutoBackupCtaTimestamp`," +
                "`$SettingIndependentSafeInfoCtaState`," +
                "`$SettingIndependentSafeInfoCtaTimestamp`," +
                "`$AppVisitHasFinishOneSafeKOnBoarding`," +
                "`$AppVisitHasDoneOnBoardingBubbles`," +
                "`$AppVisitHasHiddenCameraTips`," +
                "`$AppVisitHasSeenItemEditionUrlToolTip`," +
                "`$AppVisitHasSeenItemEditionEmojiToolTip`," +
                "`$AppVisitHasSeenItemReadEditToolTip`," +
                "`$OpenOrder`"

        // Copy back data from Safe_Old table to new Safe table
        db.execSQL("INSERT INTO Safe($valueNames) SELECT $valueNames FROM Safe_Old")

        // Drop temp table
        db.execSQL("DROP TABLE Safe_Old")

        // Re-disable legacy_alter_table
        db.execSQL("PRAGMA legacy_alter_table = OFF")
        // Re-enable foreign key checks
        db.execSQL("PRAGMA foreign_keys = ON")
    }
}

private const val Id = "id"
private const val Version = "version"
private const val CryptoMasterSalt = "crypto_master_salt"
private const val CryptoEncTest = "crypto_enc_test"
private const val CryptoEncIndexKey = "crypto_enc_index_key"
private const val CryptoEncBubblesKey = "crypto_enc_bubbles_key"
private const val CryptoEncItemEditionKey = "crypto_enc_item_edition_key"
private const val CryptoBiometricCryptoMaterial = "crypto_biometric_crypto_material"
private const val SettingMaterialYou = "setting_material_you"
private const val SettingAutomation = "setting_automation"
private const val SettingDisplayShareWarning = "setting_display_share_warning"
private const val SettingAllowScreenshot = "setting_allow_screenshot"
private const val SettingShakeToLock = "setting_shake_to_lock"
private const val SettingBubblesPreview = "setting_bubbles_preview"
private const val SettingCameraSystem = "setting_camera_system"
private const val SettingAutoLockOskHiddenDelay = "setting_auto_lock_osk_hidden_delay"
private const val SettingVerifyPasswordInterval = "setting_verify_password_interval"
private const val SettingLastPasswordVerification = "setting_last_password_verification"
private const val SettingAutoLockInactivityDelay = "setting_auto_lock_inactivity_delay"
private const val SettingAutoLockAppChangeDelay = "setting_auto_lock_app_change_delay"
private const val SettingClipboardDelay = "setting_clipboard_delay"
private const val SettingBubblesResendMessageDelay = "setting_bubbles_resend_message_delay"
private const val SettingAutoLockOskInactivityDelay = "setting_auto_lock_osk_inactivity_delay"
private const val SettingAutoBackupEnabled = "setting_auto_backup_enabled"
private const val SettingAutoBackupFrequency = "setting_auto_backup_frequency"
private const val SettingAutoBackupMaxNumber = "setting_auto_backup_max_number"
private const val SettingCloudBackupEnabled = "setting_cloud_backup_enabled"
private const val SettingKeepLocalBackupEnabled = "setting_keep_local_backup_enabled"
private const val SettingItemOrdering = "setting_item_ordering"
private const val SettingItemsLayoutSetting = "setting_items_layout_setting"
private const val SettingBubblesHomeCardCtaState = "setting_bubbles_home_card_cta_state"
private const val SettingBubblesHomeCardCtaTimestamp = "setting_bubbles_home_card_cta_timestamp"
private const val SettingDriveSelectedAccount = "setting_drive_selected_account"
private const val SettingDriveFolderId = "setting_drive_folder_id"
private const val SettingDriveFolderUrl = "setting_drive_folder_url"
private const val SettingEnableAutoBackupCtaState = "setting_enable_auto_backup_cta_state"
private const val SettingEnableAutoBackupCtaTimestamp = "setting_enable_auto_backup_cta_timestamp"
private const val SettingIndependentSafeInfoCtaState = "setting_independent_safe_info_cta_state"
private const val SettingIndependentSafeInfoCtaTimestamp = "setting_independent_safe_info_cta_timestamp"
private const val AppVisitHasFinishOneSafeKOnBoarding = "app_visit_has_finish_one_safe_k_on_boarding"
private const val AppVisitHasDoneOnBoardingBubbles = "app_visit_has_done_on_boarding_bubbles"
private const val AppVisitHasHiddenCameraTips = "app_visit_has_hidden_camera_tips"
private const val AppVisitHasSeenItemEditionUrlToolTip = "app_visit_has_seen_item_edition_url_tool_tip"
private const val AppVisitHasSeenItemEditionEmojiToolTip = "app_visit_has_seen_item_edition_emoji_tool_tip"
private const val AppVisitHasSeenItemReadEditToolTip = "app_visit_has_seen_item_read_edit_tool_tip"
private const val OpenOrder = "open_order"
