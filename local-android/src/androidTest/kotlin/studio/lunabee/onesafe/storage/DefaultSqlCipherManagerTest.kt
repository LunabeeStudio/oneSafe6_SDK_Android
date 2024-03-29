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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.junit.After
import org.junit.Test
import studio.lunabee.onesafe.domain.model.crypto.DatabaseKey
import kotlin.random.Random
import kotlin.test.assertEquals

class DefaultSqlCipherManagerTest {

    private var plainDbBuilder: RoomDatabase.Builder<MainDatabase>
    private var cipherDbBuilder: RoomDatabase.Builder<MainDatabase>
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val dbKey = DatabaseKey(Random.Default.nextBytes(32))

    private val dbName = "database_test"

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
    }

    @Test
    fun migrateToEncrypted_test(): TestResult = runTest {
        val plainDb: MainDatabase = plainDbBuilder.build()
        val expectedItem = OSStorageTestUtils.createRoomSafeItem(encName = byteArrayOf(1, 2, 3, 4))
        plainDb.safeItemDao().insert(expectedItem)

        val sqlCipherManager = DefaultSqlCipherManager(Dispatchers.IO, context, dbName)
        sqlCipherManager.migrateToEncrypted(dbKey)
        plainDb.close()
        sqlCipherManager.finishMigrationIfNeeded()

        val cipherDb = cipherDbBuilder.build()
        val actualItem = cipherDb.safeItemDao().getAllSafeItems().first()

        assertEquals(expectedItem, actualItem)
    }

    @Test
    fun migrateToPlain_test(): TestResult = runTest {
        val cipherDb: MainDatabase = cipherDbBuilder.build()
        val expectedItem = OSStorageTestUtils.createRoomSafeItem(encName = byteArrayOf(1, 2, 3, 4))
        cipherDb.safeItemDao().insert(expectedItem)

        val sqlCipherManager = DefaultSqlCipherManager(Dispatchers.IO, context, dbName)
        sqlCipherManager.migrateToPlain(dbKey)
        cipherDb.close()
        sqlCipherManager.finishMigrationIfNeeded()

        val plainDb = plainDbBuilder.build()
        val actualItem = plainDb.safeItemDao().getAllSafeItems().first()

        assertEquals(expectedItem, actualItem)
    }
}
