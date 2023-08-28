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

import androidx.core.database.getBlobOrNull
import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.storage.MainDatabase
import studio.lunabee.onesafe.storage.Migration3to4
import studio.lunabee.onesafe.test.OSTestUtils
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.toByteArray
import javax.inject.Inject
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@HiltAndroidTest
class MainDatabaseMigrationTest {
    private val dbName = "migration-test"

    @Inject lateinit var migration3to4: Migration3to4

    @get:Rule val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

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
    fun migrate3To4() {
        val blobs = List(8) { OSTestUtils.random.nextBytes(10) }
        val blobsString = blobs.map { "X'${it.joinToString("") { byte -> "%02x".format(byte) }}'" }

        helper.createDatabase(dbName, 3).apply {
            execSQL(
                "INSERT INTO Contact (id, enc_name, updated_at, shared_conversation_id, enc_is_using_deeplink) " +
                    "VALUES (${blobsString[0]}, ${blobsString[1]}, 0, ${blobsString[2]}, ${blobsString[3]})",
            )
            execSQL(
                "INSERT INTO Message (id, contact_id, enc_sent_at, enc_content, direction,`order`) " +
                    "VALUES (0, ${blobsString[0]}, ${blobsString[4]}, ${blobsString[5]}, 'SENT', 123)",
            )
            execSQL(
                "INSERT INTO Message (id, contact_id, enc_sent_at, enc_content, direction,`order`) " +
                    "VALUES (1, ${blobsString[0]}, ${blobsString[6]}, ${blobsString[7]}, 'FOO', 456)",
            )
            close()
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
}
