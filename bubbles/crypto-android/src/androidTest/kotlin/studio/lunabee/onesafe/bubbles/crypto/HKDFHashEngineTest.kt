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
 * Last modified 01/08/2023 10:02
 */

package studio.lunabee.onesafe.bubbles.crypto

import studio.lunabee.bubbles.domain.crypto.BubblesDataHashEngine
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.toByteArray
import kotlin.test.Test
import kotlin.test.assertContentEquals

class HKDFHashEngineTest {

    @Test
    fun deriveKeyTest() {
        val hashEngine1: BubblesDataHashEngine = HKDFHashEngine()
        val hashEngine2: BubblesDataHashEngine = HKDFHashEngine()

        val key1 = testUUIDs[0].toByteArray()
        val key2 = key1.copyOf()

        val salt1 = testUUIDs[1].toByteArray()
        val salt2 = salt1.copyOf()
        val result1 = hashEngine1.deriveKey(key1, salt1, 32)
        val result2 = hashEngine2.deriveKey(key2, salt2, 32)
        assertContentEquals(result1, result2)
    }
}
