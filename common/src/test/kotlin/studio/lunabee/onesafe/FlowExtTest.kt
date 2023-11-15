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
 * Created by Lunabee Studio / Date - 11/6/2023 - for the oneSafe6 SDK.
 * Last modified 11/6/23, 8:42 AM
 */

package studio.lunabee.onesafe

import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class FlowExtTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun combine_empty_test(): TestResult = runTest {
        val resultFlow = emptyList<Flow<LBFlowResult<Unit>>>().combine()
        val result: MutableList<LBFlowResult<List<Unit?>>> = mutableListOf()

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            resultFlow.collect {
                println("Collect result $it")
                result += it
            }
        }

        assertIs<LBFlowResult.Success<Unit?>>(result.single())

        collectJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun combine_failure_test(): TestResult = runTest {
        val loadingData2 = "loading2"
        val failureData1 = "failure1"
        val successData2 = "success2"

        val flow1 = MutableStateFlow<LBFlowResult<String>?>(null)
        val flow2 = MutableStateFlow<LBFlowResult<String>?>(null)

        val result: MutableList<LBFlowResult<List<String?>>> = mutableListOf()
        val resultFlow = listOf(
            flow1.filterNotNull(),
            flow2.filterNotNull(),
        ).combine()

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            resultFlow.collect {
                println("Collect result $it")
                result += it
            }
        }

        var actual: LBFlowResult<List<String?>>?

        assertTrue(result.isEmpty())

        // Emit loading on both
        flow1.value = LBFlowResult.Loading()
        flow2.value = LBFlowResult.Loading(partialData = loadingData2)
        actual = result[0]
        assertIs<LBFlowResult.Loading<List<String?>>>(actual)
        assertContentEquals(listOf(null, loadingData2), actual.data)

        // Emit one failure on flow1
        flow1.value = LBFlowResult.Failure(failureData = failureData1)
        actual = result[1]
        assertIs<LBFlowResult.Loading<List<String?>>>(actual)
        assertContentEquals(listOf<String?>(failureData1, loadingData2), actual.data)

        // Emit one success on flow2
        flow2.value = LBFlowResult.Success(successData2)
        actual = result[2]
        assertIs<LBFlowResult.Failure<List<String?>>>(actual)
        assertContentEquals(listOf<String?>(failureData1, successData2), actual.data)

        assertEquals(result.size, 3)

        collectJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun combine_success_test(): TestResult = runTest {
        val successData1 = "success1"
        val successData2 = "success2"

        val flow1 = MutableStateFlow<LBFlowResult<String>?>(null)
        val flow2 = MutableStateFlow<LBFlowResult<String>?>(null)

        val result: MutableList<LBFlowResult<List<String?>>> = mutableListOf()
        val resultFlow = listOf(
            flow1.filterNotNull(),
            flow2.filterNotNull(),
        ).combine()

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            resultFlow.collect {
                println("Collect result $it")
                result += it
            }
        }

        assertTrue(result.isEmpty())

        // Emit success on both
        flow1.value = LBFlowResult.Success(successData = successData1)
        flow2.value = LBFlowResult.Success(successData = successData2)
        val actual = result[0]
        assertIs<LBFlowResult.Success<List<String?>>>(actual)
        assertContentEquals(listOf<String?>(successData1, successData2), actual.data)

        assertEquals(result.size, 1)

        collectJob.cancel()
    }
}
