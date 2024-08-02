/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 3/4/2024 - for the oneSafe6 SDK.
 * Last modified 3/4/24, 11:23 AM
 */

package studio.lunabee.onesafe.benchmark.cryptography

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import kotlin.test.Test
import studio.lunabee.onesafe.cryptography.AndroidMainCryptoRepository
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltTest
import studio.lunabee.onesafe.test.testUUIDs
import javax.inject.Inject

@HiltAndroidTest
class AndroidMainCryptoRepositoryBenchmark : OSHiltTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @get:Rule
    val benchmarkRule: BenchmarkRule = BenchmarkRule()

    @Inject
    lateinit var cryptoRepo: AndroidMainCryptoRepository

    @Test
    fun crypto_encrypt_item_benchmark() {
        val uuid = testUUIDs[0]
        val entry = EncryptEntry("aaa")
        benchmarkRule.measureRepeated {
            runBlocking {
                val itemKey = cryptoRepo.generateKeyForItemId(uuid)
                cryptoRepo.encrypt(itemKey, entry)
            }
        }
    }

    @Test
    fun crypto_encrypt_multi_item_benchmark() {
        val uuid = testUUIDs[0]
        val entry = EncryptEntry("aaa")
        benchmarkRule.measureRepeated {
            repeat(10) {
                runBlocking {
                    val itemKey = cryptoRepo.generateKeyForItemId(uuid)
                    cryptoRepo.encrypt(itemKey, entry)
                }
            }
        }
    }
}
