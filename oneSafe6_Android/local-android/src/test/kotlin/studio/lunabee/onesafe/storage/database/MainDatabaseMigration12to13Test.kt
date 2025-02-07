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
 * Last modified 8/26/24, 11:16 AM
 */

package studio.lunabee.onesafe.storage.database

import android.database.sqlite.SQLiteConstraintException
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import studio.lunabee.onesafe.domain.model.safe.BiometricCryptoMaterial
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.importexport.model.AutoBackupError
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.model.GoogleDriveSettings
import studio.lunabee.onesafe.storage.MainDatabase
import studio.lunabee.onesafe.storage.migration.RoomMigration12to13
import studio.lunabee.onesafe.storage.model.RoomAppVisit
import studio.lunabee.onesafe.storage.utils.toSqlBlobString
import studio.lunabee.onesafe.test.CommonTestUtils
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.OSTestUtils
import studio.lunabee.onesafe.test.assertThrows
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.jvm.toByteArray
import studio.lunabee.onesafe.jvm.toUUID
import java.io.File
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(
    application = HiltTestApplication::class,
)
@HiltAndroidTest
class MainDatabaseMigration12to13Test {

    private val dbName = "migration-test"

    @get:Rule
    val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        MainDatabase::class.java,
    )

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    // TODO <multisafe> Add Contact, IndexWordEntry and Backup + populate settings randomly
    @Test
    fun dummy_test() {
        val dummySafeMigrationProvider = DummySafeMigrationProvider()
        val migration12to13 = RoomMigration12to13(
            safeMigrationProvider = dummySafeMigrationProvider,
        )

        val itemId = testUUIDs[0].toByteArray()
        val itemIdSqlBlob = itemId.toSqlBlobString()

        val fieldId = testUUIDs[1].toByteArray()
        val fieldIdSqlBlob = fieldId.toSqlBlobString()

        val data = OSTestConfig.random.nextBytes(10)
        val dataSqlBlob = data.toSqlBlobString()

        helper.createDatabase(dbName, 12).use { db ->
            db.execSQL(
                "INSERT INTO SafeItem VALUES ($itemIdSqlBlob, " +
                    "NULL, " +
                    "NULL, " +
                    "false, " +
                    "0, " +
                    "0, " +
                    "0, " +
                    "NULL, " +
                    "NULL, " +
                    "NULL, " +
                    "NULL, " +
                    "NULL, " +
                    "0)",
            )
            db.execSQL(
                "INSERT INTO SafeItemField VALUES ($fieldIdSqlBlob, " +
                    "NULL, " +
                    "0.0, " +
                    "$itemIdSqlBlob, " +
                    "NULL, " +
                    "NULL, " +
                    "false, " +
                    "NULL, " +
                    "0, " +
                    "false, " +
                    "NULL, " +
                    "NULL, " +
                    "false, " +
                    "NULL)",
            )
            db.execSQL("INSERT INTO IndexWordEntry VALUES (0, $dataSqlBlob, $itemIdSqlBlob, $fieldIdSqlBlob)")
            db.execSQL("INSERT INTO Backup VALUES (0, NULL, NULL, 0)")
            db.execSQL("INSERT INTO Contact VALUES (0, $dataSqlBlob, $dataSqlBlob, 0, $dataSqlBlob, $dataSqlBlob, 0)")
        }

        val db = helper.runMigrationsAndValidate(dbName, 13, true, migration12to13)
        db.execSQL("PRAGMA foreign_keys = ON")

        val itemCursor = db.query("SELECT * FROM SafeItem").apply { moveToFirst() }
        val actualItemId = itemCursor.getBlob("id")
        assertContentEquals(itemId, actualItemId)
        val actualSafeId = itemCursor.getBlob("safe_id")
        assertContentEquals(dummySafeMigrationProvider.safeId.toByteArray(), actualSafeId)

        val fieldCursor = db.query("SELECT * FROM SafeItemField").apply { moveToFirst() }
        val actualFieldId = fieldCursor.getBlob("id")
        assertContentEquals(fieldId, actualFieldId)
        val actualFieldItemId = fieldCursor.getBlob("item_id")
        assertContentEquals(itemId, actualFieldItemId)

        val safeCryptoCursor = db.query("SELECT * FROM Safe").apply { moveToFirst() }
        assertEquals(1, safeCryptoCursor.count)
        val safeCryptoId = safeCryptoCursor.getBlob("id")
        assertContentEquals(dummySafeMigrationProvider.safeId.toByteArray(), safeCryptoId)
        val safeCryptoMasterSalt = safeCryptoCursor.getBlob("crypto_master_salt")
        assertContentEquals(dummySafeMigrationProvider.salt, safeCryptoMasterSalt)
        val safeCryptoEncTest = safeCryptoCursor.getBlob("crypto_enc_test")
        assertContentEquals(dummySafeMigrationProvider.encTest, safeCryptoEncTest)
        val safeCryptoEncIndexKey = safeCryptoCursor.getBlob("crypto_enc_index_key")
        assertContentEquals(dummySafeMigrationProvider.encIndexKey, safeCryptoEncIndexKey)
        val safeCryptoEncBubblesKey = safeCryptoCursor.getBlob("crypto_enc_bubbles_key")
        assertContentEquals(dummySafeMigrationProvider.encBubblesKey, safeCryptoEncBubblesKey)
        val safeCryptoEncItemEditionKey = safeCryptoCursor.getBlob("crypto_enc_item_edition_key")
        assertContentEquals(dummySafeMigrationProvider.encItemEditionKey, safeCryptoEncItemEditionKey)

        // Check foreign key constraint still there
        val badItemIdSqlBlob = testUUIDs[2].toByteArray().toSqlBlobString()
        val badFieldIdSqlBlob = testUUIDs[3].toByteArray().toSqlBlobString()
        assertThrows<SQLiteConstraintException> {
            db.execSQL(
                "INSERT INTO SafeItemField VALUES ($badFieldIdSqlBlob, " +
                    "NULL, " +
                    "0.0, " +
                    "$badItemIdSqlBlob, " +
                    "NULL, " +
                    "NULL, " +
                    "false, " +
                    "NULL, " +
                    "0, " +
                    "false, " +
                    "NULL, " +
                    "NULL, " +
                    "false, " +
                    "NULL)",
            )
        }

        val autoBackupErrorCursor = db.query("SELECT * FROM AutoBackupError").apply { moveToFirst() }
        val expectedAutoBackupError = dummySafeMigrationProvider.autoBackupError?.let {
            AutoBackupError(
                id = it.id,
                date = it.date,
                code = it.code,
                message = it.message,
                source = it.source,
                safeId = firstSafeId,
            )
        }
        if (expectedAutoBackupError != null) {
            assertEquals(1, autoBackupErrorCursor.count)
            val actualAutoBackupError = AutoBackupError(
                id = autoBackupErrorCursor.getBlob("id").toUUID(),
                date = ZonedDateTime.parse(autoBackupErrorCursor.getString("date")),
                code = autoBackupErrorCursor.getString("code")!!,
                message = autoBackupErrorCursor.getString("message"),
                source = AutoBackupMode.valueOf(autoBackupErrorCursor.getString("source")!!),
                safeId = SafeId(autoBackupErrorCursor.getBlob("id")),
            )
            assertEquals(expectedAutoBackupError, actualAutoBackupError)
        } else {
            assertEquals(0, autoBackupErrorCursor.count)
        }
    }
}

internal class DummySafeMigrationProvider : RoomMigration12to13.MultiSafeMigrationProvider {

    val safeId = testUUIDs[10]
    val salt = OSTestConfig.random.nextBytes(10)
    val encTest = OSTestConfig.random.nextBytes(10)
    val encIndexKey = OSTestConfig.random.nextBytes(10)
    val encBubblesKey = OSTestConfig.random.nextBytes(10)
    val encItemEditionKey = OSTestConfig.random.nextBytes(10)
    val biometricCryptoMaterial = BiometricCryptoMaterial(OSTestConfig.random.nextBytes(64))

    val autoBackupError: RoomMigration12to13.AutoBackupErrorMigration? = if (OSTestConfig.random.nextBoolean()) {
        RoomMigration12to13.AutoBackupErrorMigration(
            id = testUUIDs[0],
            date = ZonedDateTime.now(OSTestConfig.clock),
            code = "AutoBackupError_code",
            message = "AutoBackupError_message",
            source = AutoBackupMode.CloudOnly,
        )
    } else {
        null
    }

    override suspend fun getSafeCrypto(db: SupportSQLiteDatabase): RoomMigration12to13.SafeCryptoMigration {
        return RoomMigration12to13.SafeCryptoMigration(
            id = SafeId(safeId),
            salt = salt,
            encTest = encTest,
            encIndexKey = encIndexKey,
            encBubblesKey = encBubblesKey,
            encItemEditionKey = encItemEditionKey,
            biometricCryptoMaterial = biometricCryptoMaterial,
        )
    }

    override suspend fun getAppVisit(): RoomAppVisit = CommonTestUtils.roomAppVisit()
    override suspend fun getDriveSettings(): GoogleDriveSettings = OSTestUtils.driveSettings()
    override suspend fun getAutoBackupError(): RoomMigration12to13.AutoBackupErrorMigration? = autoBackupError
    override suspend fun getFilesAndIcons(): List<File> = emptyList() // TODO <multisafe> Add some data for test

    override suspend fun onMigrationDone() {}

    override suspend fun getSafeSettings(): RoomMigration12to13.SafeSettingsMigration = CommonTestUtils.safeSettingsMigration()
}
