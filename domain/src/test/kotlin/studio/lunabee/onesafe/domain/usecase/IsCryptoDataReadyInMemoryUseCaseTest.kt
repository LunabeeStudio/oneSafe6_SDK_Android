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
 * Created by Lunabee Studio / Date - 8/23/2023 - for the oneSafe6 SDK.
 * Last modified 8/23/23, 2:43 PM
 */

package studio.lunabee.onesafe.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.usecase.authentication.IsCryptoDataReadyInMemoryUseCase
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.test.assertDoesNotThrow
import studio.lunabee.onesafe.test.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class IsCryptoDataReadyInMemoryUseCaseTest {
    private val mainCryptoRepository: MainCryptoRepository = mockk()
    private val useCase: IsCryptoDataReadyInMemoryUseCase = IsCryptoDataReadyInMemoryUseCase(mainCryptoRepository)

    @Test
    fun wait_crypto_loaded_test(): TestResult = runTest {
        coEvery { mainCryptoRepository.isCryptoDataInMemory(Duration.INFINITE) } returns true
        assertDoesNotThrow {
            useCase.wait()
        }
    }

    @Test
    fun wait_crypto_timeout_test(): TestResult = runTest {
        coEvery { mainCryptoRepository.isCryptoDataInMemory(any()) } returns false
        val err = assertThrows<OSDomainError> {
            useCase.wait(10.milliseconds)
        }
        assertEquals(OSDomainError.Code.CRYPTO_NOT_READY_TIMEOUT, err.code)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun with_crypto_test(): TestResult = runTest {
        val cryptoFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
        every { mainCryptoRepository.isCryptoDataInMemoryFlow() } returns cryptoFlow

        val testFlow = MutableStateFlow("data_0")

        val actual: MutableList<String> = mutableListOf()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            useCase.withCrypto(testFlow).toList(actual)
        }

        // No crypto
        assertEquals(0, actual.size)

        // Load crypto
        cryptoFlow.value = true
        assertEquals(1, actual.size)
        assertEquals("data_0", actual[0])

        // Unload crypto
        cryptoFlow.value = false
        assertEquals(1, actual.size)

        // Update data with crypto unload
        testFlow.value = "data_1"
        assertEquals(1, actual.size)

        // Load crypto
        cryptoFlow.value = true
        assertEquals(2, actual.size)
        assertEquals("data_1", actual[1])
    }
}
