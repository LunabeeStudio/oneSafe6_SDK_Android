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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import studio.lunabee.onesafe.cryptography.utils.SafeDataMutableStateFlow
import studio.lunabee.onesafe.error.OSCryptoError
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class SafeDataMutableStateFlowTest {

    private val overrideCode = OSCryptoError.Code.MASTER_KEY_ALREADY_LOADED
    private val nullableCode = OSCryptoError.Code.MASTER_KEY_NOT_LOADED

    private var testFlow: SafeDataMutableStateFlow = SafeDataMutableStateFlow(
        overrideCode = overrideCode,
        nullableCode = nullableCode,
    )

    @Test
    fun set_while_already_set_test(): TestResult = runTest {
        testFlow.value = "value".encodeToByteArray()
        val error = assertFailsWith<OSCryptoError> {
            testFlow.value = "value override".encodeToByteArray()
        }
        assertEquals(overrideCode, error.code)
    }

    @Test
    fun get_while_no_value_set_test(): TestResult = runTest {
        testFlow.value = null
        val error = assertFailsWith<OSCryptoError> {
            testFlow.value
        }
        assertEquals(nullableCode, error.code)
    }

    @Test
    fun collect_value_flow_test(): TestResult = runTest {
        val values = mutableListOf<ByteArray?>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            testFlow.toList(values)
        }
        assertEquals(null, values[0])
        testFlow.value = byteArrayOf(1)
        assertContentEquals(byteArrayOf(1), values[1])
        testFlow.value = null
        assertEquals(null, values[2])
        testFlow.value = byteArrayOf(2)
        assertContentEquals(byteArrayOf(2), values[3])
        val error = assertFailsWith<OSCryptoError> {
            testFlow.value = byteArrayOf(3)
        }
        assertEquals(overrideCode, error.code)
        collectJob.cancel()
    }
}
