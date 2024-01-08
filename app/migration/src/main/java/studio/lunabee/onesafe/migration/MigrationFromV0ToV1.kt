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

package studio.lunabee.onesafe.migration

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import studio.lunabee.onesafe.cryptography.CryptoEngine
import studio.lunabee.onesafe.cryptography.DatastoreEngine
import studio.lunabee.onesafe.cryptography.qualifier.DataStoreType
import studio.lunabee.onesafe.cryptography.qualifier.DatastoreEngineProvider
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.error.OSMigrationError
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
    private val cryptoEngine: CryptoEngine,
    private val safeItemKeyDao: SafeItemKeyDao,
    private val indexWordEntryDao: IndexWordEntryDao,
) {
    suspend operator fun invoke(masterKey: ByteArray): LBResult<Unit> = OSError.runCatching(
        mapErr = {
            OSMigrationError(OSMigrationError.Code.USERNAME_REMOVAL_FAIL, cause = it)
        },
    ) {
        val username = dataStoreEngine.retrieveValue(DATASTORE_USERNAME).firstOrNull()
        if (username != null) {
            val encMasterKeyTest = dataStoreEngine.retrieveValue(DATASTORE_MASTER_KEY_TEST).firstOrNull()
                ?: throw OSCryptoError(OSCryptoError.Code.MASTER_KEY_NOT_GENERATED)

            val plainMasterKeyTest = try {
                cryptoEngine.decrypt(encMasterKeyTest, masterKey, username).decodeToString()
            } catch (e: Exception) {
                throw OSCryptoError(OSCryptoError.Code.MASTER_KEY_WRONG_PASSWORD, cause = e)
            }
            val isPasswordOk = plainMasterKeyTest == MASTER_KEY_TEST_VALUE

            if (isPasswordOk) {
                logger.i("Run migration from V0 to V1")

                val roomSafeItemKeys = safeItemKeyDao.getAllSafeItemKeys()
                roomSafeItemKeys.forEach { roomSafeItemKey ->
                    val rawKey = cryptoEngine.decrypt(roomSafeItemKey.encValue, masterKey, username)
                    val rawMigratedKey = cryptoEngine.encrypt(rawKey, masterKey, null)
                    rawMigratedKey.copyInto(roomSafeItemKey.encValue)
                }
                val migratedEncMasterKeyTest = cryptoEngine.encrypt(MASTER_KEY_TEST_VALUE.encodeToByteArray(), masterKey, null)

                val encIndexKey = dataStoreEngine.retrieveValue(DATASTORE_SEARCH_INDEX_KEY).firstOrNull()
                    ?: throw OSCryptoError(OSCryptoError.Code.MASTER_KEY_NOT_GENERATED)
                val indexKey = cryptoEngine.decrypt(encIndexKey, masterKey, username)
                val migratedEncIndexKey = cryptoEngine.encrypt(indexKey, masterKey, null)

                val roomIndexWorldEntries = indexWordEntryDao.getAll().first()
                indexKey.use {
                    roomIndexWorldEntries.forEach { roomIndexWordEntry ->
                        val rawIndexWordEntry = cryptoEngine.decrypt(roomIndexWordEntry.encWord, indexKey, username)
                        val rawMigratedIndexWordEntry = cryptoEngine.encrypt(rawIndexWordEntry, indexKey, null)
                        rawMigratedIndexWordEntry.copyInto(roomIndexWordEntry.encWord)
                    }
                }

                // Update safe item keys
                safeItemKeyDao.update(roomSafeItemKeys)
                // Update index word entries
                indexWordEntryDao.update(roomIndexWorldEntries)
                // Update key test value
                dataStoreEngine.editValue(migratedEncMasterKeyTest, DATASTORE_MASTER_KEY_TEST)
                // Update index key
                dataStoreEngine.editValue(migratedEncIndexKey, DATASTORE_SEARCH_INDEX_KEY)
                // Clear username
                dataStoreEngine.editValue(null, DATASTORE_USERNAME)
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
        private const val DATASTORE_USERNAME = "792bb428-b9bd-45bd-9a10-5d68de628b61"
        private const val DATASTORE_MASTER_KEY_TEST = "f9e3fa44-2f54-4246-8ba6-2784a18b63ea"
        private const val MASTER_KEY_TEST_VALUE = "44c5dac9-17ba-4690-9275-c7471b2e0582"
        private const val DATASTORE_SEARCH_INDEX_KEY = "f0ab7671-5314-41dc-9f57-3c689180ab33"
    }
}
