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
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.cryptography.ChachaPolyJCECryptoEngine
import studio.lunabee.compose.androidtest.helper.LbcResourcesHelper
import timber.log.Timber
import java.io.File
import java.lang.reflect.Method

@LargeTest
@HiltAndroidTest
class ChachaPolyJCECryptoEngineBenchmark {

    @get:Rule(order = 0)
    var hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val benchmarkRule: BenchmarkRule = BenchmarkRule()

    private val cryptoEngine: ChachaPolyJCECryptoEngine = ChachaPolyJCECryptoEngine(
        ivProvider = { CryptoBenchUtils.iv12 },
    )

    val doEncrypt: Method = ChachaPolyJCECryptoEngine::class.java.getDeclaredMethod(
        "doEncrypt",
        ByteArray::class.java,
        ByteArray::class.java,
        ByteArray::class.java,
    ).apply {
        isAccessible = true
    }

    val doDecryptData: Method = ChachaPolyJCECryptoEngine::class.java.getDeclaredMethod(
        "doDecrypt",
        ByteArray::class.java,
        ByteArray::class.java,
        ByteArray::class.java,
    ).apply {
        isAccessible = true
    }

    val doDecryptFile: Method = ChachaPolyJCECryptoEngine::class.java.getDeclaredMethod(
        "doDecrypt",
        AtomicFile::class.java,
        ByteArray::class.java,
        ByteArray::class.java,
    ).apply {
        isAccessible = true
    }

    init {
        Timber.plant(Timber.DebugTree())
    }

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun chachaPoly1305_encrypt_benchmark() {
        benchmarkRule.measureRepeated {
            doEncrypt(cryptoEngine, CryptoBenchUtils.plainData, CryptoBenchUtils.key256)
        }
    }

    @Test
    fun chachaPoly1305_decrypt_data_benchmark() {
        benchmarkRule.measureRepeated {
            doDecryptData(cryptoEngine, CryptoBenchUtils.chacha20poly1305_data, CryptoBenchUtils.key256)
        }
    }

    @Test
    fun chachaPoly1305_decrypt_file_benchmark() {
        val cipherFile = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, "cipher_file")
        LbcResourcesHelper.copyResourceToDeviceFile(CryptoBenchUtils.chacha20poly1305_file.name, cipherFile)
        val atomicFile = AtomicFile(cipherFile)

        benchmarkRule.measureRepeated {
            doDecryptFile(cryptoEngine, atomicFile, CryptoBenchUtils.key256)
        }
    }
}
