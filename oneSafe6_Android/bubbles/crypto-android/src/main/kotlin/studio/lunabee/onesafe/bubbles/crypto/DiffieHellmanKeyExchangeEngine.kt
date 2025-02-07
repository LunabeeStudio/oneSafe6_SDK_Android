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
 * Created by Lunabee Studio / Date - 8/1/2023 - for the oneSafe6 SDK.
 * Last modified 01/08/2023 09:30
 */

package studio.lunabee.onesafe.bubbles.crypto

import studio.lunabee.bubbles.domain.crypto.BubblesKeyExchangeEngine
import studio.lunabee.bubbles.domain.model.BubblesKeyPair
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement
import javax.inject.Inject

class DiffieHellmanKeyExchangeEngine @Inject constructor() : BubblesKeyExchangeEngine {
    override fun generateKeyPair(): BubblesKeyPair {
        val ecSpec = ECGenParameterSpec(NAMED_CURVE_SPEC)
        val keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM_EC)
        keyPairGenerator.initialize(ecSpec)
        val javaKeyPair = keyPairGenerator.generateKeyPair()
        return BubblesKeyPair(publicKey = javaKeyPair.public.encoded, privateKey = javaKeyPair.private.encoded)
    }

    override fun createSharedSecret(publicKey: ByteArray, privateKey: ByteArray, size: Int): ByteArray {
        val out = ByteArray(size)
        val keyFactory = KeyFactory.getInstance(ALGORITHM_EC)
        val contactPublicKey = keyFactory.generatePublic(X509EncodedKeySpec(publicKey))
        val localPrivateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(privateKey))
        val keyAgreement = KeyAgreement.getInstance(ALGORITHM_EC_DH)
        keyAgreement.init(localPrivateKey)
        keyAgreement.doPhase(contactPublicKey, true)
        keyAgreement.generateSecret(out, 0)
        return out
    }

    private companion object {
        private const val NAMED_CURVE_SPEC = "secp256r1"
        private const val ALGORITHM_EC = "EC"
        private const val ALGORITHM_EC_DH = "ECDH"
    }
}
