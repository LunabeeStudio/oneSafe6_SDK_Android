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
import studio.lunabee.bubbles.domain.crypto.BubblesCryptoEngine
import java.io.File
import java.io.InputStream
import java.io.OutputStream

interface CryptoEngine : BubblesCryptoEngine {
    fun encrypt(plainData: ByteArray, key: ByteArray, associatedData: ByteArray?): Result<ByteArray>
    fun decrypt(cipherData: ByteArray, key: ByteArray, associatedData: ByteArray?): Result<ByteArray>
    fun decrypt(cipherFile: AtomicFile, key: ByteArray, associatedData: ByteArray?): Result<ByteArray>
    fun getEncryptStream(file: File, key: ByteArray, associatedData: ByteArray?): OutputStream
    fun getDecryptStream(cipherFile: AtomicFile, key: ByteArray, associatedData: ByteArray?): InputStream
    fun getCipherOutputStream(outputStream: OutputStream, key: ByteArray, associatedData: ByteArray?): OutputStream

    override fun bubblesEncrypt(plainData: ByteArray, key: ByteArray, associatedData: ByteArray?): ByteArray? {
        return encrypt(plainData, key, associatedData).getOrNull()
    }

    override fun bubblesDecrypt(cipherData: ByteArray, key: ByteArray, associatedData: ByteArray?): ByteArray? {
        return decrypt(cipherData, key, associatedData).getOrNull()
    }
}
