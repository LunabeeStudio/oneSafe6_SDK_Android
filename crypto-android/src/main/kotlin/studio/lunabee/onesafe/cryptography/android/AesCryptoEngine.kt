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
 * Created by Lunabee Studio / Date - 9/4/2024 - for the oneSafe6 SDK.
 * Last modified 9/4/24, 10:39 AM
 */

package studio.lunabee.onesafe.cryptography.android

import androidx.core.util.AtomicFile
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.cryptography.android.utils.SelfDestroyCipherInputStream
import studio.lunabee.onesafe.error.OSCryptoError
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.UnrecoverableKeyException
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.security.cert.CertificateException

private val logger = LBLogger.get<AesCryptoEngine>()

/**
 * CryptoEngine implementation of AES-GCM using Android embedded provider
 * Actual decrypt/encrypt code is extracted to non suspend fun for benchmarking purpose.
 *
 * @param ivProvider The Initialization Vector provider (formally Nonce provider here) to be used when encrypting data
 */
class AesCryptoEngine @Inject constructor(
    private val ivProvider: IVProvider,
) : CryptoEngine {

    init {
        val cipher = getCipher()
        logger.i("Initialize ${javaClass.simpleName} using ${cipher.provider}")
    }

    override fun encrypt(plainData: ByteArray, key: ByteArray, associatedData: ByteArray?): Result<ByteArray> {
        return runCatching {
            val bos = ByteArrayOutputStream()
            getCipherOutputStream(bos, key, null).use { cos ->
                plainData.inputStream().use { input ->
                    input.copyTo(cos)
                }
            }
            bos.toByteArray()
        }
    }

    override fun decrypt(cipherData: ByteArray, key: ByteArray, associatedData: ByteArray?): Result<ByteArray> {
        return runCatching {
            val cipher = getCipher()
            val iv: ByteArray = try {
                cipherData.copyOfRange(0, AES_GCM_IV_LENGTH)
            } catch (e: IndexOutOfBoundsException) {
                throw OSCryptoError(OSCryptoError.Code.DECRYPTION_UNKNOWN_FAILURE, cause = e)
            }
            val ivSpec = getGcmParameterSpec(iv)
            val secretKey = getSecretKeySpec(key)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            cipher.doFinal(
                cipherData,
                AES_GCM_IV_LENGTH,
                cipherData.size - AES_GCM_IV_LENGTH,
            )
        }
    }

    override fun decrypt(cipherFile: AtomicFile, key: ByteArray, associatedData: ByteArray?): Result<ByteArray> {
        return runCatching {
            val secretKey = getSecretKeySpec(key)

            val fis = cipherFile.openRead()
            val cis = getCipherInputStream(fis, secretKey)
            val bos = ByteArrayOutputStream()

            bos.use { output ->
                cis.use { cis ->
                    cis.copyTo(output, STREAM_BUFFER_SIZE)
                }
            }

            bos.toByteArray()
        }
    }

    override fun getEncryptStream(file: File, key: ByteArray, associatedData: ByteArray?): OutputStream {
        val fos = file.outputStream()
        return getCipherOutputStream(fos, key, associatedData)
    }

    override fun getDecryptStream(cipherFile: AtomicFile, key: ByteArray, associatedData: ByteArray?): InputStream {
        val secretKey = getSecretKeySpec(key)
        val fis = cipherFile.openRead()
        return getCipherInputStream(fis, secretKey)
    }

    /**
     * Create a CipherInputStream instance.
     *
     * @param inputStream the input stream
     * @return the created InputStream
     */
    @Throws(
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        CertificateException::class,
        InvalidKeyException::class,
        KeyStoreException::class,
        UnrecoverableKeyException::class,
        IllegalBlockSizeException::class,
        NoSuchProviderException::class,
        InvalidAlgorithmParameterException::class,
        IOException::class,
    )
    private fun getCipherInputStream(inputStream: InputStream, secretKey: SecretKeySpec): InputStream {
        val iv = ByteArray(AES_GCM_IV_LENGTH)
        inputStream.read(iv)
        val cipher = getCipher()
        val spec: AlgorithmParameterSpec = getGcmParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return SelfDestroyCipherInputStream(inputStream, cipher, secretKey)
    }

    override fun getCipherOutputStream(outputStream: OutputStream, key: ByteArray, associatedData: ByteArray?): OutputStream {
        val secretKey = getSecretKeySpec(key)
        val cipher = getCipher()
        val iv = ivProvider(AES_GCM_IV_LENGTH)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, getGcmParameterSpec(iv))
        associatedData?.let(cipher::updateAAD)
        outputStream.write(iv)
        return CipherOutputStream(outputStream, cipher)
    }

    private fun getGcmParameterSpec(iv: ByteArray) = GCMParameterSpec(AES_GCM_TAG_LENGTH_IN_BITS, iv)

    private fun getCipher() = Cipher.getInstance(AES_GCM_CIPHER_TYPE)

    private fun getSecretKeySpec(key: ByteArray) = SecretKeySpec(key, ALGORITHM_AES)

    companion object {
        private const val ALGORITHM_AES = "AES"

        private const val AES_GCM_CIPHER_TYPE = "AES/GCM/NoPadding"
        private const val AES_GCM_IV_LENGTH = 12
        private const val AES_GCM_TAG_LENGTH_IN_BITS = 128

        private const val STREAM_BUFFER_SIZE = DEFAULT_BUFFER_SIZE
    }
}
