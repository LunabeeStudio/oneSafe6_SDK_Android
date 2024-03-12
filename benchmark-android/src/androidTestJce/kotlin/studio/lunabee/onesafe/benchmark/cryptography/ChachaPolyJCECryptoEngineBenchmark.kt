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
import studio.lunabee.compose.androidtest.helper.LbcResourcesHelper
import studio.lunabee.onesafe.cryptography.ChachaPolyJCECryptoEngine
import java.io.File
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.time.measureTime

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

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun chachaPoly1305_encrypt_benchmark() {
        benchmarkRule.measureRepeated {
            cryptoEngine.encrypt(CryptoBenchUtils.plainData, CryptoBenchUtils.key256, null)
        }
    }

    @Test
    fun chachaPoly1305_decrypt_data_benchmark() {
        benchmarkRule.measureRepeated {
            cryptoEngine.decrypt(CryptoBenchUtils.chacha20poly1305_data, CryptoBenchUtils.key256, null)
        }
    }

    @Test
    fun chachaPoly1305_decrypt_file_benchmark() {
        val cipherFile = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, "cipher_file")
        LbcResourcesHelper.copyResourceToDeviceFile(CryptoBenchUtils.chacha20poly1305_file.name, cipherFile)
        val atomicFile = AtomicFile(cipherFile)

        benchmarkRule.measureRepeated {
            cryptoEngine.decrypt(atomicFile, CryptoBenchUtils.key256, null)
        }
    }

    @Test
    fun chachaPoly1305_decrypt_file_stream_benchmark() {
        val cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
        val fileSizeMB = 50

        val plainFile = File(cacheDir, "plain_file")
        val plainFile2 = File(cacheDir, "plain_file2")
        plainFile.delete()
        plainFile.outputStream().use { fos ->
            repeat(1024 * fileSizeMB) {
                fos.write(ByteArray(1024) { it.toByte() })
            }
        }
        println("File size = ${plainFile.length() / 1024f / 1024f}mo")

        val cipherFile = File(cacheDir, "cipher_file")
        cipherFile.delete()

        measureTime {
            cryptoEngine.getEncryptStream(cipherFile, CryptoBenchUtils.key256, null).use { fos ->
                plainFile.inputStream().use { fis ->
                    fis.copyTo(fos)
                }
            }
        }.let {
            println("Encrypt duration = $it")
        }

        val atomicFile = AtomicFile(cipherFile)

        benchmarkRule.measureRepeated {
            plainFile2.outputStream().use {
                cryptoEngine.getDecryptStream(atomicFile, CryptoBenchUtils.key256, null).use { fis ->
                    plainFile2.outputStream().use { fos ->
                        fis.copyTo(fos)
                    }
                }
            }

            runWithTimingDisabled {
                assertEquals(plainFile.length(), plainFile2.length())
                plainFile2.delete()
            }
        }
    }

    @Suppress("unused") // Could be useful (but heavier to run in loop)
    private fun assertFileContent(plainFile: File, plainFile2: File) {
        assertEquals(plainFile.length(), plainFile2.length())
        plainFile.inputStream().use { fis ->
            plainFile2.inputStream().use { fis2 ->
                var readSize = -1
                val read = ByteArray(512)
                val read2 = ByteArray(512)
                while (readSize == -1 || readSize == 512) {
                    readSize = fis.read(read)
                    fis2.read(read2)
                    assertContentEquals(read, read2)
                }
            }
        }
    }
}
