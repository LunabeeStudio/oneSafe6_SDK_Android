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
import io.mockk.mockk
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.usecase.authentication.IsCryptoDataReadyInMemoryUseCase
import studio.lunabee.onesafe.error.OSDomainError
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
}
