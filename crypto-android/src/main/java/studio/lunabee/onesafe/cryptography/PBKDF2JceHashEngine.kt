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

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.bouncycastle.jce.provider.BouncyCastleProvider
import studio.lunabee.onesafe.cryptography.extension.use
import studio.lunabee.onesafe.cryptography.qualifier.CryptoDispatcher
import studio.lunabee.onesafe.cryptography.qualifier.PBKDF2Iterations
import studio.lunabee.onesafe.error.OSCryptoError
import timber.log.Timber
import java.security.NoSuchAlgorithmException
import java.security.Security
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject

class PBKDF2JceHashEngine @Inject constructor(
    @CryptoDispatcher private val dispatcher: CoroutineDispatcher,
    @PBKDF2Iterations private val iterationNumber: Int,
) : HashEngine {

    private val secretKeyFactory: SecretKeyFactory

    init {
        secretKeyFactory = try {
            getFactory()
        } catch (error: NoSuchAlgorithmException) {
            val bcProvider = BouncyCastleProvider()
            Security.removeProvider(bcProvider.name)
            val res = Security.addProvider(bcProvider)
            if (res == -1) {
                Timber.e("Failed to insert $bcProvider")
            }
            getFactory()
        }

        Timber.i("Initialize ${javaClass.simpleName} using ${secretKeyFactory.provider}")
    }

    override suspend fun deriveKey(password: CharArray, salt: ByteArray): ByteArray {
        if (password.isEmpty()) throw OSCryptoError(OSCryptoError.Code.DERIVATION_WITH_EMPTY_PASSWORD)
        return withContext(dispatcher) {
            doHash(password, salt)
        }
    }

    /**
     * Extracted for benchmarking
     *
     * @see studio.lunabee.onesafe.benchmark.cryptography.PBKDF2JceHashEngineBenchmark
     */
    @Suppress("NOTHING_TO_INLINE")
    private inline fun doHash(password: CharArray, salt: ByteArray): ByteArray {
        return PBEKeySpec(password, salt, iterationNumber, DERIVED_KEY_LENGTH_BIT).use { pbeKeySpec ->
            secretKeyFactory.generateSecret(pbeKeySpec).use { secretKey ->
                secretKey.encoded
            }
        }
    }

    private fun getFactory() = SecretKeyFactory.getInstance(ALGORITHM)

    companion object {
        private const val ALGORITHM = "PBKDF2withHmacSHA512"
        private const val DERIVED_KEY_LENGTH_BIT = 256
    }
}
