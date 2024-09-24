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
import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabaseCorruptException
import android.database.sqlite.SQLiteException
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lblogger.LBLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.domain.model.crypto.DatabaseKey
import studio.lunabee.onesafe.domain.qualifier.DatabaseName
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.domain.repository.DatabaseEncryptionManager
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.jvm.use
import java.io.File
import javax.inject.Inject
import net.zetetic.database.sqlcipher.SQLiteDatabase as SQLCipherDatabase

private val logger = LBLogger.get<SqlCipherDBManager>()

/**
 * Handle encryption of a SQLite database by using SQLCipher library
 *
 * @see <a href=https://www.notion.so/lunabeestudio/SQLCipher-over-encryption-7854bb3449f64ba8ad76d71a08847a04>Notion</a>
 */
class SqlCipherDBManager @Inject constructor(
    @FileDispatcher private val dispatcher: CoroutineDispatcher,
    @ApplicationContext private val context: Context,
    @DatabaseName(DatabaseName.Type.Main) private val databaseName: String,
    @DatabaseName(DatabaseName.Type.CipherTemp) private val tempDbName: String,
) : DatabaseEncryptionManager {

    init {
        System.loadLibrary(sqlCipherLibrary)
        logger.i("SQL Cipher loaded")
    }

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
                    database.rawExecSQL(
                        "ATTACH DATABASE '${tempMigratedDbFile.absolutePath}' AS $newDatabase KEY $hexKey;",
                    )
                }
            }
            database.rawExecSQL("SELECT sqlcipher_export('$newDatabase')")
            database.rawExecSQL("DETACH DATABASE $newDatabase")

            checkDatabaseAccess(key, tempMigratedDbFile)

            logger.i("Done sqlcipher export")
        } catch (t: Throwable) {
            // Delete temp database file in case of error during export
            SQLiteDatabase.deleteDatabase(tempMigratedDbFile)
            throw t
        }
    }

    override suspend fun finishMigrationIfNeeded(
        key: DatabaseKey?,
        oldKey: DatabaseKey?,
    ): Flow<LBFlowResult<DatabaseEncryptionManager.MigrationState>> = flow {
        val tempMigratedDbFile = context.getDatabasePath(tempDbName)
        if (tempMigratedDbFile.exists()) {
            val mainDbFile = context.getDatabasePath(databaseName)
            emit(LBFlowResult.Loading())

            try {
                // Check temp db is valid
                checkDatabaseAccess(key, tempMigratedDbFile)
            } catch (e: Throwable) {
                SQLiteDatabase.deleteDatabase(tempMigratedDbFile)
                emit(LBFlowResult.Success(DatabaseEncryptionManager.MigrationState.Canceled))
                return@flow
            }

            safeReplaceDatabase(mainDbFile, tempMigratedDbFile, key, oldKey)
        } else {
            emit(LBFlowResult.Success(DatabaseEncryptionManager.MigrationState.Noop))
        }
    }.flowOn(dispatcher)

    private suspend fun FlowCollector<LBFlowResult<DatabaseEncryptionManager.MigrationState>>.safeReplaceDatabase(
        dbFile: File,
        tempDbFile: File,
        key: DatabaseKey?,
        oldKey: DatabaseKey?,
    ) {
        backupDatabase(dbFile, oldKey)
        SQLCipherDatabase.deleteDatabase(dbFile)
        tempDbFile.renameTo(dbFile)

        try {
            // Check final db is valid
            checkDatabaseAccess(key, dbFile)
            emit(LBFlowResult.Success(DatabaseEncryptionManager.MigrationState.Done))
        } catch (e: Throwable) {
            restoreDatabaseBackup(dbFile)
            checkDatabaseAccess(oldKey, dbFile)
            emit(LBFlowResult.Success(DatabaseEncryptionManager.MigrationState.Canceled))
        } finally {
            deleteDatabaseBackup(dbFile)
        }
    }

    private fun backupDatabase(dbFile: File, key: DatabaseKey?) {
        val backupFile = File(dbFile.absolutePath + ".backup")
        dbFile.takeIf { it.exists() }?.renameTo(backupFile)
        File(dbFile.absolutePath + "-shm").takeIf { it.exists() }?.let {
            it.renameTo(File(it.absolutePath.replace("-shm", ".backup-shm")))
        }
        File(dbFile.absolutePath + "-wal").takeIf { it.exists() }?.let {
            it.renameTo(File(it.absolutePath.replace("-wal", ".backup-wal")))
        }

        checkDatabaseAccess(key, backupFile)
    }

    private fun deleteDatabaseBackup(dbFile: File) {
        val parentFile = dbFile.parentFile
            ?: throw OSStorageError.Code.DATABASE_CANNOT_ACCESS_DIR.get()
        val backupFiles = parentFile.listFiles { _, name ->
            name.startsWith(dbFile.nameWithoutExtension) && name.endsWith(backupSuffix)
        } ?: throw OSStorageError.Code.DATABASE_CANNOT_ACCESS_FILES.get()

        backupFiles.forEach { it.delete() }
    }

    private fun restoreDatabaseBackup(dbFile: File) {
        val parentFile = dbFile.parentFile
            ?: throw OSStorageError.Code.DATABASE_CANNOT_ACCESS_DIR.get()
        val databaseFiles = parentFile.listFiles { _, name ->
            name.startsWith(dbFile.nameWithoutExtension) && !name.endsWith(backupSuffix)
        } ?: throw OSStorageError.Code.DATABASE_CANNOT_ACCESS_FILES.get()
        val backupFiles = parentFile.listFiles { _, name ->
            name.startsWith(dbFile.nameWithoutExtension) && name.endsWith(backupSuffix)
        } ?: throw OSStorageError.Code.DATABASE_CANNOT_ACCESS_FILES.get()

        databaseFiles.forEach { file -> file.delete() }
        backupFiles.forEach { file -> file.renameTo(File(file.name.removeSuffix(backupSuffix))) }
    }

    override fun checkDatabaseAccess(key: DatabaseKey?) {
        checkDatabaseAccess(key, context.getDatabasePath(databaseName))
    }

    private fun checkDatabaseAccess(key: DatabaseKey?, dbFile: File) {
        var error: Throwable? = null
        try {
            SQLCipherDatabase.openDatabase(
                dbFile.absolutePath,
                key?.raw,
                null,
                SQLCipherDatabase.ENABLE_WRITE_AHEAD_LOGGING,
                null,
                null,
            ).use { database ->
                // https://www.sqlite.org/pragma.html#pragma_integrity_check
                val checkCursor = database.query("PRAGMA integrity_check")
                checkCursor.moveToFirst()
                if (checkCursor.getString(0) != "ok") {
                    error = OSStorageError.Code.DATABASE_CORRUPTED.get(message = "integrity_check failed")
                }
            }
        } catch (e: SQLiteCantOpenDatabaseException) {
            error = OSStorageError.Code.DATABASE_NOT_FOUND.get(cause = e)
        } catch (e: SQLiteDatabaseCorruptException) {
            error = OSStorageError.Code.DATABASE_CORRUPTED.get(cause = e)
        } catch (e: SQLiteException) {
            error = if (isMissingDatabaseKeyError(e)) {
                OSStorageError.Code.DATABASE_WRONG_KEY.get(cause = e)
            } else {
                e
            }
        }
        error?.let { throw it }
    }

    override fun isMissingDatabaseKeyError(error: Throwable): Boolean =
        error is SQLiteException && error.message?.contains("code 26") == true

    companion object {
        private const val sqlCipherLibrary: String = "sqlcipher"
        private const val backupSuffix: String = ".backup"
        private const val openFlags = SQLCipherDatabase.ENABLE_WRITE_AHEAD_LOGGING or // enable WAL like Room does
            SQLCipherDatabase.CREATE_IF_NECESSARY // allow export db creation
    }
}
