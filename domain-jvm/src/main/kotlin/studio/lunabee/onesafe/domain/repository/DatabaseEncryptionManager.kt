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
 * Created by Lunabee Studio / Date - 3/20/2024 - for the oneSafe6 SDK.
 * Last modified 3/20/24, 11:11 AM
 */

package studio.lunabee.onesafe.domain.repository

import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.crypto.DatabaseKey
import studio.lunabee.onesafe.error.OSStorageError

/**
 * Manage whole database encryption
 */
interface DatabaseEncryptionManager {
    /**
     * Export a plain database to a temporary encrypted database. Run [finishMigrationIfNeeded] to finish the migration by replacing the
     * plain database by the new encrypted database before using it (i.e during app launch)
     */
    suspend fun migrateToEncrypted(key: DatabaseKey)

    /**
     * Export an encrypted database to a temporary plain database. Run [finishMigrationIfNeeded] to finish the migration by replacing the
     * encrypted database by the new plain database before using it (i.e during app launch)
     */
    suspend fun migrateToPlain(key: DatabaseKey)

    /**
     * Finish the export of the database from plain (or encrypted) to encrypted (or plain)
     *
     * @param key The current database key (after migrating)
     * @param oldKey The previous database key (before migrating)
     */
    suspend fun finishMigrationIfNeeded(key: DatabaseKey?, oldKey: DatabaseKey?): Flow<LBFlowResult<MigrationState>>

    /**
     * Try to open the main database with the provided [key].
     *
     * Throws OSStorageError with code [OSStorageError.Code.DATABASE_WRONG_KEY] if the provided [key] does not open the database.
     * Throws OSStorageError with code [OSStorageError.Code.DATABASE_NOT_FOUND] if the database does not exist.
     */
    fun checkDatabaseAccess(key: DatabaseKey?)

    /**
     * State of migration after calling [finishMigrationIfNeeded]
     */
    enum class MigrationState {
        /**
         * No migration requested
         */
        Noop,

        /**
         * Migration succeeded
         */
        Done,

        /**
         * Migration failed and database has been rollback to its previous state
         */
        Canceled,
    }

    /**
     * Check if the [error] is due to the database key missing
     */
    fun isMissingDatabaseKeyError(error: Throwable): Boolean
}
