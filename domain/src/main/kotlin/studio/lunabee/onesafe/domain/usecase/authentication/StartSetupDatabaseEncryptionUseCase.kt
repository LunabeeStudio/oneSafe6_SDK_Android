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
 * Created by Lunabee Studio / Date - 3/19/2024 - for the oneSafe6 SDK.
 * Last modified 3/19/24, 5:25 PM
 */

package studio.lunabee.onesafe.domain.usecase.authentication

import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.flow.firstOrNull
import studio.lunabee.onesafe.domain.repository.DatabaseKeyRepository
import studio.lunabee.onesafe.domain.repository.SqlCipherManager
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.error.OSError.Companion.get
import javax.inject.Inject

/**
 * Initialize the activation or deactivation of the database encryption. Must be finish by calling [FinishSetupDatabaseEncryptionUseCase].
 * The key of the database will be stored in the encrypted datastore.
 *
 * @see SqlCipherManager
 */
class StartSetupDatabaseEncryptionUseCase @Inject constructor(
    private val databaseCryptoRepository: DatabaseKeyRepository,
    private val sqlCipherManager: SqlCipherManager,
) {
    /**
     * @see StartSetupDatabaseEncryptionUseCase
     */
    suspend operator fun invoke(enable: Boolean): LBResult<Unit> {
        return if (enable) startEnable() else startDisable()
    }

    private suspend fun startEnable() = OSError.runCatching(
        mapErr = { e ->
            if (e.code == OSCryptoError.Code.DATASTORE_ENTRY_KEY_ALREADY_EXIST) {
                OSDomainError.Code.DATABASE_ENCRYPTION_ALREADY_ENABLED.get(cause = e)
            } else {
                e
            }
        },
    ) {
        val createKey = databaseCryptoRepository.createKey()
        createKey.use { key ->
            try {
                sqlCipherManager.migrateToEncrypted(key)
            } catch (t: Throwable) {
                // safely remove the
                runCatching { databaseCryptoRepository.removeKey() }
                throw t
            }
        }
    }

    private suspend fun startDisable() = OSError.runCatching {
        val databaseKey = databaseCryptoRepository.getKeyFlow().firstOrNull()
            ?: throw OSDomainError.Code.DATABASE_ENCRYPTION_NOT_ENABLED.get()

        databaseKey.use { key ->
            sqlCipherManager.migrateToPlain(key)
        }
        databaseCryptoRepository.removeKey()
    }
}
