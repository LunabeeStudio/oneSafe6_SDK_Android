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

import studio.lunabee.onesafe.domain.model.crypto.DatabaseKey

/**
 * Manage interaction with SQLCipher library
 */
interface SqlCipherManager {
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
     */
    suspend fun finishMigrationIfNeeded()
}
