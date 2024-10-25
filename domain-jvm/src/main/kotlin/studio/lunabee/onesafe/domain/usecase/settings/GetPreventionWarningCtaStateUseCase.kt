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
 * Created by Lunabee Studio / Date - 6/19/2024 - for the oneSafe6 SDK.
 * Last modified 6/19/24, 5:39 PM
 */

package studio.lunabee.onesafe.domain.usecase.settings

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.domain.Constant
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.domain.model.warning.PreventionSettingsWarning
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.repository.SafeSettingsRepository
import studio.lunabee.onesafe.domain.repository.SecuritySettingsRepository
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

/**
 * Get warning type to display. Warning type can be found in the following enum: [PreventionSettingsWarning]
 * To be displayed, following condition must be met:
 * - safe must not be empty (items and/or bubbles).
 * - safe must be older than 10 days.
 * - last displayed occurs more than 30 days ago.
 */
class GetPreventionWarningCtaStateUseCase @Inject constructor(
    private val settingRepository: SafeSettingsRepository,
    private val safeRepository: SafeRepository,
    private val securitySettingsRepository: SecuritySettingsRepository,
    private val contactRepository: ContactRepository,
    private val safeItemRepository: SafeItemRepository,
    private val clock: Clock,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<PreventionSettingsWarning?> {
        return safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
            safeId?.let {
                settingRepository.preventionWarningCtaState(safeId = safeId).flatMapLatest { preventionWarningCtaState ->
                    if (shouldDisplayCta(preventionWarningCtaState)) {
                        safeItemRepository.getSafeItemsCountFlow(safeId = safeId).flatMapLatest { itemCount ->
                            contactRepository.getContactCountFlow(DoubleRatchetUUID(safeId.id)).flatMapLatest { contactCount ->
                                if (itemCount > 0 || contactCount > 0) {
                                    checkSettingsConditions(safeId = safeId)
                                } else {
                                    flowOf(value = null)
                                }
                            }
                        }
                    } else {
                        flowOf(value = null)
                    }
                }
            } ?: flowOf(value = null)
        }
    }

    private fun checkSettingsConditions(safeId: SafeId): Flow<PreventionSettingsWarning?> {
        return combine(
            safeRepository.isBiometricEnabledForSafeFlow(safeId = safeId),
            securitySettingsRepository.verifyPasswordIntervalFlow(safeId = safeId),
            settingRepository.hasBackupSince(safeId = safeId, duration = Constant.PreventionWarningBackupAge),
        ) { isBiometricEnabled, verifyPasswordInterval, hasBackupSinceOneMonth ->
            val shouldWarnAboutPasswordVerification = isBiometricEnabled && verifyPasswordInterval == VerifyPasswordInterval.NEVER
            val shouldWarnAboutBackup = !hasBackupSinceOneMonth
            when {
                shouldWarnAboutBackup && shouldWarnAboutPasswordVerification -> PreventionSettingsWarning.PasswordVerificationAndBackup
                shouldWarnAboutPasswordVerification -> PreventionSettingsWarning.PasswordVerification
                shouldWarnAboutBackup -> PreventionSettingsWarning.Backup
                else -> null
            }
        }
    }

    private fun shouldDisplayCta(preventionWarningCtaState: CtaState?): Boolean {
        val lastDismissDate = (preventionWarningCtaState as? CtaState.DismissedAt)?.timestamp ?: return false
        val dateToCompare = lastDismissDate.plus(Constant.DelayBeforeShowingCtaState, Constant.DelayUnitPreventionWarningCtaState)
        return dateToCompare < Instant.now(clock)
    }
}
