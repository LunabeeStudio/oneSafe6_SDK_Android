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
 * Last modified 3/4/24, 12:31 PM
 */

package studio.lunabee.onesafe.benchmark.cryptography

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import kotlin.test.Test
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.extension.iconSample
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltTest
import javax.inject.Inject

@HiltAndroidTest
class CreateItemUseCaseBenchmark : OSHiltTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @get:Rule
    val benchmarkRule: BenchmarkRule = BenchmarkRule()

    @Inject
    lateinit var createItemUseCase: CreateItemUseCase

    @Test
    fun createItem_benchmark() {
        var count = 0.0
        benchmarkRule.measureRepeated {
            runBlocking {
                createItemUseCase("benchmark_item $count", null, false, iconSample, null, count)
            }
            runWithTimingDisabled { count++ }
        }
    }
}
