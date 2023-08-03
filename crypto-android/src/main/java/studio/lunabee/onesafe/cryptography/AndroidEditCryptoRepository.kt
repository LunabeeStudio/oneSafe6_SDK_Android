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
import studio.lunabee.onesafe.domain.repository.EditCryptoRepository
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.randomize
import studio.lunabee.onesafe.use
import javax.crypto.Cipher
import javax.inject.Inject

class AndroidEditCryptoRepository @Inject constructor(
    private val saltProvider: SaltProvider,
    private val hashEngine: PasswordHashEngine,
    private val mainCryptoRepository: MainCryptoRepository,
) : EditCryptoRepository {
    private var salt: ByteArray? = null
    private var key: ByteArray? = null
    private var biometricCipher: Cipher? = null

    override suspend fun generateCryptographicData(password: CharArray) {
        password.use {
            val salt = saltProvider()
            this.salt = salt
            this.key = hashEngine.deriveKey(password, salt)
        }
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

    override suspend fun setMainCryptographicData(): Unit = persistMainCryptographicData(false)

    override suspend fun overrideMainCryptographicData(): Unit = persistMainCryptographicData(true)

    private suspend fun persistMainCryptographicData(allowOverride: Boolean = false) {
        try {
            val salt = this.salt ?: throw OSCryptoError(OSCryptoError.Code.ONBOARDING_SALT_NOT_LOADED)
            val key = this.key ?: throw OSCryptoError(OSCryptoError.Code.ONBOARDING_KEY_NOT_LOADED)
            if (allowOverride) {
                mainCryptoRepository.overrideMasterKeyAndSalt(key, salt)
            } else {
                mainCryptoRepository.storeMasterKeyAndSalt(key, salt)
            }
            biometricCipher?.let { mainCryptoRepository.enableBiometric(it) }
        } finally {
            this.key?.randomize()
            this.salt?.randomize()
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
}
