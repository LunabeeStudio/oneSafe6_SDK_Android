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
 * Last modified 1/16/24, 10:57 AM
 */

package studio.lunabee.onesafe.importexport.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Check the backup cta state and observe items + backup settings to update the cta state if needed
 *
 * @see <a href="https://www.notion.so/lunabeestudio/Automatic-backup-96a991d57b6a40379acbab62e7c65efb?pvs=
 * 4#af824b85b7b44fa580b2d233fdd6cff8">Notion spec</a>
 */
class GetEnableAutoBackupCtaStateUseCase @Inject constructor(
    private val autoBackupSettingsRepository: AutoBackupSettingsRepository,
    private val itemRepository: SafeItemRepository,
    private val clock: Clock,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<CtaState> {
        return autoBackupSettingsRepository.enableAutoBackupCtaState.transformLatest { currentState ->
            emit(currentState)
            when (currentState) {
                is CtaState.DismissedAt -> {} // keep dismiss
                CtaState.Hidden,
                is CtaState.VisibleSince,
                -> { // observe new value and update on value change
                    val hasItemFlow = itemRepository.getSafeItemsCountFlow()
                        .map { count -> count >= MinItemBeforeCta }
                        .distinctUntilChanged()
                    combine(
                        autoBackupSettingsRepository.autoBackupEnabled,
                        hasItemFlow,
                    ) { isBackupEnabled, hasItem ->
                        val newState = if (!isBackupEnabled && hasItem) {
                            CtaState.VisibleSince(Instant.now(clock).plus(DelayBeforeCtaDays, ChronoUnit.DAYS))
                        } else {
                            CtaState.Hidden
                        }
                        if (newState::class != currentState::class) {
                            autoBackupSettingsRepository.setEnableAutoBackupCtaState(newState)
                        }
                    }.collect()
                }
            }
        }
    }

    companion object {
        private const val MinItemBeforeCta: Int = 1
        private const val DelayBeforeCtaDays: Long = 1L
    }
}
