/*
 * Copyright (c) 2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 8/23/2023 - for the oneSafe6 SDK.
 * Last modified 8/23/23, 10:48 AM
 */

package studio.lunabee.onesafe.storage.database

import android.database.Cursor
import androidx.core.database.getBlobOrNull
import androidx.core.database.getStringOrNull
import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import studio.lunabee.onesafe.storage.MainDatabase
import studio.lunabee.onesafe.storage.migration.RoomMigration3to4
import studio.lunabee.onesafe.storage.migration.RoomMigration8to9
import studio.lunabee.onesafe.storage.migration.RoomMigration9to10
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.toByteArray
import javax.inject.Inject
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@HiltAndroidTest
class MainDatabaseMigrationTest {
    private val dbName = "migration-test"

    @Inject lateinit var migration3to4: RoomMigration3to4

    @Inject lateinit var migration8to9: RoomMigration8to9

    @Inject lateinit var migration9to10: RoomMigration9to10

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

    @Test
    fun migrate3To4_test() {
        val blobs = List(8) { OSTestConfig.random.nextBytes(10) }
        val blobsString = blobs.map { "X'${it.joinToString("") { byte -> "%02x".format(byte) }}'" }

        helper.createDatabase(dbName, 3).use { db ->
            db.execSQL(
                "INSERT INTO Contact (id, enc_name, updated_at, shared_conversation_id, enc_is_using_deeplink) " +
                    "VALUES (${blobsString[0]}, ${blobsString[1]}, 0, ${blobsString[2]}, ${blobsString[3]})",
            )
            db.execSQL(
                "INSERT INTO Message (id, contact_id, enc_sent_at, enc_content, direction,`order`) " +
                    "VALUES (0, ${blobsString[0]}, ${blobsString[4]}, ${blobsString[5]}, 'SENT', 123)",
            )
            db.execSQL(
                "INSERT INTO Message (id, contact_id, enc_sent_at, enc_content, direction,`order`) " +
                    "VALUES (1, ${blobsString[0]}, ${blobsString[6]}, ${blobsString[7]}, 'FOO', 456)",
            )
        }

        val db = helper.runMigrationsAndValidate(dbName, 4, true, migration3to4)

        db.query("SELECT * FROM Message").apply {
            assertEquals(2, count)
            moveToFirst()
            assertContentEquals(testUUIDs[0].toByteArray(), getBlob(0))
            assertContentEquals(blobs[0], getBlob(1))
            assertContentEquals(blobs[4], getBlob(2))
            assertContentEquals(blobs[5], getBlob(3))
            assertEquals("SENT", getString(4))
            assertEquals(123f, getFloat(5))
            assertEquals(null, getBlobOrNull(6))

            moveToNext()
            assertContentEquals(testUUIDs[1].toByteArray(), getBlob(0))
            assertContentEquals(blobs[0], getBlob(1))
            assertContentEquals(blobs[6], getBlob(2))
            assertContentEquals(blobs[7], getBlob(3))
            assertEquals("FOO", getString(4))
            assertEquals(456f, getFloat(5))
            assertEquals(null, getBlobOrNull(6))
        }
    }

    @Test
    fun migration8to9_test() {
        val ids = testUUIDs.subList(0, 9).map { it.toByteArray() }
        val idWithUpdatedAt = ids.map {
            it.toSqlBlobString() to OSTestConfig.random.nextLong()
        }

        val valueRows = idWithUpdatedAt.joinToString(",") { (id, updatedAt) ->
            "($id, NULL, NULL, false, $updatedAt, 0.0, NULL, NULL, NULL, NULL, NULL, 0)"
        }

        helper.createDatabase(dbName, 8).use { db ->
            db.execSQL("INSERT INTO SafeItem VALUES $valueRows;")
        }

        val db = helper.runMigrationsAndValidate(dbName, 9, true, migration8to9)

        val cursor = db.query("SELECT * FROM SafeItem")
        while (cursor.moveToNext()) {
            val actualCreatedAt = cursor.getLong(4)
            assertEquals(idWithUpdatedAt[cursor.position].second, actualCreatedAt)
        }
    }

    /**
     * Test existing recursive item migration (on parent_id and deleted_parent_id)
     * Test triggers added
     */
    @Test
    fun migration9to10_test() {
        val id0 = testUUIDs[0].toByteArray().toSqlBlobString()
        val id1 = testUUIDs[1].toByteArray().toSqlBlobString()
        val id2 = testUUIDs[2].toByteArray().toSqlBlobString()

        helper.createDatabase(dbName, 9).use { db ->
            db.execSQL("INSERT INTO SafeItem VALUES ($id0, NULL, $id0, false, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, 0)")
            db.execSQL("INSERT INTO SafeItem VALUES ($id1, NULL, NULL, false, 0, 0, 0, NULL, NULL, NULL, $id1, NULL, 0)")
            db.execSQL("INSERT INTO SafeItem VALUES ($id2, NULL, $id2, false, 0, 0, 0, NULL, NULL, NULL, $id2, NULL, 0)")
        }

        val db = helper.runMigrationsAndValidate(dbName, 10, true, migration9to10)

        val cursor = db.query("SELECT parent_id, deleted_parent_id FROM SafeItem")
        while (cursor.moveToNext()) {
            val parentId = cursor.getBlob(0)
            val deletedParentId = cursor.getBlob(1)
            assertNull(parentId)
            assertNull(deletedParentId)
        }

        val triggersCursor = db.query("SELECT * FROM sqlite_master WHERE type = 'trigger'")
        triggersCursor.moveToFirst()
        val trigger0 = triggersCursor.getString(triggersCursor.getColumnIndex("name"))
        triggersCursor.moveToNext()
        val trigger1 = triggersCursor.getString(triggersCursor.getColumnIndex("name"))

        assertTrue(triggersCursor.isLast)
        assertEquals("recursive_item_insert", trigger0)
        assertEquals("recursive_item_update", trigger1)
    }

    private fun ByteArray.toSqlBlobString() = "X'${joinToString(separator = "") { byte -> "%02x".format(byte) }}'"
}

internal fun Cursor.getBlob(columnName: String): ByteArray {
    return getBlob(getColumnIndex(columnName))
}

internal fun Cursor.getString(columnName: String): String? {
    return getStringOrNull(getColumnIndex(columnName))
}
