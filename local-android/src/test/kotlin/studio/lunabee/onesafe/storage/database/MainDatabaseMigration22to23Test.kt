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
 * Last modified 9/9/24, 4:25 PM
 */

package studio.lunabee.onesafe.storage.database

import android.content.ContentValues
import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.jvm.toByteArray
import studio.lunabee.onesafe.storage.MainDatabase
import studio.lunabee.onesafe.storage.migration.RoomMigration22to23
import studio.lunabee.onesafe.storage.model.RoomCtaState
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.assertDoesNotThrow
import studio.lunabee.onesafe.test.nextString
import studio.lunabee.onesafe.test.testUUIDs
import javax.inject.Inject
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(
    application = HiltTestApplication::class,
)
@HiltAndroidTest
class MainDatabaseMigration22to23Test {
    private val dbName = "migration-test"

    @get:Rule
    val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var mainDatabase: dagger.Lazy<MainDatabase>

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        MainDatabase::class.java,
    )

    @Inject
    lateinit var migration22to23: RoomMigration22to23

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun migration22to23_test(): TestResult = runTest {
        val bubblesKey = OSTestConfig.random.nextBytes(3)
        val contentValuesList = List(1) {
            ContentValues().apply {
                put("id", testUUIDs[it].toByteArray())
                put("version", OSTestConfig.random.nextInt())
                put("crypto_master_salt", OSTestConfig.random.nextBytes(3))
                put("crypto_enc_test", OSTestConfig.random.nextBytes(3))
                put("crypto_enc_index_key", OSTestConfig.random.nextBytes(3))
                put("crypto_enc_bubbles_key", bubblesKey)
                put("crypto_enc_item_edition_key", OSTestConfig.random.nextBytes(3))
                put("crypto_biometric_crypto_material", OSTestConfig.random.nextBytes(3))
                put("crypto_auto_destruction_key", OSTestConfig.random.nextBytes(3))
                put("setting_material_you", OSTestConfig.random.nextBoolean())
                put("setting_automation", OSTestConfig.random.nextBoolean())
                put("setting_display_share_warning", OSTestConfig.random.nextBoolean())
                put("setting_allow_screenshot", OSTestConfig.random.nextBoolean())
                put("setting_bubbles_preview", OSTestConfig.random.nextBoolean())
                put("setting_camera_system", CameraSystem.entries.random(OSTestConfig.random).name)
                put("setting_auto_lock_osk_hidden_delay", OSTestConfig.random.nextInt())
                put("setting_verify_password_interval", VerifyPasswordInterval.entries.random(OSTestConfig.random).name)
                put("setting_last_password_verification", OSTestConfig.random.nextInt())
                put("setting_auto_lock_inactivity_delay", OSTestConfig.random.nextInt())
                put("setting_auto_lock_app_change_delay", OSTestConfig.random.nextInt())
                put("setting_clipboard_delay", OSTestConfig.random.nextInt())
                put("setting_bubbles_resend_message_delay", OSTestConfig.random.nextInt())
                put("setting_auto_lock_osk_inactivity_delay", OSTestConfig.random.nextInt())
                put("setting_auto_backup_enabled", OSTestConfig.random.nextBoolean())
                put("setting_auto_backup_frequency", OSTestConfig.random.nextInt())
                put("setting_auto_backup_max_number", OSTestConfig.random.nextBoolean())
                put("setting_cloud_backup_enabled", OSTestConfig.random.nextBoolean())
                put("setting_keep_local_backup_enabled", OSTestConfig.random.nextBoolean())
                put("setting_item_ordering", ItemOrder.entries.random(OSTestConfig.random).name)
                put("setting_items_layout_setting", ItemLayout.entries.random(OSTestConfig.random).name)
                put("setting_bubbles_home_card_cta_state", RoomCtaState.State.entries.random(OSTestConfig.random).name)
                put("setting_bubbles_home_card_cta_timestamp", OSTestConfig.random.nextInt())
                put("setting_drive_selected_account", OSTestConfig.random.nextString())
                put("setting_drive_folder_id", OSTestConfig.random.nextString())
                put("setting_drive_folder_url", OSTestConfig.random.nextString())
                put("setting_enable_auto_backup_cta_state", RoomCtaState.State.entries.random(OSTestConfig.random).name)
                put("setting_enable_auto_backup_cta_timestamp", OSTestConfig.random.nextInt())
                put("app_visit_has_finish_one_safe_k_on_boarding", OSTestConfig.random.nextBoolean())
                put("app_visit_has_done_on_boarding_bubbles", OSTestConfig.random.nextBoolean())
                put("app_visit_has_hidden_camera_tips", OSTestConfig.random.nextBoolean())
                put("app_visit_has_seen_item_edition_url_tool_tip", OSTestConfig.random.nextBoolean())
                put("app_visit_has_seen_item_edition_emoji_tool_tip", OSTestConfig.random.nextBoolean())
                put("app_visit_has_seen_item_read_edit_tool_tip", OSTestConfig.random.nextBoolean())
                put("app_visit_has_seen_dialog_message_save_confirmation", OSTestConfig.random.nextBoolean())
                put("setting_independent_safe_info_cta_state", RoomCtaState.State.entries.random(OSTestConfig.random).name)
                put("setting_independent_safe_info_cta_timestamp", OSTestConfig.random.nextInt())
                put("setting_shake_to_lock", OSTestConfig.random.nextBoolean())
                put("open_order", OSTestConfig.random.nextInt())
                put("is_panic_destruction_enabled", OSTestConfig.random.nextBoolean())
            }
        }
        helper.createDatabase(dbName, 22).use { db ->
            contentValuesList.forEach { contentValues ->
                db.insert("Safe", 0, contentValues)
            }
        }

        val db = helper.runMigrationsAndValidate(dbName, 23, true, migration22to23)
        val cursor = db.query("SELECT setting_prevention_warning_cta_state FROM Safe")
        assertEquals(1, cursor.count)
        while (cursor.moveToNext()) {
            val ctaState = cursor.getString(0)
            assertEquals(RoomCtaState.State.DismissedAt.name, ctaState)
        }
        cursor.close()
        db.close()

        assertDoesNotThrow {
            mainDatabase
                .get()
                .safeDao()
                .getAllOrderByLastOpenAsc()
        }
    }
}
