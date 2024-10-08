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
 * Last modified 4/6/23, 9:06 AM
 */

package studio.lunabee.onesafe.cryptography.android

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChachaPolyTinkCryptoEngineTest {

    private val crypto: ChachaPolyTinkCryptoEngine = ChachaPolyTinkCryptoEngine(
        ivProvider = { CryptoAndroidTestUtils.iv12 },
    )

    @Test
    fun encrypt_data_test(): TestResult = runTest { crypto.encrypt_chacha20poly1305_data() }

    @Test
    fun encrypt_data_test_authenticated(): TestResult = runTest { crypto.encrypt_chacha20poly1305_data_authenticated() }

    @Test
    fun decrypt_data_test(): TestResult = runTest { crypto.decrypt_chacha20poly1305_data() }

    @Test
    fun decrypt_data_test_authenticated(): TestResult = runTest { crypto.decrypt_chacha20poly1305_data_authenticated() }

    @Test
    fun decrypt_file_test(): TestResult = runTest { crypto.decrypt_chacha20poly1305_file() }
}
