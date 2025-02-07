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

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import co.touchlab.kermit.Logger
import com.lunabee.lblogger.LBLogger
import kotlinx.coroutines.runBlocking
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safe.BiometricCryptoMaterial
import studio.lunabee.onesafe.domain.model.safe.SafeCrypto
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safe.SafeSettings
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.model.GoogleDriveSettings
import studio.lunabee.onesafe.jvm.toByteArray
import studio.lunabee.onesafe.storage.model.RoomAppVisit
import studio.lunabee.onesafe.storage.model.RoomCtaState
import studio.lunabee.onesafe.storage.utils.addUniqueBiometricKeyTrigger
import studio.lunabee.onesafe.storage.utils.queryNumEntries
import studio.lunabee.onesafe.storage.utils.toSqlBlobString
import java.io.File
import java.time.Instant
import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration

private val logger: Logger = LBLogger.get<RoomMigration12to13>()

class RoomMigration12to13 @Inject constructor(
    private val safeMigrationProvider: MultiSafeMigrationProvider,
) : Migration(12, 13) {

    /**
     * Retrieves data to inject in new v13 tables during migration
     */
    interface MultiSafeMigrationProvider {
        suspend fun getSafeCrypto(db: SupportSQLiteDatabase): SafeCryptoMigration?
        suspend fun getSafeSettings(): SafeSettingsMigration
        suspend fun getAppVisit(): RoomAppVisit?
        suspend fun getDriveSettings(): GoogleDriveSettings?
        suspend fun getAutoBackupError(): AutoBackupErrorMigration?
        suspend fun getFilesAndIcons(): List<File>
        suspend fun onMigrationDone()
    }

    class SafeCryptoMigration(
        val id: SafeId,
        val salt: ByteArray,
        val encTest: ByteArray,
        val encIndexKey: ByteArray?,
        val encBubblesKey: ByteArray?,
        val encItemEditionKey: ByteArray?,
        val biometricCryptoMaterial: BiometricCryptoMaterial?,
    )

    class AutoBackupErrorMigration(
        val id: UUID,
        val date: ZonedDateTime,
        val code: String,
        val message: String?,
        val source: AutoBackupMode,
    )

    override fun migrate(db: SupportSQLiteDatabase) {
        runBlocking {
            val safeCryptoMigration = safeMigrationProvider.getSafeCrypto(db)

            if (safeCryptoMigration == null) {
                // Make sure the database is really empty because using null will cause the migration to not copy back data during
                // tables migration
                val itemCount = queryNumEntries(db, "SafeItem")
                check(itemCount == 0) {
                    "No master key/salt found but database contains items"
                }
                val contactCount = queryNumEntries(db, "Contact")
                check(contactCount == 0) {
                    "No master key/salt found but database contains contacts"
                }
            }

            val safeSettings = safeMigrationProvider.getSafeSettings()
            val appVisit = safeMigrationProvider.getAppVisit()
            val driveSettings = safeMigrationProvider.getDriveSettings()
            val autoBackupError = safeMigrationProvider.getAutoBackupError()
            val files = safeMigrationProvider.getFilesAndIcons()
            val safeId = safeCryptoMigration?.id?.id?.toByteArray()?.toSqlBlobString()

            val safeCrypto = safeCryptoMigration?.let {
                SafeCrypto(
                    id = safeCryptoMigration.id,
                    salt = safeCryptoMigration.salt,
                    encTest = safeCryptoMigration.encTest,
                    encIndexKey = safeCryptoMigration.encIndexKey ?: byteArrayOf(0),
                    encBubblesKey = safeCryptoMigration.encBubblesKey ?: byteArrayOf(0),
                    encItemEditionKey = safeCryptoMigration.encItemEditionKey ?: byteArrayOf(0),
                    biometricCryptoMaterial = safeCryptoMigration.biometricCryptoMaterial,
                    autoDestructionKey = null,
                )
            }

            migrateSafe(
                db = db,
                safeCrypto = safeCrypto,
                safeSettings = safeSettings,
                appVisit = appVisit,
                driveSettings = driveSettings,
            )

            db.addUniqueBiometricKeyTrigger()

            // Switch off foreign key constraint during migration
            db.execSQL("PRAGMA foreign_keys = OFF")
            // Switch on legacy alter to preserve foreign key constraints during migration
            // https://www.sqlite.org/pragma.html#pragma_legacy_alter_table
            db.execSQL(" PRAGMA legacy_alter_table = ON")

            migrateSafeItem(db, safeId)
            migrateIndexWordEntry(db, safeId)
            migrateContact(db, safeId)
            migrateSentMessage(db, safeId)
            migrateBackup(db, safeId)
            migrateAutoBackupError(db, autoBackupError, safeId)
            migrateSafeFile(db, safeId, files)

            // Re-disable legacy_alter_table
            db.execSQL("PRAGMA legacy_alter_table = OFF")
            // Re-enable foreign key checks
            db.execSQL("PRAGMA foreign_keys = ON")

            safeMigrationProvider.onMigrationDone()
        }
    }

    private fun migrateSafe(
        db: SupportSQLiteDatabase,
        safeCrypto: SafeCrypto?,
        safeSettings: SafeSettingsMigration,
        appVisit: RoomAppVisit?,
        driveSettings: GoogleDriveSettings?,
    ) {
        // Create Safe table and populate with new Safe provided
        db.execSQL(
            "CREATE TABLE `Safe` (" +
                "`id` BLOB NOT NULL, " +
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
                "`app_visit_has_seen_item_read_edit_tool_tip` INTEGER NOT NULL," +
                "PRIMARY KEY(`id`)" +
                ")",
        )

        if (safeCrypto != null) {
            checkNotNull(driveSettings)
            checkNotNull(appVisit)
            val roomBubblesHomeCardCtaState = RoomCtaState.fromCtaState(safeSettings.bubblesHomeCardCtaState)
            val roomEnableAutoBackupCtaState = RoomCtaState.fromCtaState(safeSettings.enableAutoBackupCtaState)
            val roomIndependentSafeInfoCtaState = RoomCtaState.fromCtaState(safeSettings.independentSafeInfoCtaState)
            val values = ContentValues().apply {
                put("id", safeCrypto.id.toByteArray())
                put("version", safeSettings.version)
                put("crypto_master_salt", safeCrypto.salt)
                put("crypto_enc_test", safeCrypto.encTest)
                put("crypto_enc_index_key", safeCrypto.encIndexKey)
                put("crypto_enc_bubbles_key", safeCrypto.encBubblesKey)
                put("crypto_enc_item_edition_key", safeCrypto.encItemEditionKey)
                put("crypto_biometric_crypto_material", safeCrypto.biometricCryptoMaterial?.raw)
                put("setting_material_you", safeSettings.materialYou)
                put("setting_automation", safeSettings.automation)
                put("setting_display_share_warning", safeSettings.displayShareWarning)
                put("setting_allow_screenshot", safeSettings.allowScreenshot)
                put("setting_bubbles_preview", safeSettings.bubblesPreview)
                put("setting_camera_system", safeSettings.cameraSystem.name)
                put("setting_auto_lock_osk_hidden_delay", safeSettings.autoLockOSKHiddenDelay.inWholeMilliseconds)
                put("setting_verify_password_interval", safeSettings.verifyPasswordInterval.name)
                put("setting_last_password_verification", safeSettings.lastPasswordVerification.toEpochMilli())
                put("setting_auto_lock_inactivity_delay", safeSettings.autoLockInactivityDelay.inWholeMilliseconds)
                put("setting_auto_lock_app_change_delay", safeSettings.autoLockAppChangeDelay.inWholeMilliseconds)
                put("setting_clipboard_delay", safeSettings.clipboardDelay.inWholeMilliseconds)
                put("setting_bubbles_resend_message_delay", safeSettings.bubblesResendMessageDelay.inWholeMilliseconds)
                put("setting_auto_lock_osk_inactivity_delay", safeSettings.autoLockOSKInactivityDelay.inWholeMilliseconds)
                put("setting_auto_backup_enabled", safeSettings.autoBackupEnabled)
                put("setting_auto_backup_frequency", safeSettings.autoBackupFrequency.inWholeMilliseconds)
                put("setting_auto_backup_max_number", safeSettings.autoBackupMaxNumber)
                put("setting_cloud_backup_enabled", safeSettings.cloudBackupEnabled)
                put("setting_keep_local_backup_enabled", safeSettings.keepLocalBackupEnabled)
                put("setting_item_ordering", safeSettings.itemOrdering.name)
                put("setting_items_layout_setting", safeSettings.itemLayout.name)
                put("setting_bubbles_home_card_cta_state", roomBubblesHomeCardCtaState.state.name)
                put("setting_bubbles_home_card_cta_timestamp", roomBubblesHomeCardCtaState.timestamp?.toEpochMilli())
                put("setting_drive_selected_account", driveSettings.selectedAccount)
                put("setting_drive_folder_id", driveSettings.folderId)
                put("setting_drive_folder_url", driveSettings.folderUrl)
                put("setting_enable_auto_backup_cta_state", roomEnableAutoBackupCtaState.state.name)
                put("setting_enable_auto_backup_cta_timestamp", roomEnableAutoBackupCtaState.timestamp?.toEpochMilli())
                put("app_visit_has_finish_one_safe_k_on_boarding", appVisit.hasFinishOneSafeKOnBoarding)
                put("app_visit_has_done_on_boarding_bubbles", appVisit.hasDoneOnBoardingBubbles)
                put("app_visit_has_hidden_camera_tips", appVisit.hasHiddenCameraTips)
                put("app_visit_has_seen_item_edition_url_tool_tip", appVisit.hasSeenItemEditionUrlToolTip)
                put("app_visit_has_seen_item_edition_emoji_tool_tip", appVisit.hasSeenItemEditionEmojiToolTip)
                put("app_visit_has_seen_item_read_edit_tool_tip", appVisit.hasSeenItemReadEditToolTip)
                put("setting_independent_safe_info_cta_state", roomIndependentSafeInfoCtaState.state.name)
                put("setting_independent_safe_info_cta_timestamp", roomIndependentSafeInfoCtaState.timestamp?.toEpochMilli())
            }
            db.insert("Safe", SQLiteDatabase.CONFLICT_FAIL, values)
        }
    }

    private fun migrateSafeItem(db: SupportSQLiteDatabase, safeId: String?) {
        val numEntries = queryNumEntries(db, "SafeItem")

        // Rename SafeItem table & delete indices to re-create them with the non-null safe_id column (without default value)
        db.execSQL("ALTER TABLE SafeItem RENAME TO SafeItem_Old")
        db.execSQL("DROP INDEX `index_SafeItem_parent_id`")
        db.execSQL("DROP INDEX `index_SafeItem_created_at`")
        db.execSQL("DROP INDEX `index_SafeItem_deleted_parent_id`")
        db.execSQL("DROP INDEX `index_SafeItem_consulted_at`")
        db.execSQL("DROP INDEX `index_SafeItem_index_alpha`")

        // Create SafeItem table with all indices
        db.execSQL(
            "CREATE TABLE `SafeItem` (" +
                "`id` BLOB NOT NULL, " +
                "`enc_name` BLOB, " +
                "`parent_id` BLOB, " +
                "`is_favorite` INTEGER NOT NULL, " +
                "`created_at` INTEGER NOT NULL, " +
                "`updated_at` INTEGER NOT NULL, " +
                "`position` REAL NOT NULL, " +
                "`icon_id` BLOB, " +
                "`enc_color` BLOB, " +
                "`deleted_at` INTEGER, " +
                "`deleted_parent_id` BLOB, " +
                "`consulted_at` INTEGER, " +
                "`index_alpha` REAL NOT NULL, " +
                "`safe_id` BLOB NOT NULL, " +
                "PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`parent_id`) REFERENCES `SafeItem`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , " +
                "FOREIGN KEY(`deleted_parent_id`) REFERENCES `SafeItem`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , " +
                "FOREIGN KEY(`safe_id`) REFERENCES `Safe`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        )
        db.execSQL("CREATE INDEX `index_SafeItem_parent_id` ON `SafeItem` (`parent_id`)")
        db.execSQL("CREATE INDEX `index_SafeItem_created_at` ON `SafeItem` (`created_at`)")
        db.execSQL("CREATE INDEX `index_SafeItem_deleted_parent_id` ON `SafeItem` (`deleted_parent_id`)")
        db.execSQL("CREATE INDEX `index_SafeItem_consulted_at` ON `SafeItem` (`consulted_at`)")
        db.execSQL("CREATE INDEX `index_SafeItem_index_alpha` ON `SafeItem` (`index_alpha`)")
        db.execSQL("CREATE INDEX `index_SafeItem_safe_id` ON `SafeItem` (`safe_id`)")

        if (safeId != null) {
            // Insert safe_id value in SafeItem_Old table
            db.execSQL("ALTER TABLE SafeItem_Old ADD safe_id BLOB NOT NULL DEFAULT $safeId")
            // Copy back data from SafeItem_Old table to new SafeItem table
            db.execSQL(
                "INSERT INTO SafeItem(" +
                    "id," +
                    "enc_name," +
                    "parent_id," +
                    "is_favorite," +
                    "created_at," +
                    "updated_at," +
                    "position," +
                    "icon_id," +
                    "enc_color," +
                    "deleted_at," +
                    "deleted_parent_id," +
                    "consulted_at," +
                    "index_alpha," +
                    "safe_id" +
                    ") SELECT " +
                    "id," +
                    "enc_name," +
                    "parent_id," +
                    "is_favorite," +
                    "created_at," +
                    "updated_at," +
                    "position," +
                    "icon_id," +
                    "enc_color," +
                    "deleted_at," +
                    "deleted_parent_id," +
                    "consulted_at," +
                    "index_alpha," +
                    "safe_id " +
                    "FROM SafeItem_Old",
            )
        }

        val migratedNumEntries = queryNumEntries(db, "SafeItem")
        check(numEntries == migratedNumEntries)

        // Drop temp table
        db.execSQL("DROP TABLE SafeItem_Old")
    }

    private fun migrateIndexWordEntry(db: SupportSQLiteDatabase, safeId: String?) {
        val numEntries = queryNumEntries(db, "IndexWordEntry")

        // Rename IndexWordEntry table & delete indices to re-create them with the non-null safe_id column (without default value)
        db.execSQL("ALTER TABLE IndexWordEntry RENAME TO IndexWordEntry_Old")
        db.execSQL("DROP INDEX `index_IndexWordEntry_item_match`")
        db.execSQL("DROP INDEX `index_IndexWordEntry_field_match`")

        db.execSQL(
            "CREATE TABLE `IndexWordEntry` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`word` BLOB NOT NULL, " +
                "`item_match` BLOB NOT NULL, " +
                "`field_match` BLOB, " +
                "`safe_id` BLOB NOT NULL, " +
                "FOREIGN KEY(`item_match`) REFERENCES `SafeItem`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , " +
                "FOREIGN KEY(`field_match`) REFERENCES `SafeItemField`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , " +
                "FOREIGN KEY(`safe_id`) REFERENCES `Safe`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        )
        db.execSQL("CREATE INDEX `index_IndexWordEntry_item_match` ON `IndexWordEntry` (`item_match`)")
        db.execSQL("CREATE INDEX `index_IndexWordEntry_field_match` ON `IndexWordEntry` (`field_match`)")
        db.execSQL("CREATE INDEX `index_IndexWordEntry_safe_id` ON `IndexWordEntry` (`safe_id`)")

        if (safeId != null) {
            val values = "`id`,`word`,`item_match`,`field_match`,`safe_id`"
            // Insert safe_id value in IndexWordEntry_Old table
            db.execSQL("ALTER TABLE IndexWordEntry_Old ADD safe_id BLOB NOT NULL DEFAULT $safeId")
            // Copy back data from IndexWordEntry_Old table to new IndexWordEntry table
            db.execSQL("INSERT INTO IndexWordEntry($values) SELECT $values FROM IndexWordEntry_Old")
        }

        val migratedNumEntries = queryNumEntries(db, "IndexWordEntry")
        check(numEntries == migratedNumEntries)

        // Drop temp table
        db.execSQL("DROP TABLE IndexWordEntry_Old")
    }

    private fun migrateContact(db: SupportSQLiteDatabase, safeId: String?) {
        val numEntries = queryNumEntries(db, "Contact")

        // Rename Contact table & delete indices to re-create them with the non-null safe_id column (without default value)
        db.execSQL("ALTER TABLE Contact RENAME TO Contact_Old")

        db.execSQL(
            "CREATE TABLE `Contact` (" +
                "`id` BLOB NOT NULL, " +
                "`enc_name` BLOB NOT NULL, " +
                "`enc_shared_key` BLOB, " +
                "`updated_at` INTEGER NOT NULL, " +
                "`shared_conversation_id` BLOB NOT NULL, " +
                "`enc_sharing_mode` BLOB NOT NULL, " +
                "`consulted_at` INTEGER DEFAULT null, " +
                "`safe_id` BLOB NOT NULL, " +
                "PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`safe_id`) REFERENCES `Safe`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        )
        db.execSQL("CREATE INDEX `index_Contact_safe_id` ON `Contact` (`safe_id`)")

        if (safeId != null) {
            val values =
                "`id`,`enc_name`,`enc_shared_key`,`updated_at`,`shared_conversation_id`,`enc_sharing_mode`,`consulted_at`,`safe_id`"
            // Insert safe_id value in Contact_Old table
            db.execSQL("ALTER TABLE Contact_Old ADD safe_id BLOB NOT NULL DEFAULT $safeId")
            // Copy back data from Contact_Old table to new Contact table
            db.execSQL("INSERT INTO Contact($values) SELECT $values FROM Contact_Old")
        }

        val migratedNumEntries = queryNumEntries(db, "Contact")
        check(numEntries == migratedNumEntries)

        // Drop temp table
        db.execSQL("DROP TABLE Contact_Old")
    }

    private fun migrateSentMessage(db: SupportSQLiteDatabase, safeId: String?) {
        val numEntries = queryNumEntries(db, "SentMessage")

        // Rename Contact table & delete indices to re-create them with the non-null safe_id column (without default value)
        db.execSQL("ALTER TABLE SentMessage RENAME TO SentMessage_Old")

        db.execSQL(
            "CREATE TABLE `SentMessage` (" +
                "`id` BLOB NOT NULL, " +
                "`enc_content` BLOB NOT NULL, " +
                "`enc_created_at` BLOB NOT NULL, " +
                "`contact_id` BLOB NOT NULL, " +
                "`safe_id` BLOB NOT NULL, " +
                "`order` REAL NOT NULL, " +
                "PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`safe_id`) REFERENCES `Safe`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                "FOREIGN KEY(`id`) REFERENCES `Message`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        )
        db.execSQL("CREATE INDEX `index_SentMessage_safe_id` ON `SentMessage` (`safe_id`)")

        if (safeId != null) {
            val values = "`id`,`enc_content`,`enc_created_at`,`contact_id`,`safe_id`,`order`"
            // Insert safe_id value in Contact_Old table
            db.execSQL("ALTER TABLE SentMessage_Old ADD safe_id BLOB NOT NULL DEFAULT $safeId")
            // Copy back data from Contact_Old table to new Contact table
            db.execSQL("INSERT INTO SentMessage($values) SELECT $values FROM SentMessage_Old")
        }

        val migratedNumEntries = queryNumEntries(db, "SentMessage")
        check(numEntries == migratedNumEntries)

        // Drop temp table
        db.execSQL("DROP TABLE SentMessage_Old")
    }

    private fun migrateBackup(db: SupportSQLiteDatabase, safeId: String?) {
        val numEntries = queryNumEntries(db, "Backup")

        // Rename Backup table & delete indices to re-create them with the non-null safe_id column (without default value)
        db.execSQL("ALTER TABLE Backup RENAME TO Backup_Old")
        db.execSQL("DROP INDEX `index_Backup_remote_id`")

        db.execSQL(
            "CREATE TABLE `Backup` (" +
                "`id` TEXT NOT NULL, " +
                "`remote_id` TEXT, " +
                "`local_file` TEXT, " +
                "`date` INTEGER NOT NULL, " +
                "`safe_id` BLOB, " +
                "`name` TEXT, " +
                "PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`safe_id`) REFERENCES `Safe`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        )
        db.execSQL("CREATE UNIQUE INDEX `index_Backup_remote_id` ON `Backup` (`remote_id`)")
        db.execSQL("CREATE INDEX `index_Backup_safe_id` ON `Backup` (`safe_id`)")

        if (safeId != null) {
            val values = "`id`,`remote_id`,`local_file`,`date`,`safe_id`,`name`"
            // Insert safe_id value in Backup_Old table
            db.execSQL("ALTER TABLE Backup_Old ADD safe_id BLOB NOT NULL DEFAULT $safeId")
            db.execSQL("ALTER TABLE Backup_Old ADD name TEXT DEFAULT $safeId")
            db.execSQL("UPDATE Backup_Old SET name = id")
            // Copy back data from Backup_Old table to new Backup table
            db.execSQL("INSERT INTO Backup($values) SELECT $values FROM Backup_Old")
        }

        val migratedNumEntries = queryNumEntries(db, "Backup")
        check(numEntries == migratedNumEntries)

        // Drop temp table
        db.execSQL("DROP TABLE Backup_Old")
    }

    private fun migrateAutoBackupError(db: SupportSQLiteDatabase, autoBackupError: AutoBackupErrorMigration?, safeId: String?) {
        db.execSQL(
            "CREATE TABLE `AutoBackupError` (" +
                "`id` BLOB NOT NULL, " +
                "`date` TEXT NOT NULL, " +
                "`code` TEXT NOT NULL, " +
                "`message` TEXT, " +
                "`source` TEXT NOT NULL, " +
                "`safe_id` BLOB NOT NULL, " +
                "PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`safe_id`) REFERENCES `Safe`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE" +
                ")",
        )
        db.execSQL("CREATE INDEX `index_AutoBackupError_safe_id` ON `AutoBackupError` (`safe_id`)")

        if (autoBackupError != null && safeId != null) {
            val values = ContentValues().apply {
                put("id", autoBackupError.id.toByteArray())
                put("date", autoBackupError.date.toString())
                put("code", autoBackupError.code)
                put("message", autoBackupError.message)
                put("source", autoBackupError.source.name)
                put("safe_id", safeId)
            }
            db.insert("AutoBackupError", SQLiteDatabase.CONFLICT_FAIL, values)
        }
    }

    private fun migrateSafeFile(db: SupportSQLiteDatabase, safeId: String?, files: List<File>) {
        db.execSQL(
            "CREATE TABLE `SafeFile` (" +
                "`file` TEXT NOT NULL, " +
                "`safe_id` BLOB NOT NULL, " +
                "PRIMARY KEY(`file`), " +
                "FOREIGN KEY(`safe_id`) REFERENCES `Safe`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE" +
                ")",
        )
        db.execSQL("CREATE INDEX `index_SafeFile_safe_id` ON `SafeFile` (`safe_id`)")

        if (safeId != null) {
            files.forEach {
                val value = ContentValues().apply {
                    put("file", it.path)
                    put("safe_id", safeId)
                }
                db.insert("SafeFile", SQLiteDatabase.CONFLICT_FAIL, value)
            }
        } else if (files.isNotEmpty()) {
            logger.e("SafeId is null but files are not empty")
        }
    }

    @Suppress("LongParameterList")
    class SafeSettingsMigration(
        val version: Int,
        val materialYou: Boolean,
        val automation: Boolean,
        val displayShareWarning: Boolean,
        val allowScreenshot: Boolean,
        val bubblesPreview: Boolean,
        val cameraSystem: CameraSystem,
        val autoLockOSKHiddenDelay: Duration,
        val verifyPasswordInterval: VerifyPasswordInterval,
        val bubblesHomeCardCtaState: CtaState,
        val autoLockInactivityDelay: Duration,
        val autoLockAppChangeDelay: Duration,
        val clipboardDelay: Duration,
        val bubblesResendMessageDelay: Duration,
        val autoLockOSKInactivityDelay: Duration,
        val autoBackupEnabled: Boolean,
        val autoBackupFrequency: Duration,
        val autoBackupMaxNumber: Int,
        val cloudBackupEnabled: Boolean,
        val keepLocalBackupEnabled: Boolean,
        val itemOrdering: ItemOrder,
        val itemLayout: ItemLayout,
        val enableAutoBackupCtaState: CtaState,
        val lastPasswordVerification: Instant,
        val independentSafeInfoCtaState: CtaState,
    ) {
        constructor(safeSettings: SafeSettings) : this(
            version = safeSettings.version,
            materialYou = safeSettings.materialYou,
            automation = safeSettings.automation,
            displayShareWarning = safeSettings.displayShareWarning,
            allowScreenshot = safeSettings.allowScreenshot,
            bubblesPreview = safeSettings.bubblesPreview,
            cameraSystem = safeSettings.cameraSystem,
            autoLockOSKHiddenDelay = safeSettings.autoLockOSKHiddenDelay,
            verifyPasswordInterval = safeSettings.verifyPasswordInterval,
            bubblesHomeCardCtaState = safeSettings.bubblesHomeCardCtaState,
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
            enableAutoBackupCtaState = safeSettings.enableAutoBackupCtaState,
            lastPasswordVerification = safeSettings.lastPasswordVerification,
            independentSafeInfoCtaState = safeSettings.independentSafeInfoCtaState,
        )
    }
}
