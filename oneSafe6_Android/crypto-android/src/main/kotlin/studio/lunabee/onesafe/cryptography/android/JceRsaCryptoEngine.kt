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

import com.lunabee.lblogger.LBLogger
import org.conscrypt.Conscrypt
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Security
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.inject.Inject

private val logger = LBLogger.get<JceRsaCryptoEngine>()

class JceRsaCryptoEngine @Inject constructor() : RsaCryptoEngine {

    init {
        val cipher = try {
            getCipher()
        } catch (error: NoSuchAlgorithmException) {
            val jceProvider = Conscrypt.newProvider()
            val res = Security.addProvider(jceProvider)
            if (res == -1) {
                logger.e("Failed to insert $jceProvider")
            }
            getCipher()
        }

        logger.i("Initialize ${javaClass.simpleName} using ${cipher.provider}")
    }

    override fun getKeyPair(): KeyPair {
        val keyPairGenerator: KeyPairGenerator = KeyPairGenerator.getInstance(RsaAlgorithm)
        keyPairGenerator.initialize(RsaKeySize)
        return keyPairGenerator.genKeyPair()
    }

    override fun getPublicKey(key: ByteArray): PublicKey = KeyFactory
        .getInstance(RsaAlgorithm)
        .generatePublic(X509EncodedKeySpec(key))

    override fun encrypt(key: PublicKey, data: ByteArray): ByteArray {
        val cipher: Cipher = getCipher()
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(data)
    }

    override fun decrypt(key: PrivateKey, data: ByteArray): ByteArray {
        val cipher: Cipher = getCipher()
        cipher.init(Cipher.DECRYPT_MODE, key)
        return cipher.doFinal(data)
    }

    private fun getCipher() = Cipher.getInstance(RsaTransformation)

    companion object {
        private const val RsaKeySize: Int = 2048
        private const val RsaAlgorithm: String = "RSA"
        private const val RsaTransformation: String = "RSA/ECB/OAEPwithSHA-256andMGF1Padding"
    }
}
