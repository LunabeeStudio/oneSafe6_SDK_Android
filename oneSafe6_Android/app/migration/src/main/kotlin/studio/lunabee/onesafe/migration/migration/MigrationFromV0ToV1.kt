/*
 * Copyright (c) 2023-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 7/9/2024 - for the oneSafe6 SDK.
 * Last modified 7/8/24, 5:01 PM
 */

package studio.lunabee.onesafe.migration.migration

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import studio.lunabee.onesafe.cryptography.android.DatastoreEngine
import studio.lunabee.onesafe.cryptography.android.qualifier.DataStoreType
import studio.lunabee.onesafe.cryptography.android.qualifier.DatastoreEngineProvider
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.error.OSMigrationError
import studio.lunabee.onesafe.migration.MigrationSafeData0
import studio.lunabee.onesafe.migration.utils.MigrationCryptoV0UseCase
import studio.lunabee.onesafe.migration.utils.MigrationCryptoV1UseCase
import studio.lunabee.onesafe.migration.utils.MigrationGetSafeIdBeforeV14UseCase
import studio.lunabee.onesafe.storage.dao.IndexWordEntryDao
import studio.lunabee.onesafe.storage.dao.SafeItemKeyDao
import studio.lunabee.onesafe.use
import javax.inject.Inject

private val logger = LBLogger.get<MigrationFromV0ToV1>()

/**
 * Username removal
 *   • Re-encrypt item keys
 *   • Re-encrypt index word entries
 *   • Re-encrypt the master key test string
 *   • Re-encrypt index key
 *   • Remove username from datastore
 */
class MigrationFromV0ToV1 @Inject constructor(
    @DatastoreEngineProvider(DataStoreType.Plain) private val dataStoreEngine: DatastoreEngine,
    private val migrationCryptoV0UseCase: MigrationCryptoV0UseCase,
    private val migrationCryptoV1UseCase: MigrationCryptoV1UseCase,
    private val safeItemKeyDao: SafeItemKeyDao,
    private val indexWordEntryDao: IndexWordEntryDao,
    private val migrationGetSafeIdBeforeV14UseCase: MigrationGetSafeIdBeforeV14UseCase,
) : AppMigration0(0, 1) {
    override suspend fun migrate(migrationSafeData: MigrationSafeData0): LBResult<Unit> = OSError.runCatching(
        mapErr = {
            OSMigrationError(OSMigrationError.Code.USERNAME_REMOVAL_FAIL, cause = it)
        },
    ) {
        val masterKey = migrationSafeData.masterKey
        val safeId = migrationGetSafeIdBeforeV14UseCase()
        val username = dataStoreEngine.retrieveValue(DatastoreUsername).firstOrNull()
        if (username != null) {
            val encMasterKeyTest = dataStoreEngine.retrieveValue(DatastoreMasterKeyTest).firstOrNull()
                ?: throw OSCryptoError(OSCryptoError.Code.MASTER_KEY_NOT_GENERATED)

            val plainMasterKeyTest = try {
                migrationCryptoV0UseCase.decrypt(encMasterKeyTest, masterKey, username).decodeToString()
            } catch (e: Exception) {
                throw OSCryptoError(OSCryptoError.Code.MASTER_KEY_WRONG_PASSWORD, cause = e)
            }
            val isPasswordOk = plainMasterKeyTest == MasterKeyTestValue

            if (isPasswordOk) {
                logger.i("Run migration from V0 to V1")

                val roomSafeItemKeys = safeItemKeyDao.getAllSafeItemKeys(safeId)
                roomSafeItemKeys.forEach { roomSafeItemKey ->
                    val rawKey = migrationCryptoV0UseCase.decrypt(roomSafeItemKey.encValue, masterKey, username)
                    val rawMigratedKey = migrationCryptoV1UseCase.encrypt(rawKey, masterKey)
                    rawMigratedKey.copyInto(roomSafeItemKey.encValue)
                }
                val migratedEncMasterKeyTest = migrationCryptoV1UseCase
                    .encrypt(MasterKeyTestValue.encodeToByteArray(), masterKey)

                val encIndexKey = dataStoreEngine.retrieveValue(DatastoreSearchIndexKey).firstOrNull()
                    ?: throw OSCryptoError(OSCryptoError.Code.MASTER_KEY_NOT_GENERATED)
                val indexKey = migrationCryptoV0UseCase.decrypt(encIndexKey, masterKey, username)
                val migratedEncIndexKey = migrationCryptoV1UseCase.encrypt(indexKey, masterKey)

                val roomIndexWorldEntries = indexWordEntryDao.getAll(safeId).first()
                indexKey.use {
                    roomIndexWorldEntries.forEach { roomIndexWordEntry ->
                        val rawIndexWordEntry = migrationCryptoV0UseCase
                            .decrypt(roomIndexWordEntry.encWord, indexKey, username)
                        val rawMigratedIndexWordEntry = migrationCryptoV1UseCase.encrypt(rawIndexWordEntry, indexKey)
                        rawMigratedIndexWordEntry.copyInto(roomIndexWordEntry.encWord)
                    }
                }

                // Update safe item keys
                safeItemKeyDao.update(roomSafeItemKeys)
                // Update index word entries
                indexWordEntryDao.update(roomIndexWorldEntries)
                // Update key test value
                dataStoreEngine.insertValue(DatastoreMasterKeyTest, migratedEncMasterKeyTest)
                // Update index key
                dataStoreEngine.insertValue(DatastoreSearchIndexKey, migratedEncIndexKey)
                // Clear username
                dataStoreEngine.removeValue(DatastoreUsername)
            } else {
                throw OSCryptoError(
                    OSCryptoError.Code.MASTER_KEY_WRONG_PASSWORD,
                    "Unable to load the current master key for migration with the provided credentials",
                )
            }
        }
    }

    companion object {
        // Copy of AndroidMainCryptoRepository to fix values as of v0
        private const val DatastoreUsername = "792bb428-b9bd-45bd-9a10-5d68de628b61"
        private const val DatastoreMasterKeyTest = "f9e3fa44-2f54-4246-8ba6-2784a18b63ea"
        private const val MasterKeyTestValue = "44c5dac9-17ba-4690-9275-c7471b2e0582"
        private const val DatastoreSearchIndexKey = "f0ab7671-5314-41dc-9f57-3c689180ab33"
    }
}
