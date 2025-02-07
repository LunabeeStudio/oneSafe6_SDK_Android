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
 * Created by Lunabee Studio / Date - 9/30/2024 - for the oneSafe6 SDK.
 * Last modified 30/09/2024 17:10
 */

package studio.lunabee.onesafe.usecase.panicdestruction

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.onesafe.domain.usecase.panicmode.ExecutePanicDestructionUseCase
import studio.lunabee.onesafe.domain.usecase.panicmode.SetIsPanicDestructionEnabledUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.OSTestConfig
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@HiltAndroidTest
class ExecutePanicDestructionUseCaseTest : OSHiltUnitTest() {

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject
    lateinit var executePanicDestructionUseCase: ExecutePanicDestructionUseCase

    @Inject
    lateinit var setIsPanicDestructionEnabledUseCase: SetIsPanicDestructionEnabledUseCase

    @Test
    fun `Execute auto destruction if disabled`(): TestResult = runTest {
        val currentSafeId = safeRepository.currentSafeId()
        assertEquals(OSTestConfig.extraSafeIds.size + 1, safeRepository.getAllSafeOrderByLastOpenAsc().size)
        setIsPanicDestructionEnabledUseCase(false)
        assertFalse(safeRepository.isPanicDestructionEnabledFlow(currentSafeId).first())
        executePanicDestructionUseCase()
        assertEquals(OSTestConfig.extraSafeIds.size + 1, safeRepository.getAllSafeOrderByLastOpenAsc().size)
    }

    @Test
    fun `Execute auto destruction if enabled`(): TestResult = runTest {
        val currentSafeId = safeRepository.currentSafeId()
        assertEquals(OSTestConfig.extraSafeIds.size + 1, safeRepository.getAllSafeOrderByLastOpenAsc().size)
        setIsPanicDestructionEnabledUseCase(true)
        assert(safeRepository.isPanicDestructionEnabledFlow(currentSafeId).first())
        executePanicDestructionUseCase()
        assertEquals(OSTestConfig.extraSafeIds.size, safeRepository.getAllSafeOrderByLastOpenAsc().size)
    }
}
