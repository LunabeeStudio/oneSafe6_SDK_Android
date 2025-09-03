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

import com.google.crypto.tink.subtle.Hkdf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.domain.qualifier.CryptoDispatcher
import studio.lunabee.onesafe.jvm.use
import studio.lunabee.onesafe.jvm.toByteArray
import studio.lunabee.onesafe.use
import javax.inject.Inject

class HKDFTinkHashEngine @Inject constructor(
    @param:CryptoDispatcher private val dispatcher: CoroutineDispatcher,
) : PasswordHashEngine {
    override suspend fun deriveKey(password: CharArray, salt: ByteArray): ByteArray =
        withContext(dispatcher) {
            doHash(password, salt)
        }

    /**
     * Extracted for testing
     *
     * @see studio.lunabee.onesafe.cryptography.HKDFTinkHashEngineTest
     */
    @Suppress("NOTHING_TO_INLINE")
    private inline fun doHash(password: CharArray, salt: ByteArray): ByteArray {
        password.toByteArray().use { passwordBytes ->
            return Hkdf.computeHkdf(
                ALGORITHM,
                passwordBytes,
                salt,
                byteArrayOf(),
                DERIVED_KEY_LENGTH_BYTE,
            )
        }
    }

    companion object {
        private const val ALGORITHM = "HMACSHA512"
        private const val DERIVED_KEY_LENGTH_BYTE = 32
    }
}
