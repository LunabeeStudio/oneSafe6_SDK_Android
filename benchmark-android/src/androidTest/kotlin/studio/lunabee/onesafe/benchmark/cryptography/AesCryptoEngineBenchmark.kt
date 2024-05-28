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

package studio.lunabee.onesafe.benchmark.cryptography

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.core.util.AtomicFile
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import kotlin.test.Test
import studio.lunabee.compose.androidtest.helper.LbcResourcesHelper
import studio.lunabee.onesafe.cryptography.AesCryptoEngine
import java.io.File

@LargeTest
class AesCryptoEngineBenchmark {

    @get:Rule
    val benchmarkRule: BenchmarkRule = BenchmarkRule()

    private val cryptoEngine: AesCryptoEngine = AesCryptoEngine(
        ivProvider = { CryptoBenchUtils.iv12 },
    )

    @Test
    fun aesGcm_encrypt_benchmark() {
        benchmarkRule.measureRepeated {
            cryptoEngine.encrypt(CryptoBenchUtils.plainData, CryptoBenchUtils.key256, null)
        }
    }

    @Test
    fun aesGcm_decrypt_data_benchmark() {
        benchmarkRule.measureRepeated {
            cryptoEngine.decrypt(CryptoBenchUtils.aes256gcm_data, CryptoBenchUtils.key256, null)
        }
    }

    @Test
    fun aesGcm_decrypt_file_benchmark() {
        val cipherFile = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, "cipher_file")
        LbcResourcesHelper.copyResourceToDeviceFile(CryptoBenchUtils.aes256gcm_file.name, cipherFile)
        val atomicFile = AtomicFile(cipherFile)

        benchmarkRule.measureRepeated {
            cryptoEngine.decrypt(atomicFile, CryptoBenchUtils.key256, null)
        }
    }
}
