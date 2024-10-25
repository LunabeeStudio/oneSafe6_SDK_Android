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
 * Created by Lunabee Studio / Date - 4/2/2024 - for the oneSafe6 SDK.
 * Last modified 4/2/24, 11:50 AM
 */

package studio.lunabee.onesafe.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.onesafe.domain.Constant
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.domain.model.warning.PreventionSettingsWarning
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.repository.SafeSettingsRepository
import studio.lunabee.onesafe.domain.repository.SecuritySettingsRepository
import studio.lunabee.onesafe.domain.usecase.settings.GetPreventionWarningCtaStateUseCase
import studio.lunabee.onesafe.jvm.toByteArray
import studio.lunabee.onesafe.test.testUUIDs
import java.time.Clock
import java.time.Instant
import kotlin.test.assertEquals

class GetPreventionWarningCtaStateUseCaseTest {
    private val settingRepository: SafeSettingsRepository = mockk()
    private val safeRepository: SafeRepository = mockk {
        every { currentSafeIdFlow() } returns flowOf(SafeId(testUUIDs[0].toByteArray()))
    }
    private val securitySettingsRepository: SecuritySettingsRepository = mockk()
    private val contactRepository: ContactRepository = mockk()
    private val safeItemRepository: SafeItemRepository = mockk()

    private val lastDismissAt: Instant
        get() = Instant.now().minus(Constant.DelayBeforeShowingCtaState, Constant.DelayUnitPreventionWarningCtaState)

    val useCase: GetPreventionWarningCtaStateUseCase = GetPreventionWarningCtaStateUseCase(
        settingRepository = settingRepository,
        safeRepository = safeRepository,
        securitySettingsRepository = securitySettingsRepository,
        contactRepository = contactRepository,
        safeItemRepository = safeItemRepository,
        clock = Clock.systemUTC(),
    )

    @Test
    fun prevention_warning_delay_not_elapsed_test() {
        runTest {
            every { safeRepository.isBiometricEnabledForSafeFlow(any()) } returns flowOf(true)
            every { securitySettingsRepository.verifyPasswordIntervalFlow(any()) } returns flowOf(VerifyPasswordInterval.NEVER)
            every { contactRepository.getContactCountFlow(any()) } returns flowOf(10)
            every { safeItemRepository.getSafeItemsCountFlow(any()) } returns flowOf(10)
            every { settingRepository.hasBackupSince(any(), any()) } returns flowOf(true)
            every { settingRepository.preventionWarningCtaState(any()) } returns flowOf(CtaState.DismissedAt(Instant.now()))
            assertEquals(null, useCase().first())
        }
    }

    @Test
    fun prevention_warning_biometric_test() {
        runTest {
            every { safeRepository.isBiometricEnabledForSafeFlow(any()) } returns flowOf(true)
            every { securitySettingsRepository.verifyPasswordIntervalFlow(any()) } returns flowOf(VerifyPasswordInterval.NEVER)
            every { contactRepository.getContactCountFlow(any()) } returns flowOf(10)
            every { safeItemRepository.getSafeItemsCountFlow(any()) } returns flowOf(10)
            every { settingRepository.hasBackupSince(any(), any()) } returns flowOf(true)
            every { settingRepository.preventionWarningCtaState(any()) } returns flowOf(CtaState.DismissedAt(lastDismissAt))
            assertEquals(PreventionSettingsWarning.PasswordVerification, useCase().first())
        }
    }

    @Test
    fun prevention_warning_backup_and_biometry_test() {
        runTest {
            every { safeRepository.isBiometricEnabledForSafeFlow(any()) } returns flowOf(true)
            every { securitySettingsRepository.verifyPasswordIntervalFlow(any()) } returns flowOf(VerifyPasswordInterval.NEVER)
            every { contactRepository.getContactCountFlow(any()) } returns flowOf(10)
            every { safeItemRepository.getSafeItemsCountFlow(any()) } returns flowOf(10)
            every { settingRepository.hasBackupSince(any(), any()) } returns flowOf(false)
            every { settingRepository.preventionWarningCtaState(any()) } returns flowOf(CtaState.DismissedAt(lastDismissAt))
            assertEquals(PreventionSettingsWarning.PasswordVerificationAndBackup, useCase().first())
        }
    }

    @Test
    fun prevention_warning_backup_test() {
        runTest {
            every { safeRepository.isBiometricEnabledForSafeFlow(any()) } returns flowOf(false)
            every { securitySettingsRepository.verifyPasswordIntervalFlow(any()) } returns flowOf(VerifyPasswordInterval.NEVER)
            every { contactRepository.getContactCountFlow(any()) } returns flowOf(10)
            every { safeItemRepository.getSafeItemsCountFlow(any()) } returns flowOf(10)
            every { settingRepository.hasBackupSince(any(), any()) } returns flowOf(false)
            every { settingRepository.preventionWarningCtaState(any()) } returns flowOf(CtaState.DismissedAt(lastDismissAt))
            assertEquals(PreventionSettingsWarning.Backup, useCase().first())
        }
    }

    @Test
    fun prevention_no_warning_test() {
        runTest {
            every { safeRepository.isBiometricEnabledForSafeFlow(any()) } returns flowOf(true)
            every { securitySettingsRepository.verifyPasswordIntervalFlow(any()) } returns flowOf(VerifyPasswordInterval.EVERY_WEEK)
            every { contactRepository.getContactCountFlow(any()) } returns flowOf(10)
            every { safeItemRepository.getSafeItemsCountFlow(any()) } returns flowOf(10)
            every { settingRepository.hasBackupSince(any(), any()) } returns flowOf(true)
            every { settingRepository.preventionWarningCtaState(any()) } returns flowOf(CtaState.DismissedAt(lastDismissAt))
            assertEquals(null, useCase().first())
        }
    }

    @Test
    fun prevention_no_warning_empty_safe_test() {
        runTest {
            every { safeRepository.isBiometricEnabledForSafeFlow(any()) } returns flowOf(true)
            every { securitySettingsRepository.verifyPasswordIntervalFlow(any()) } returns flowOf(VerifyPasswordInterval.NEVER)
            every { contactRepository.getContactCountFlow(any()) } returns flowOf(0)
            every { safeItemRepository.getSafeItemsCountFlow(any()) } returns flowOf(0)
            every { settingRepository.hasBackupSince(any(), any()) } returns flowOf(false)
            every { settingRepository.preventionWarningCtaState(any()) } returns flowOf(CtaState.DismissedAt(lastDismissAt))
            assertEquals(null, useCase().first())
        }
    }
}
