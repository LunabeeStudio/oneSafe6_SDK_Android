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
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:24 AM
 */

package studio.lunabee.onesafe.storage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.lunabee.lblogger.LBLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.domain.model.crypto.DatabaseKey
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.domain.repository.SqlCipherManager
import studio.lunabee.onesafe.use
import javax.inject.Inject
import net.zetetic.database.sqlcipher.SQLiteDatabase as SQLCipherDatabase

private val logger = LBLogger.get<DefaultSqlCipherManager>()

class DefaultSqlCipherManager @Inject constructor(
    @FileDispatcher private val dispatcher: CoroutineDispatcher,
    @ApplicationContext private val context: Context,
    private val databaseName: String,
) : SqlCipherManager {
    private val tempDbName: String = "$databaseName-cipher_export"

    override suspend fun migrateToEncrypted(key: DatabaseKey) {
        withContext(dispatcher) {
            val mainDbFile = context.getDatabasePath(databaseName)
            val plainDb = SQLCipherDatabase.openDatabase(
                mainDbFile.absolutePath,
                byteArrayOf(),
                null,
                openFlags,
                null,
                null,
            )
            plainDb.use {
                migrate(key, plainDb)
            }
        }
    }

    override suspend fun migrateToPlain(key: DatabaseKey) {
        withContext(dispatcher) {
            val mainDbFile = context.getDatabasePath(databaseName)
            val cipherDb = SQLCipherDatabase.openDatabase(
                mainDbFile.absolutePath,
                key.raw,
                null,
                openFlags,
                null,
            )
            cipherDb.use {
                migrate(null, cipherDb)
            }
        }
    }

    /**
     * @see <a href="https://tinyurl.com/jexrhbbn">ImportUnencryptedDatabaseTest.java</a>
     */
    private fun migrate(
        key: DatabaseKey?,
        database: SQLCipherDatabase,
    ) {
        logger.i("Start sqlcipher export")
        val tempMigratedDbFile = context.getDatabasePath(tempDbName)
        try {
            val newDatabase = "new_database"
            if (key == null) {
                database.rawExecSQL(
                    "ATTACH DATABASE '${tempMigratedDbFile.absolutePath}' AS $newDatabase KEY '';",
                )
            } else {
                key.asCharArray().use { charKey ->
                    val hexKey = "x'${charKey.joinToString("")}'"
                    logger.d("Migrate with KEY $hexKey")
                    database.rawExecSQL(
                        "ATTACH DATABASE '${tempMigratedDbFile.absolutePath}' AS $newDatabase KEY $hexKey;",
                    )
                }
            }
            database.rawExecSQL("SELECT sqlcipher_export('$newDatabase')")
            database.rawExecSQL("DETACH DATABASE $newDatabase")
            logger.i("Done sqlcipher export")

            // TODO <cipher> check that tempMigratedDbFile db is openable
        } catch (t: Throwable) {
            // Delete temp database file in case of error during export
            tempMigratedDbFile.delete()
            throw t
        }
    }

    override suspend fun finishMigrationIfNeeded() {
        val tempMigratedDbFile = context.getDatabasePath(tempDbName)
        if (tempMigratedDbFile.exists()) {
            // TODO <cipher> check that tempMigratedDbFile db is openable
            withContext(dispatcher) {
                val mainDbFile = context.getDatabasePath(databaseName)
                SQLiteDatabase.deleteDatabase(mainDbFile)
                tempMigratedDbFile.renameTo(mainDbFile)
            }
        }
    }

    companion object {
        private const val openFlags = SQLCipherDatabase.ENABLE_WRITE_AHEAD_LOGGING or // enable WAL like Room does
            SQLCipherDatabase.CREATE_IF_NECESSARY // allow export db creation
    }
}
