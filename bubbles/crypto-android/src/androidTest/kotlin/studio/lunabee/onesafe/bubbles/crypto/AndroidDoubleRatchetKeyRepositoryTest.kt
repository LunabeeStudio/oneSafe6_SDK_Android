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

import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import studio.lunabee.doubleratchet.model.DRChainKey
import studio.lunabee.doubleratchet.model.DRRootKey
import studio.lunabee.doubleratchet.model.DRSharedSecret
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.toByteArray
import kotlin.test.assertContentEquals

class AndroidDoubleRatchetKeyRepositoryTest {

    private val hashEngine: DataHashEngine = HKDFHashEngine()
    private val keyExchangeEngine: KeyExchangeEngine = DiffieHellmanKeyExchangeEngine()

    @Test
    fun hkdfRootAlgorithmTest(): TestResult = runTest {
        val sharedSecret = DRSharedSecret(testUUIDs[0].toByteArray())
        val rootKey = DRRootKey(testUUIDs[1].toByteArray())
        val cryptoRepository1 = AndroidDoubleRatchetKeyRepository(hashEngine, keyExchangeEngine)
        val cryptoRepository2 = AndroidDoubleRatchetKeyRepository(hashEngine, keyExchangeEngine)
        val rootKey2 = DRRootKey(rootKey.value.copyOf())
        val value1 = cryptoRepository1.deriveRootKeys(rootKey, sharedSecret)
        val value2 = cryptoRepository2.deriveRootKeys(rootKey2, sharedSecret)

        assertContentEquals(value1.rootKey.value, value2.rootKey.value)
        assertContentEquals(value1.chainKey.value, value2.chainKey.value)
    }

    @Test
    fun dhSharedSecretAlgorithmTest(): TestResult = runTest {
        val cryptoRepositoryA = AndroidDoubleRatchetKeyRepository(hashEngine, keyExchangeEngine)
        val cryptoRepositoryB = AndroidDoubleRatchetKeyRepository(hashEngine, keyExchangeEngine)
        val keyPairAlice = cryptoRepositoryA.generateKeyPair()
        val keyPairBob = cryptoRepositoryB.generateKeyPair()
        val sharedSecretAlice = DRSharedSecret(ByteArray(cryptoRepositoryA.sharedSecretByteSize) { -1 })
        val sharedSecretBob = DRSharedSecret(ByteArray(cryptoRepositoryB.sharedSecretByteSize) { -2 })

        cryptoRepositoryA.createDiffieHellmanSharedSecret(keyPairBob.publicKey, keyPairAlice.privateKey, sharedSecretAlice)
        cryptoRepositoryB.createDiffieHellmanSharedSecret(keyPairAlice.publicKey, keyPairBob.privateKey, sharedSecretBob)

        assertContentEquals(sharedSecretAlice.value, sharedSecretBob.value)
    }

    @Test
    fun kdfChainAlgorithmTest(): TestResult = runTest {
        val chainKey = DRChainKey(testUUIDs[0].toByteArray())
        val chainKey2 = DRChainKey(chainKey.value.copyOf())
        val cryptoRepository1 = AndroidDoubleRatchetKeyRepository(hashEngine, keyExchangeEngine)
        val cryptoRepository2 = AndroidDoubleRatchetKeyRepository(hashEngine, keyExchangeEngine)
        val value1 = cryptoRepository1.deriveChainKeys(chainKey)
        val value2 = cryptoRepository2.deriveChainKeys(chainKey2)

        assertContentEquals(value1.messageKey.value, value2.messageKey.value)
        assertContentEquals(value1.chainKey.value, value2.chainKey.value)
    }
}
