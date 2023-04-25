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

package studio.lunabee.onesafe.cryptography

import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.importexport.ImportExportCryptoRepository
import studio.lunabee.onesafe.use
import java.security.GeneralSecurityException
import java.util.UUID
import javax.inject.Inject

class AndroidImportExportCryptoRepository @Inject constructor(
    private val crypto: CryptoEngine,
    private val hashEngine: HashEngine,
    private val saltProvider: SaltProvider,
) : ImportExportCryptoRepository {

    override suspend fun deriveKey(password: CharArray, salt: ByteArray): ByteArray {
        return hashEngine.deriveKey(
            password = password,
            salt = salt,
        )
    }

    override suspend fun createMasterKeyAndSalt(password: CharArray): Pair<ByteArray, ByteArray> {
        return password.use {
            val salt = saltProvider()
            val masterKey = hashEngine.deriveKey(password, salt)
            masterKey to salt
        }
    }

    override suspend fun decryptRawItemKey(cipherData: ByteArray, key: ByteArray): ByteArray {
        return try {
            crypto.decrypt(cipherData = cipherData, key, null)
        } catch (e: GeneralSecurityException) {
            throw OSCryptoError(OSCryptoError.Code.DECRYPTION_FAILED_WRONG_KEY, cause = e)
        }
    }

    override suspend fun decrypt(cipherData: ByteArray, key: ByteArray): ByteArray {
        return try {
            crypto.decrypt(cipherData = cipherData, key, null)
        } catch (e: GeneralSecurityException) {
            throw OSCryptoError(OSCryptoError.Code.DECRYPTION_FAILED_WRONG_KEY, cause = e)
        }
    }

    override suspend fun createSafeItemKeyFromRaw(
        itemId: UUID,
        rawItemKey: ByteArray,
        cryptoKey: ByteArray,
    ): SafeItemKey {
        val itemKey = crypto.encrypt(rawItemKey, cryptoKey, null)
        return SafeItemKey(itemId, itemKey)
    }
}
