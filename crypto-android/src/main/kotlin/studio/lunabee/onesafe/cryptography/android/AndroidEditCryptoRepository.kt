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

package studio.lunabee.onesafe.cryptography.android

import studio.lunabee.onesafe.domain.model.crypto.NewSafeCrypto
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.repository.EditCryptoRepository
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.utils.SaltProvider
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.jvm.use
import studio.lunabee.onesafe.randomize
import studio.lunabee.onesafe.use
import javax.crypto.Cipher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidEditCryptoRepository @Inject constructor(
    private val saltProvider: SaltProvider,
    private val hashEngine: PasswordHashEngine,
    private val mainCryptoRepository: MainCryptoRepository,
    private val safeRepository: SafeRepository,
    private val cryptoEngine: CryptoEngine,
) : EditCryptoRepository {
    private var salt: ByteArray? = null
    private var key: ByteArray? = null
    private var biometricCipher: Cipher? = null

    override suspend fun generateCryptographicData(password: CharArray) {
        val salt = saltProvider()
        this.salt = salt
        this.key = hashEngine.deriveKey(password, salt)
    }

    override suspend fun checkCryptographicData(password: CharArray): Boolean {
        return password.use {
            val salt = this.salt ?: throw OSCryptoError(OSCryptoError.Code.ONBOARDING_SALT_NOT_LOADED)
            hashEngine.deriveKey(password, salt).use { checkKey ->
                checkKey.contentEquals(this.key)
            }
        }
    }

    override fun initializeBiometric(cipher: Cipher) {
        biometricCipher = cipher
    }

    override suspend fun setMainCryptographicData(): NewSafeCrypto = loadMainCryptographicData(null)

    override suspend fun overrideMainCryptographicData(safeId: SafeId): NewSafeCrypto = loadMainCryptographicData(safeId)

    private suspend fun loadMainCryptographicData(overrideSafeId: SafeId?): NewSafeCrypto {
        return try {
            val salt = this.salt ?: throw OSCryptoError(OSCryptoError.Code.ONBOARDING_SALT_NOT_LOADED)
            val key = this.key ?: throw OSCryptoError(OSCryptoError.Code.ONBOARDING_KEY_NOT_LOADED)

            if (overrideSafeId != null) {
                mainCryptoRepository.regenerateAndOverrideLoadedCrypto(key, salt, biometricCipher)
            } else {
                mainCryptoRepository.generateCrypto(key, salt, biometricCipher)
            }
        } finally {
            this.key?.randomize()
            reset()
        }
    }

    override fun reset() {
        salt = null
        key = null
        biometricCipher = null
    }

    override suspend fun reEncryptItemKeys(itemKeys: List<SafeItemKey>) {
        val key = this.key ?: throw OSCryptoError(OSCryptoError.Code.ONBOARDING_KEY_NOT_LOADED)
        itemKeys.forEach { itemKey ->
            mainCryptoRepository.reEncryptItemKey(itemKey, key)
        }
    }

    override suspend fun checkPasswordUniqueness(password: CharArray): Boolean {
        val safes = safeRepository.getAllSafeOrderByLastOpenAsc()
        return safes.none { safe ->
            hashEngine.deriveKey(password, safe.salt).use { key ->
                cryptoEngine.decrypt(
                    cipherData = safe.encTest,
                    key = key,
                    associatedData = null,
                ).getOrNull()?.decodeToString() == MainCryptoRepository.MASTER_KEY_TEST_VALUE
            }
        }
    }

    override suspend fun deriveAutoDestructionKey(password: CharArray, safeSalt: ByteArray): ByteArray {
        return hashEngine.deriveKey(password, safeSalt)
    }
}
