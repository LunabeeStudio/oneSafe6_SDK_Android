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

package studio.lunabee.onesafe.cryptography

import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Test
import studio.lunabee.onesafe.test.ConsoleTree
import timber.log.Timber

class AesCryptoEngineTest {

    private val crypto: AesCryptoEngine = AesCryptoEngine(
        ivProvider = { CryptoAndroidTestUtils.iv12 },
    )

    init {
        Timber.plant(ConsoleTree())
    }

    @Test
    fun encrypt_data_test(): TestResult = runTest { crypto.encrypt_aes256gcm_data() }

    @Test
    fun decrypt_data_test(): TestResult = runTest { crypto.decrypt_aes256gcm_data() }

    @Test
    fun decrypt_aes256gcm_file_test(): TestResult = runTest { crypto.decrypt_aes256gcm_file() }
}
