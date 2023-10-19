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
 * Created by Lunabee Studio / Date - 5/17/2023 - for the oneSafe6 SDK.
 * Last modified 5/17/23, 5:23 PM
 */

package studio.lunabee.onesafe.domain.usecase

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.authentication.IsCryptoDataReadyInMemoryUseCase
import studio.lunabee.onesafe.domain.usecase.item.SecureGetItemUseCase
import studio.lunabee.onesafe.test.OSTestUtils
import java.util.UUID
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class SecureGetItemUseCaseTest {

    private val uuid = UUID.randomUUID()
    private val expectedItem: SafeItem = OSTestUtils.createSafeItem(id = uuid)

    private val safeItemRepository: SafeItemRepository = mockk {
        every { getSafeItemFlow(any()) } returns flowOf(null)
        every { getSafeItemFlow(uuid) } returns flowOf(expectedItem)
    }

    private val isLoadedFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private val isCryptoDataReadyInMemoryUseCase: IsCryptoDataReadyInMemoryUseCase = mockk {
        every { this@mockk.flow() } returns isLoadedFlow
    }

    // Add a timeout around SecureGetItemUseCase flow so we can test the empty flow case
    private val useCase: SecureGetItemUseCase =
        spyk(SecureGetItemUseCase(safeItemRepository, isCryptoDataReadyInMemoryUseCase)) {
            every { this@spyk.invoke(any()) } answers { callOriginal().timeout(100.milliseconds) }
        }

    @Test
    fun master_key_loaded_test(): TestResult = runTest {
        val actualItem = useCase(uuid).first()
        assertEquals(expectedItem, actualItem)

        val actualWrongItem = useCase(UUID.randomUUID()).first()
        assertNull(actualWrongItem)
    }

    @Test
    fun master_key_not_loaded_test(): TestResult = runTest {
        isLoadedFlow.value = false

        val itemFlow = useCase(uuid)
        assertThrows<TimeoutCancellationException> {
            itemFlow.first()
        }

        val itemWrongFlow = useCase(UUID.randomUUID())
        assertThrows<TimeoutCancellationException> {
            itemWrongFlow.first()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun load_unload_master_key_test(): TestResult = runTest {
        isLoadedFlow.value = false

        val itemFlow = useCase(uuid)
        val expected = List(2) { expectedItem }
        val actual: MutableList<SafeItem?> = mutableListOf()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            itemFlow.toList(actual)
        }

        isLoadedFlow.value = true
        isLoadedFlow.value = false
        isLoadedFlow.value = true

        assertContentEquals(expected, actual)
    }
}
