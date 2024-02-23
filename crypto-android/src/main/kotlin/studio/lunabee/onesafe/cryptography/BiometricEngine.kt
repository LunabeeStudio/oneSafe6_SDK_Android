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

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.lunabee.onesafe.cryptography.qualifier.DataStoreType
import studio.lunabee.onesafe.cryptography.qualifier.DatastoreEngineProvider
import studio.lunabee.onesafe.cryptography.utils.dataStoreValueDelegate
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.use
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

@Suppress("ObsoleteSdkInt")
@RequiresApi(Build.VERSION_CODES.M)
class BiometricEngine @Inject constructor(
    private val androidKeyStoreEngine: AndroidKeyStoreEngine,
    @DatastoreEngineProvider(DataStoreType.Encrypted) private val dataStoreEngine: DatastoreEngine,
) {

    private var encryptedMasterKey: ByteArray? by dataStoreValueDelegate(
        key = ENC_MASTER_KEY_KEY,
        datastoreEngine = dataStoreEngine,
        errorCodeIfOverrideExistingValue = OSCryptoError.Code.BIOMETRIC_MASTER_KEY_ALREADY_GENERATED,
    )

    private var biometricIV: ByteArray? by dataStoreValueDelegate(
        key = PREF_IV,
        datastoreEngine = dataStoreEngine,
        errorCodeIfOverrideExistingValue = OSCryptoError.Code.BIOMETRIC_IV_ALREADY_GENERATED,
    )

    private fun getCipher(): Cipher {
        return Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7,
        )
    }

    @Throws(OSCryptoError::class)
    private fun getSecretKey(): SecretKey {
        try {
            return androidKeyStoreEngine.retrieveSecretKeyFromKeyStore(KEY_ALIAS)
        } catch (e: OSCryptoError) {
            throw OSCryptoError(OSCryptoError.Code.BIOMETRIC_KEY_NOT_GENERATED)
        }
    }

    @Throws(OSCryptoError::class)
    fun getCipherBiometricForDecrypt(): Cipher {
        val cipher = getCipher()
        val secretKey = getSecretKey()
        biometricIV?.use { iv ->
            val ivSpec = IvParameterSpec(iv)
            try {
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            } catch (e: KeyPermanentlyInvalidatedException) {
                throw OSCryptoError(OSCryptoError.Code.BIOMETRIC_KEY_INVALIDATE, cause = e)
            }
        }
        return cipher
    }

    fun createCipherBiometricForEncrypt(): Cipher {
        val cipher = getCipher()
        val secretKey = generateKeyBiometric()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        cipher.iv.use {
            biometricIV = it
        }
        return cipher
    }

    @Throws(OSCryptoError::class)
    fun storeKey(key: ByteArray, cipher: Cipher) {
        try {
            val encodedKey: ByteArray = cipher.doFinal(key)
            encryptedMasterKey = encodedKey
        } catch (e: IllegalStateException) {
            throw OSCryptoError(OSCryptoError.Code.IV_ALREADY_USED)
        }
    }

    @Throws(OSCryptoError::class)
    fun retrieveKey(cipher: Cipher): ByteArray {
        return try {
            encryptedMasterKey?.let { key ->
                cipher.doFinal(key)
            } ?: throw OSCryptoError(OSCryptoError.Code.BIOMETRIC_DECRYPTION_FAIL)
        } catch (e: IllegalBlockSizeException) {
            throw OSCryptoError(OSCryptoError.Code.BIOMETRIC_DECRYPTION_NOT_AUTHENTICATED, cause = e)
        }
    }

    private fun generateKeyBiometric(): SecretKey {
        try {
            return androidKeyStoreEngine.generateSecretKey(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setUserAuthenticationRequired(true)
                    .build(),
            )
        } catch (e: OSCryptoError) {
            throw OSCryptoError(OSCryptoError.Code.BIOMETRIC_KEY_GENERATION_ERROR, cause = e.cause)
        }
    }

    fun isBiometricEnabledFlow(): Flow<Boolean> = dataStoreEngine.retrieveValue(ENC_MASTER_KEY_KEY).map {
        it != null
    }

    fun disableBiometric() {
        encryptedMasterKey = null
        biometricIV = null
        androidKeyStoreEngine.removeSecretKey(KEY_ALIAS)
    }

    companion object {
        const val KEY_ALIAS: String = "2bebec3e-4029-469b-a054-725689254611"
        private const val PREF_IV = "56819b7d-e14a-4952-bb1d-5b8d5a06568a"
        private const val ENC_MASTER_KEY_KEY = "d548d24f-8ea4-4457-8698-63622cb91db9"
    }
}
