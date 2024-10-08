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
 * Created by Lunabee Studio / Date - 9/23/2024 - for the oneSafe6 SDK.
 * Last modified 23/09/2024 09:14
 */

package studio.lunabee.onesafe.cryptography.android

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.cryptography.android.extension.use
import studio.lunabee.onesafe.domain.qualifier.CryptoDispatcher
import studio.lunabee.onesafe.domain.repository.WorkerCryptoRepository
import studio.lunabee.onesafe.error.OSCryptoError
import java.io.ByteArrayOutputStream
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

@Suppress("ObsoleteSdkInt")
@RequiresApi(Build.VERSION_CODES.M)
class AndroidWorkerCryptoRepository @Inject constructor(
    private val androidKeyStoreEngine: AndroidKeyStoreEngine,
    @CryptoDispatcher private val cryptoDispatcher: CoroutineDispatcher,
    private val ivProvider: IVProvider,
) : WorkerCryptoRepository {

    private fun getCipher() = Cipher.getInstance(TRANSFORMATION)
    private fun getGcmParameterSpec(iv: ByteArray) = GCMParameterSpec(AES_GCM_TAG_LENGTH_IN_BITS, iv)

    private fun getKeyGenParameterSpec(): KeyGenParameterSpec {
        val builder = KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
        builder.setBlockModes(BLOCK_MODE)
        builder.setEncryptionPaddings(PADDING)
        builder.setUserAuthenticationRequired(false)
        builder.setRandomizedEncryptionRequired(false)
        return builder.build()
    }

    private fun getKey() = androidKeyStoreEngine.retrieveOrGenerateSecretKey(KEY_ALIAS, getKeyGenParameterSpec())

    override suspend fun decrypt(data: ByteArray): ByteArray {
        try {
            return withContext(cryptoDispatcher) {
                getKey().use { key ->
                    val cipher = getCipher()
                    val iv: ByteArray = data.copyOfRange(0, AES_GCM_IV_LENGTH)
                    val ivSpec = getGcmParameterSpec(iv)
                    cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
                    cipher.doFinal(
                        data,
                        AES_GCM_IV_LENGTH,
                        data.size - AES_GCM_IV_LENGTH,
                    )
                }
            }
        } catch (e: Exception) {
            when (e) {
                is KeyPermanentlyInvalidatedException,
                is AEADBadTagException,
                -> throw OSCryptoError(OSCryptoError.Code.ANDROID_KEYSTORE_KEY_PERMANENTLY_INVALIDATE, cause = e)
                is IndexOutOfBoundsException -> throw OSCryptoError(OSCryptoError.Code.DECRYPTION_UNKNOWN_FAILURE, cause = e)
                else -> throw e
            }
        }
    }

    override suspend fun encrypt(data: ByteArray): ByteArray {
        return withContext(cryptoDispatcher) {
            getKey().use { key ->
                val bos = ByteArrayOutputStream()
                val cipher = getCipher()
                val iv = ivProvider(AES_GCM_IV_LENGTH)
                cipher.init(Cipher.ENCRYPT_MODE, key, getGcmParameterSpec(iv))
                bos.write(iv)
                CipherOutputStream(bos, cipher).use { cos ->
                    data.inputStream().use { input ->
                        input.copyTo(cos)
                    }
                }
                bos.toByteArray()
            }
        }
    }

    companion object {
        private const val KEY_ALIAS: String = "7882c556-7589-47a1-b6db-8fd51704db7d"

        private const val AES_GCM_IV_LENGTH = 12
        private const val AES_GCM_TAG_LENGTH_IN_BITS = 128
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }
}
