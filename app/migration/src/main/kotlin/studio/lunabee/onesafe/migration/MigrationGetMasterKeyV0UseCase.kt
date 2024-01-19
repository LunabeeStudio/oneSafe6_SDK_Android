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
import kotlinx.coroutines.flow.firstOrNull
import studio.lunabee.onesafe.cryptography.BiometricEngine
import studio.lunabee.onesafe.cryptography.DatastoreEngine
import studio.lunabee.onesafe.cryptography.PasswordHashEngine
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.use
import javax.crypto.Cipher

/**
 * Retrieve the master key manually in the V0 way
 */
internal class MigrationGetMasterKeyV0UseCase(
    private val biometricEngine: BiometricEngine,
    private val hashEngine: PasswordHashEngine,
    private val dataStoreEngine: DatastoreEngine,
) {
    suspend operator fun invoke(password: CharArray): LBResult<ByteArray> = OSError.runCatching {
        val salt = dataStoreEngine.retrieveValue(DATASTORE_MASTER_SALT).firstOrNull()
            ?: throw OSCryptoError(OSCryptoError.Code.MASTER_KEY_NOT_GENERATED)

        val masterKey = salt.use {
            hashEngine.deriveKey(password, salt)
        }

        masterKey
    }

    operator fun invoke(cipher: Cipher): LBResult<ByteArray> = OSError.runCatching {
        biometricEngine.retrieveKey(cipher)
    }

    companion object {
        private const val DATASTORE_MASTER_SALT = "b282a019-4337-45a3-8bf6-da657ad39a6c"
    }
}
