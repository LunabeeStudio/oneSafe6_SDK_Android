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
 * Created by Lunabee Studio / Date - 8/23/2024 - for the oneSafe6 SDK.
 * Last modified 8/20/24, 10:47 AM
 */

package studio.lunabee.onesafe.cryptography.android

import studio.lunabee.onesafe.test.assertContentNotEquals
import kotlin.test.Test
import kotlin.test.assertContentEquals

class JceRsaCryptoEngineTest {

    val rsaCryptoEngine = JceRsaCryptoEngine()

    // generate and retrieve pub key
    @Test
    fun getKeyPair_getPublicKey_test() {
        val keyPair = rsaCryptoEngine.getKeyPair()
        val pubKey = rsaCryptoEngine.getPublicKey(keyPair.public.encoded)
        assertContentEquals(keyPair.public.encoded, pubKey.encoded)
    }

    @Test
    fun encrypt_decrypt_test() {
        val expected = byteArrayOf(1, 2, 3, 4, 5, 6)
        val keyPair = rsaCryptoEngine.getKeyPair()
        val cipherData = rsaCryptoEngine.encrypt(keyPair.public, expected)
        val actual = rsaCryptoEngine.decrypt(keyPair.private, cipherData)

        assertContentNotEquals(cipherData, actual)
        assertContentEquals(expected, actual)
    }
}
