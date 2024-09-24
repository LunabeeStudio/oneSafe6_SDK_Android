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

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import studio.lunabee.onesafe.domain.model.safe.BiometricCryptoMaterial
import studio.lunabee.onesafe.domain.repository.BiometricCipherRepository
import studio.lunabee.onesafe.error.OSCryptoError
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

// TODO <multisafe> unit test stuff related to androidKeyStoreEngine

@Suppress("ObsoleteSdkInt")
@RequiresApi(Build.VERSION_CODES.M)
class BiometricEngine @Inject constructor(
    private val androidKeyStoreEngine: AndroidKeyStoreEngine,
) : BiometricCipherRepository {
    @Throws(OSCryptoError::class)
    override fun getCipherBiometricForDecrypt(iv: ByteArray): Cipher {
        val cipher = getCipher()
        val secretKey = getSecretKey()
        val ivSpec = IvParameterSpec(iv)
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        } catch (e: KeyPermanentlyInvalidatedException) {
            throw OSCryptoError(OSCryptoError.Code.BIOMETRIC_KEY_INVALIDATE, cause = e)
        }
        return cipher
    }

    override fun createCipherBiometricForEncrypt(): Cipher {
        val cipher = getCipher()
        val secretKey = generateKeyBiometric()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher
    }

    override fun clear() {
        androidKeyStoreEngine.removeSecretKey(KEY_ALIAS)
    }

    @Throws(OSCryptoError::class)
    fun encryptKey(key: ByteArray, cipher: Cipher): BiometricCryptoMaterial {
        val iv = cipher.iv
        val encKey = try {
            cipher.doFinal(key)
        } catch (e: IllegalStateException) {
            throw OSCryptoError(OSCryptoError.Code.IV_ALREADY_USED, cause = e)
        }
        return BiometricCryptoMaterial(iv, encKey)
    }

    @Throws(OSCryptoError::class)
    fun decryptKey(cryptoMaterial: BiometricCryptoMaterial, cipher: Cipher): ByteArray {
        return try {
            cipher.doFinal(cryptoMaterial.encKey)
        } catch (e: IllegalBlockSizeException) {
            throw OSCryptoError(OSCryptoError.Code.BIOMETRIC_DECRYPTION_NOT_AUTHENTICATED, cause = e)
        } catch (e: BadPaddingException) {
            throw OSCryptoError(OSCryptoError.Code.BIOMETRIC_DECRYPTION_FAIL, cause = e)
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
            throw OSCryptoError(OSCryptoError.Code.BIOMETRIC_KEY_NOT_GENERATED, cause = e)
        }
    }

    companion object {
        const val KEY_ALIAS: String = "2bebec3e-4029-469b-a054-725689254611"
    }
}
