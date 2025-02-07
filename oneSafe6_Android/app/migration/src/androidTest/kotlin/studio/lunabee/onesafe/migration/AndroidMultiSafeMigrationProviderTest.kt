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
 * Created by Lunabee Studio / Date - 8/26/2024 - for the oneSafe6 SDK.
 * Last modified 8/26/24, 11:12 AM
 */

package studio.lunabee.onesafe.migration

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import studio.lunabee.di.InMemoryMainDatabaseNamesModule
import studio.lunabee.onesafe.domain.qualifier.DatabaseName
import studio.lunabee.onesafe.migration.utils.AndroidMultiSafeMigrationProvider
import studio.lunabee.onesafe.storage.MainDatabase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltTest
import studio.lunabee.onesafe.test.assertThrows
import javax.inject.Inject
import kotlin.test.Test
import kotlin.test.assertNull

@HiltAndroidTest
@UninstallModules(InMemoryMainDatabaseNamesModule::class)
class AndroidMultiSafeMigrationProviderTest : OSHiltTest() {
    @get:Rule override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.SignedOut

    @BindValue
    @DatabaseName(DatabaseName.Type.Main)
    val dbName: String = "migration-test"

    @Inject
    lateinit var migrationProvider: AndroidMultiSafeMigrationProvider

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        MainDatabase::class.java,
    )

    @Test
    fun no_crypto_test(): TestResult = runTest {
        val db = helper.createDatabase(dbName, 12)
        val itemValues = ContentValues().apply {
            put("id", byteArrayOf(0))
            put("enc_name", byteArrayOf(0))
            putNull("parent_id")
            put("is_favorite", false)
            put("created_at", "")
            put("updated_at", "")
            put("position", 0)
            putNull("icon_id")
            putNull("enc_color")
            putNull("deleted_at")
            putNull("deleted_parent_id")
            put("consulted_at", "")
            put("index_alpha", 0)
        }

        val contactValues = ContentValues().apply {
            put("id", byteArrayOf(0))
            put("enc_name", byteArrayOf(0))
            put("enc_shared_key", byteArrayOf(0))
            put("updated_at", "")
            put("shared_conversation_id", byteArrayOf(0))
            put("enc_sharing_mode", byteArrayOf())
            putNull("consulted_at")
        }

        db.insert("SafeItem", SQLiteDatabase.CONFLICT_FAIL, itemValues)
        db.insert("Contact", SQLiteDatabase.CONFLICT_FAIL, contactValues)

        // Fails due to item
        assertThrows<IllegalStateException> {
            assertNull(migrationProvider.getSafeCrypto(db))
        }.printStackTrace()

        db.delete("SafeItem", null, null)

        // Fails due to contact
        assertThrows<IllegalStateException> {
            assertNull(migrationProvider.getSafeCrypto(db))
        }.printStackTrace()

        db.delete("Contact", null, null)

        // No crypto & empty db
        assertNull(migrationProvider.getSafeCrypto(db))
    }
}
