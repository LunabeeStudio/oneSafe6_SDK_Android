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
 * Created by Lunabee Studio / Date - 7/3/2023 - for the oneSafe6 SDK.
 * Last modified 03/07/2023 14:01
 */

package studio.lunabee.onesafe.bubbles.crypto

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
import javax.inject.Inject

class AndroidDoubleRatchetKeyRepository @Inject constructor(
    private val hashEngine: DataHashEngine,
    private val keyExchangeEngine: KeyExchangeEngine,
) : DoubleRatchetKeyRepository {

    override suspend fun generateKeyPair(): AsymmetricKeyPair {
        val keyPair = keyExchangeEngine.generateKeyPair()
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
        keyExchangeEngine.createSharedSecret(
            publicKey = publicKey.value,
            privateKey = privateKey.value,
            out = out.value,
        )
        return out
    }

    override suspend fun deriveRootKeys(
        rootKey: DRRootKey,
        sharedSecret: DRSharedSecret,
        outRootKey: DRRootKey,
        outChainKey: DRChainKey,
    ): DerivedKeyRootPair {
        val packedOut = ByteArray(outRootKey.value.size + outChainKey.value.size)
        hashEngine.deriveKey(rootKey.value, sharedSecret.value, packedOut)
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
        hashEngine.deriveKey(chainKey.value, messageSalt, outMessageKey.value)
        hashEngine.deriveKey(chainKey.value, chainSalt, outChainKey.value)
        return DerivedKeyMessagePair(outChainKey, outMessageKey)
    }

    private companion object {
        private val messageSalt = byteArrayOf(0x01)
        private val chainSalt = byteArrayOf(0x02)
    }
}
