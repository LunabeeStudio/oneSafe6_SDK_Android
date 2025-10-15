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
 * Last modified 9/4/24, 10:04 AM
 */

package studio.lunabee.onesafe.cryptography.android

import androidx.core.util.AtomicFile
import com.google.crypto.tink.aead.internal.InsecureNonceChaCha20Poly1305
import com.google.crypto.tink.aead.internal.Poly1305
import com.google.crypto.tink.subtle.ChaCha20Poly1305
import com.lunabee.lblogger.LBLogger
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import javax.inject.Inject

private val logger = LBLogger.get<ChachaPolyTinkCryptoEngine>()

class ChachaPolyTinkCryptoEngine @Inject constructor(
    private val ivProvider: IVProvider,
) : CryptoEngine {

    init {
        logger.i("Initialize ${javaClass.simpleName} using Google Tink")
    }

    override fun encrypt(plainData: ByteArray, key: ByteArray, associatedData: ByteArray?): Result<ByteArray> = runCatching {
        val cipher = InsecureNonceChaCha20Poly1305(key)
        val nonce = ivProvider(NonceLength)
        val output = ByteBuffer.allocate(NonceLength + plainData.size + Poly1305.MAC_TAG_SIZE_IN_BYTES)
        output.put(nonce)
        cipher.encrypt(output, nonce, plainData, associatedData)
        output.array()
    }

    override fun decrypt(cipherData: ByteArray, key: ByteArray, associatedData: ByteArray?): Result<ByteArray> = runCatching {
        ChaCha20Poly1305(key).decrypt(cipherData, associatedData)
    }

    // This implementation does not scale well for large data
    override fun decrypt(cipherFile: AtomicFile, key: ByteArray, associatedData: ByteArray?): Result<ByteArray> = runCatching {
        val cipherData = cipherFile.readFully()
        ChaCha20Poly1305(key).decrypt(cipherData, associatedData)
    }

    override fun getEncryptStream(file: File, key: ByteArray, associatedData: ByteArray?): OutputStream = throw NotImplementedError(
        "Use JCE implementation instead",
    )

    override fun getDecryptStream(
        cipherFile: AtomicFile,
        key: ByteArray,
        associatedData: ByteArray?,
    ): InputStream = throw NotImplementedError(
        "Use JCE implementation instead",
    )

    override fun getCipherOutputStream(outputStream: OutputStream, key: ByteArray, associatedData: ByteArray?): OutputStream = outputStream

    companion object {
        private const val NonceLength = 12
    }
}
