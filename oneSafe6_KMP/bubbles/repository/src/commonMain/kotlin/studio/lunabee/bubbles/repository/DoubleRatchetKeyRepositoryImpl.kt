/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 7/25/2024 - for the oneSafe6 SDK.
 * Last modified 25/07/2024 11:02
 */

package studio.lunabee.bubbles.repository

import studio.lunabee.bubbles.domain.crypto.BubblesDataHashEngine
import studio.lunabee.bubbles.domain.crypto.BubblesKeyExchangeEngine
import studio.lunabee.doubleratchet.crypto.DoubleRatchetKeyRepository
import studio.lunabee.doubleratchet.model.AsymmetricKeyPair
import studio.lunabee.doubleratchet.model.DRChainKey
import studio.lunabee.doubleratchet.model.DRMessageKey
import studio.lunabee.doubleratchet.model.DRPrivateKey
import studio.lunabee.doubleratchet.model.DRPublicKey
import studio.lunabee.doubleratchet.model.DRRootKey
import studio.lunabee.doubleratchet.model.DRSharedSecret
import studio.lunabee.doubleratchet.model.DerivedKeyMessagePair
import studio.lunabee.doubleratchet.model.DerivedKeyRootPair
import studio.lunabee.onesafe.di.Inject

class DoubleRatchetKeyRepositoryImpl @Inject constructor(
    private val hashEngine: BubblesDataHashEngine,
    private val bubblesKeyExchangeEngine: BubblesKeyExchangeEngine,
) : DoubleRatchetKeyRepository {

    override suspend fun generateKeyPair(): AsymmetricKeyPair {
        val keyPair = bubblesKeyExchangeEngine.generateKeyPair()
        return AsymmetricKeyPair(
            publicKey = DRPublicKey(keyPair.publicKey),
            privateKey = DRPrivateKey(keyPair.privateKey),
        )
    }

    override suspend fun createDiffieHellmanSharedSecret(
        publicKey: DRPublicKey,
        privateKey: DRPrivateKey,
        out: DRSharedSecret,
    ): DRSharedSecret {
        bubblesKeyExchangeEngine
            .createSharedSecret(
                publicKey = publicKey.value,
                privateKey = privateKey.value,
                size = out.value.size,
            ).copyInto(out.value)
        return out
    }

    override suspend fun deriveRootKeys(
        rootKey: DRRootKey,
        sharedSecret: DRSharedSecret,
        outRootKey: DRRootKey,
        outChainKey: DRChainKey,
    ): DerivedKeyRootPair {
        val packedOut = hashEngine.deriveKey(
            rootKey.value,
            sharedSecret.value,
            outRootKey.value.size + outChainKey.value.size,
        )
        packedOut.copyInto(
            destination = outRootKey.value,
            startIndex = 0,
            endIndex = outRootKey.value.size,
        )
        packedOut.copyInto(
            destination = outChainKey.value,
            startIndex = outRootKey.value.size,
        )
        return DerivedKeyRootPair(outRootKey, outChainKey)
    }

    override suspend fun deriveChainKeys(
        chainKey: DRChainKey,
        outChainKey: DRChainKey,
        outMessageKey: DRMessageKey,
    ): DerivedKeyMessagePair {
        hashEngine.deriveKey(chainKey.value, messageSalt, outMessageKey.value.size).copyInto(outMessageKey.value)
        hashEngine.deriveKey(chainKey.value, chainSalt, outChainKey.value.size).copyInto(outChainKey.value)
        return DerivedKeyMessagePair(outChainKey, outMessageKey)
    }

    private companion object {
        private val messageSalt = byteArrayOf(0x01)
        private val chainSalt = byteArrayOf(0x02)
    }
}
