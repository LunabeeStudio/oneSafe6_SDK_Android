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

import androidx.core.util.AtomicFile
import com.lunabee.lblogger.LBLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.cryptography.qualifier.CryptoDispatcher
import studio.lunabee.onesafe.cryptography.utils.SelfDestroyCipherInputStream
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
 * @param dispatcher The coroutine dispatcher used to run cryptographic operation on
 */
class AesCryptoEngine @Inject constructor(
    private val ivProvider: IVProvider,
    @CryptoDispatcher private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : CryptoEngine {

    init {
        val cipher = getCipher()
        logger.i("Initialize ${javaClass.simpleName} using ${cipher.provider}")
    }

    override suspend fun encrypt(plainData: ByteArray, key: ByteArray, associatedData: ByteArray?): ByteArray {
        return withContext(dispatcher) {
            doEncrypt(plainData, key)
        }
    }

    override suspend fun decrypt(cipherData: ByteArray, key: ByteArray, associatedData: ByteArray?): ByteArray {
        return withContext(dispatcher) {
            doDecrypt(cipherData, key)
        }
    }

    override suspend fun decrypt(cipherFile: AtomicFile, key: ByteArray, associatedData: ByteArray?): ByteArray {
        return withContext(dispatcher) {
            doDecrypt(cipherFile, key)
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

    @Suppress("NOTHING_TO_INLINE")
    private inline fun doEncrypt(plainData: ByteArray, key: ByteArray): ByteArray {
        val bos = ByteArrayOutputStream()
        getCipherOutputStream(bos, key, null).use { cos ->
            plainData.inputStream().use { input ->
                input.copyTo(cos)
            }
        }
        return bos.toByteArray()
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun doDecrypt(cipherData: ByteArray, key: ByteArray): ByteArray {
        val cipher = getCipher()
        val iv: ByteArray = try {
            cipherData.copyOfRange(0, AES_GCM_IV_LENGTH)
        } catch (e: IndexOutOfBoundsException) {
            throw OSCryptoError(OSCryptoError.Code.DECRYPTION_UNKNOWN_FAILURE, cause = e)
        }
        val ivSpec = getGcmParameterSpec(iv)
        val secretKey = getSecretKeySpec(key)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        return cipher.doFinal(
            cipherData,
            AES_GCM_IV_LENGTH,
            cipherData.size - AES_GCM_IV_LENGTH,
        )
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun doDecrypt(cipherFile: AtomicFile, key: ByteArray): ByteArray {
        val secretKey = getSecretKeySpec(key)

        val fis = cipherFile.openRead()
        val cis = getCipherInputStream(fis, secretKey)
        val bos = ByteArrayOutputStream()

        bos.use { output ->
            cis.use { cis ->
                cis.copyTo(output, STREAM_BUFFER_SIZE)
            }
        }

        return bos.toByteArray()
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
