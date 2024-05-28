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
 * Created by Lunabee Studio / Date - 1/16/2024 - for the oneSafe6 SDK.
 * Last modified 1/16/24, 11:59 AM
 */

package studio.lunabee.onesafe.importexport.usecase

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
import kotlin.test.Test
import org.threeten.extra.MutableClock
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertContentEquals

@OptIn(ExperimentalCoroutinesApi::class)
class GetEnableAutoBackupCtaStateUseCaseTest {
    private val clock = MutableClock.epochUTC()

    private val ctaStateFlow = MutableStateFlow<CtaState>(CtaState.Hidden)
    private val autoBackupEnabledFlow = MutableStateFlow(false)
    private val safeItemsCountFlow = MutableStateFlow(0)

    private val autoBackupSettingsRepository = mockk<AutoBackupSettingsRepository> {
        every { enableAutoBackupCtaState } returns ctaStateFlow
        every { autoBackupEnabled } returns autoBackupEnabledFlow

        // any() matcher does not work for value class. Maybe https://github.com/mockk/mockk/issues/859
        coEvery { setEnableAutoBackupCtaState(CtaState.Hidden) } coAnswers { ctaStateFlow.value = firstArg() }
        coEvery { setEnableAutoBackupCtaState(CtaState.VisibleSince(any())) } coAnswers { ctaStateFlow.value = firstArg() }
        coEvery { setEnableAutoBackupCtaState(CtaState.DismissedAt(any())) } coAnswers { ctaStateFlow.value = firstArg() }
    }

    private val itemRepository = mockk<SafeItemRepository> {
        every { getSafeItemsCountFlow() } returns safeItemsCountFlow
    }

    private val getEnableBackupCtaState: GetEnableAutoBackupCtaStateUseCase = GetEnableAutoBackupCtaStateUseCase(
        autoBackupSettingsRepository,
        itemRepository,
        clock,
    )

    @Test
    fun keep_dismissed_test(): TestResult = runTest {
        val dismissedAt = CtaState.DismissedAt(Instant.EPOCH.plusMillis(5))
        ctaStateFlow.value = dismissedAt

        val actual: MutableList<CtaState> = mutableListOf()
        val expected: MutableList<CtaState> = mutableListOf(dismissedAt)

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            getEnableBackupCtaState().toList(actual)
        }

        safeItemsCountFlow.value = 1

        assertContentEquals(actual, expected)

        job.cancel()
    }

    /* 1) initially hidden
     * 2) visible in 1 day because item count > 1
     * 3) hidden because auto backup enabled
     * 4) visible in (2+1) days because item count still > 1 and auto backup disabled (and manually move time by 2 days)
     * 5) force dismissedAt
     */
    @Test
    fun hidden_to_visibleSince_to_hidden_test(): TestResult = runTest {
        val dismissedAt = CtaState.DismissedAt(Instant.EPOCH.plusMillis(5))
        val actual: MutableList<CtaState> = mutableListOf()
        val expected: MutableList<CtaState> = mutableListOf(
            CtaState.Hidden,
            CtaState.VisibleSince(Instant.EPOCH.plus(1, ChronoUnit.DAYS)),
            CtaState.Hidden,
            CtaState.VisibleSince(Instant.EPOCH.plus(3, ChronoUnit.DAYS)),
            dismissedAt,
        )

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            getEnableBackupCtaState().toList(actual)
        }

        safeItemsCountFlow.value = 1
        autoBackupEnabledFlow.value = true
        clock.add(2, ChronoUnit.DAYS)
        autoBackupEnabledFlow.value = false
        ctaStateFlow.value = dismissedAt

        assertContentEquals(actual, expected, actual.joinToString { it.toString() })

        job.cancel()
    }
}
