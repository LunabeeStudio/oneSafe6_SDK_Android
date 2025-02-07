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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import java.util.Base64

class HKDFTinkHashEngineTest {

    private val hkdf = HKDFTinkHashEngine(Dispatchers.Default)
    private val expectedKey = Base64.getDecoder().decode("Z3B/kHgDhRM8maTsNQxrbFUAPQN30jLD+br3xFhrkOs=")

    /**
     * https://www.notion.so/lunabeestudio/Crypto-diagrams-37c478e035fc41b1b0872fb74e9eec0f#7f9bacdac66b4b9c9b5e2bacb3b3e99f
     */
    @Test
    fun deriveKey_stability_test(): TestResult = runTest { hkdf.deriveKey_stability_test(expectedKey) }
}
