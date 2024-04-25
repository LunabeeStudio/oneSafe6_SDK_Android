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
 * Created by Lunabee Studio / Date - 4/8/2024 - for the oneSafe6 SDK.
 * Last modified 4/8/24, 11:40 AM
 */

package studio.lunabee.di

import studio.lunabee.onesafe.domain.model.crypto.DatabaseKey
import studio.lunabee.onesafe.domain.repository.DatabaseEncryptionManager
import studio.lunabee.onesafe.error.OSError.Companion.get
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.storage.SqlCipherDBManager

class InMemoryDatabaseEncryptionManager(
    private val sqlCipherDBManager: SqlCipherDBManager,
    private val dbName: String,
) : DatabaseEncryptionManager by sqlCipherDBManager {

    var throwOnMigrate: Boolean = false

    override fun checkDatabaseAccess(key: DatabaseKey?) {
        // Do not check database if name is blank (i.e in memory db)
        if (dbName.isNotBlank()) sqlCipherDBManager.checkDatabaseAccess(key)
    }

    override suspend fun migrateToEncrypted(key: DatabaseKey) {
        if (throwOnMigrate) {
            throw OSStorageError.Code.UNKNOWN_DATABASE_ERROR.get()
        }
    }

    override suspend fun migrateToPlain(key: DatabaseKey) {
        if (throwOnMigrate) {
            throw OSStorageError.Code.UNKNOWN_DATABASE_ERROR.get()
        }
    }
}
