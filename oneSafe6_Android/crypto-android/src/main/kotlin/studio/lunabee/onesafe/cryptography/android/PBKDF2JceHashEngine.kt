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
 * Last modified 9/4/24, 10:48 AM
 */

package studio.lunabee.onesafe.cryptography.android

import com.lunabee.lblogger.LBLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.bouncycastle.jce.provider.BouncyCastleProvider
import studio.lunabee.onesafe.cryptography.android.extension.use
import studio.lunabee.onesafe.error.OSCryptoError
import java.security.NoSuchAlgorithmException
import java.security.Security
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private val logger = LBLogger.get<PBKDF2JceHashEngine>()

class PBKDF2JceHashEngine(
    private val dispatcher: CoroutineDispatcher,
    private val iterationNumber: Int,
) : PasswordHashEngine {

    private val secretKeyFactory: SecretKeyFactory

    init {
        secretKeyFactory = try {
            getFactory()
        } catch (error: NoSuchAlgorithmException) {
            val bcProvider = BouncyCastleProvider()
            Security.removeProvider(bcProvider.name)
            val res = Security.addProvider(bcProvider)
            if (res == -1) {
                logger.e("Failed to insert $bcProvider")
            }
            getFactory()
        }

        logger.i("Initialize ${javaClass.simpleName} using ${secretKeyFactory.provider}")
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
    private inline fun doHash(password: CharArray, salt: ByteArray): ByteArray = PBEKeySpec(
        password,
        salt,
        iterationNumber,
        DerivedKeyLengthBit,
    ).use { pbeKeySpec ->
        secretKeyFactory.generateSecret(pbeKeySpec).use { secretKey ->
            secretKey.encoded
        }
    }

    private fun getFactory() = SecretKeyFactory.getInstance(Algorithm)

    companion object {
        private const val Algorithm = "PBKDF2withHmacSHA512"
        private const val DerivedKeyLengthBit = 256
    }
}
