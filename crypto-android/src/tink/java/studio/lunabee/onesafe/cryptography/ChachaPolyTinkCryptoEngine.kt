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
 * Last modified 4/6/23, 9:06 AM
 */

package studio.lunabee.onesafe.cryptography

import androidx.core.util.AtomicFile
import com.google.crypto.tink.aead.internal.InsecureNonceChaCha20Poly1305
import com.google.crypto.tink.aead.internal.Poly1305
import com.google.crypto.tink.subtle.ChaCha20Poly1305
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.cryptography.qualifier.CryptoDispatcher
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import javax.inject.Inject

class ChachaPolyTinkCryptoEngine @Inject constructor(
    private val ivProvider: IVProvider,
    @CryptoDispatcher private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : CryptoEngine {

    init {
        Timber.i("Initialize ${javaClass.simpleName} using Google Tink")
    }

    override suspend fun encrypt(plainData: ByteArray, key: ByteArray, associatedData: ByteArray?): ByteArray {
        return withContext(dispatcher) {
            val cipher = InsecureNonceChaCha20Poly1305(key)
            val nonce = ivProvider(NONCE_LENGTH)
            val output = ByteBuffer.allocate(NONCE_LENGTH + plainData.size + Poly1305.MAC_TAG_SIZE_IN_BYTES)
            output.put(nonce)
            cipher.encrypt(output, nonce, plainData, associatedData)
            output.array()
        }
    }

    override suspend fun decrypt(cipherData: ByteArray, key: ByteArray, associatedData: ByteArray?): ByteArray {
        return withContext(dispatcher) { ChaCha20Poly1305(key).decrypt(cipherData, associatedData) }
    }

    // This implementation does not scale well for large data
    override suspend fun decrypt(cipherFile: AtomicFile, key: ByteArray, associatedData: ByteArray?): ByteArray {
        return withContext(dispatcher) {
            val cipherData = cipherFile.readFully()
            ChaCha20Poly1305(key).decrypt(cipherData, associatedData)
        }
    }

    override fun getEncryptStream(file: File, key: ByteArray, associatedData: ByteArray?): OutputStream {
        throw NotImplementedError("Use JCE implementation instead")
    }

    override fun getDecryptStream(cipherFile: AtomicFile, key: ByteArray, associatedData: ByteArray?): InputStream {
        throw NotImplementedError("Use JCE implementation instead")
    }

    override fun getCipherOutputStream(outputStream: OutputStream, key: ByteArray, associatedData: ByteArray?): OutputStream {
        return outputStream
    }

    companion object {
        private const val NONCE_LENGTH = 12
    }
}
