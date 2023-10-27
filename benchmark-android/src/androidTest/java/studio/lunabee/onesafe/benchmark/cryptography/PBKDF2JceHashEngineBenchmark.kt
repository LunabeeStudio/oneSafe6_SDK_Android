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

import android.util.Base64
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.filters.LargeTest
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.cryptography.CryptoConstants
import studio.lunabee.onesafe.cryptography.PBKDF2JceHashEngine
import timber.log.Timber
import java.lang.reflect.Method

@LargeTest
class PBKDF2JceHashEngineBenchmark {

    @get:Rule
    val benchmarkRule: BenchmarkRule = BenchmarkRule()

    init {
        Timber.plant(Timber.DebugTree())
    }

    private val hashEngine: PBKDF2JceHashEngine = PBKDF2JceHashEngine(StandardTestDispatcher(), CryptoConstants.PBKDF2Iterations)

    private val password: CharArray = "LTDf#@sGEdczDe?X@53TK&P4A^heLttP".toCharArray()
    private val salt = Base64.decode("rTygUEZCVBg4RNbJP1U16QFgIeIKwg/T0gEVA0cfIDU=", Base64.NO_WRAP)

    private val doHash: Method = PBKDF2JceHashEngine::class.java.getDeclaredMethod(
        "doHash",
        CharArray::class.java,
        ByteArray::class.java,
    ).apply {
        isAccessible = true
    }

    @Test
    fun hashEngine_benchmark() {
        benchmarkRule.measureRepeated {
            doHash(hashEngine, password, salt)
        }
    }
}
