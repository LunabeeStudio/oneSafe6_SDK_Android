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
 * Created by Lunabee Studio / Date - 3/15/2024 - for the oneSafe6 SDK.
 * Last modified 3/15/24, 3:43 PM
 */

package studio.lunabee.onesafe.storage

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.platform.app.InstrumentationRegistry
import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.junit.After
import org.junit.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import studio.lunabee.onesafe.domain.model.crypto.DatabaseKey
import studio.lunabee.onesafe.domain.repository.DatabaseEncryptionManager
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.assertSuccess
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SqlCipherDBManagerTest {

    private var plainDbBuilder: RoomDatabase.Builder<MainDatabase>
    private var cipherDbBuilder: RoomDatabase.Builder<MainDatabase>
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val dbKey = DatabaseKey(Random.Default.nextBytes(32))

    private val dbName = "database_test"
    private val tempCipherDbName = "database_cipher_temp_test"

    init {
        System.loadLibrary("sqlcipher")

        plainDbBuilder = Room.databaseBuilder(
            context,
            MainDatabase::class.java,
            dbName,
        )
            .openHelperFactory(SupportOpenHelperFactory(null))

        cipherDbBuilder = Room.databaseBuilder(
            context,
            MainDatabase::class.java,
            dbName,
        )
            .openHelperFactory(SupportOpenHelperFactory(dbKey.raw))
    }

    @After
    fun tearsDown() {
        context.getDatabasePath(dbName).delete()
        context.getDatabasePath(tempCipherDbName).delete()
    }

    @Test
    fun migrateToEncrypted_test(): TestResult = runTest {
        val plainDb: MainDatabase = plainDbBuilder.build()
        val expectedItem = OSStorageTestUtils.createRoomSafeItem(encName = byteArrayOf(1, 2, 3, 4))
        plainDb.safeItemDao().insert(expectedItem)

        val manager = SqlCipherDBManager(Dispatchers.IO, context, dbName, tempCipherDbName)
        manager.migrateToEncrypted(dbKey)
        plainDb.close()

        val results = manager.finishMigrationIfNeeded(dbKey, null).toList()
        assertIs<LBFlowResult.Loading<DatabaseEncryptionManager.MigrationState>>(results[0])
        val data = assertSuccess(results[1]).successData
        assertEquals(DatabaseEncryptionManager.MigrationState.Done, data)

        val cipherDb = cipherDbBuilder.build()
        val actualItem = cipherDb.safeItemDao().getAllSafeItems().first()

        assertEquals(expectedItem, actualItem)
    }

    @Test
    fun migrateToPlain_test(): TestResult = runTest {
        val cipherDb: MainDatabase = cipherDbBuilder.build()
        val expectedItem = OSStorageTestUtils.createRoomSafeItem(encName = byteArrayOf(1, 2, 3, 4))
        cipherDb.safeItemDao().insert(expectedItem)

        val manager = SqlCipherDBManager(Dispatchers.IO, context, dbName, tempCipherDbName)
        manager.migrateToPlain(dbKey)
        cipherDb.close()

        val results = manager.finishMigrationIfNeeded(null, dbKey).toList()
        assertIs<LBFlowResult.Loading<DatabaseEncryptionManager.MigrationState>>(results[0])
        val data = assertSuccess(results[1]).successData
        assertEquals(DatabaseEncryptionManager.MigrationState.Done, data)

        val plainDb = plainDbBuilder.build()
        val actualItem = plainDb.safeItemDao().getAllSafeItems().first()

        assertEquals(expectedItem, actualItem)
    }

    @Test
    fun migrateToPlain_rollback_test(): TestResult = runTest {
        val cipherDb: MainDatabase = cipherDbBuilder.build()
        val expectedItem = OSStorageTestUtils.createRoomSafeItem(encName = byteArrayOf(1, 2, 3, 4))
        cipherDb.safeItemDao().insert(expectedItem)

        val manager = SqlCipherDBManager(Dispatchers.IO, context, dbName, tempCipherDbName)
        manager.migrateToPlain(dbKey)
        cipherDb.close()

        // corrupt temp db
        val tempDbFile = context.getDatabasePath(tempCipherDbName)
        val dbBytes = tempDbFile.readBytes() // size = 167_936
        val randomBytes = OSTestConfig.random.nextBytes(10 * 1024)
        randomBytes.copyInto(dbBytes, 100_000)
        tempDbFile.writeBytes(dbBytes)

        val results = manager.finishMigrationIfNeeded(null, dbKey).toList()
        assertIs<LBFlowResult.Loading<DatabaseEncryptionManager.MigrationState>>(results[0])
        val data = assertSuccess(results[1]).successData
        assertEquals(DatabaseEncryptionManager.MigrationState.Canceled, data)

        val tempDbFiles = tempDbFile.parentFile?.listFiles { _, file ->
            file.startsWith(tempDbFile.name)
        }.orEmpty()
        assertTrue(tempDbFiles.isEmpty())

        val actualItem = cipherDb.safeItemDao().getAllSafeItems().first()
        assertEquals(expectedItem, actualItem)
    }

    /**
     * Check DB access with wrong and good key (plain and encrypted)
     */
    @Test
    fun checkDatabaseAccess_test(): TestResult = runTest {
        val manager = SqlCipherDBManager(Dispatchers.IO, context, dbName, tempCipherDbName)

        cipherDbBuilder.build().openHelper.writableDatabase // create cipher DB

        listOf(
            { manager.checkDatabaseAccess(null) }, // empty key
            { manager.checkDatabaseAccess(DatabaseKey(Random.Default.nextBytes(32))) }, // bad key
        ).forEach { check ->
            val errCode = assertThrows<OSStorageError>(check).code
            assertEquals(OSStorageError.Code.DATABASE_WRONG_KEY, errCode)
        }

        assertDoesNotThrow { manager.checkDatabaseAccess(dbKey) } // good key

        context.getDatabasePath(dbName).delete() // delete cipher DB
        plainDbBuilder.build().openHelper.writableDatabase // create plain DB

        val errCode = assertThrows<OSStorageError> {
            manager.checkDatabaseAccess(dbKey)
        }.code
        assertEquals(OSStorageError.Code.DATABASE_WRONG_KEY, errCode)

        assertDoesNotThrow { manager.checkDatabaseAccess(null) } // good key
    }

    /**
     * Check DB access without creating the DB
     */
    @Test
    fun checkDatabaseAccess_no_db_test(): TestResult = runTest {
        val manager = SqlCipherDBManager(Dispatchers.IO, context, dbName, tempCipherDbName)

        listOf(
            { manager.checkDatabaseAccess(null) },
            { manager.checkDatabaseAccess(dbKey) },
        ).forEach { check ->
            val errCode = assertThrows<OSStorageError>(check).code
            assertEquals(OSStorageError.Code.DATABASE_NOT_FOUND, errCode)
        }
    }
}
