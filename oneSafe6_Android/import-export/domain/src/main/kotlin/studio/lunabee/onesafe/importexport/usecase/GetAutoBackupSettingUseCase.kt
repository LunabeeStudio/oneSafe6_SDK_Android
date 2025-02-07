/*
 * Copyright (c) 2024-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 6/20/2024 - for the oneSafe6 SDK.
 * Last modified 6/20/24, 8:36 AM
 */

package studio.lunabee.onesafe.importexport.usecase

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import javax.inject.Inject
import kotlin.time.Duration

private val logger = LBLogger.get<GetAutoBackupSettingUseCase>()

class GetAutoBackupSettingUseCase @Inject constructor(
    private val autoBackupSettingsRepository: AutoBackupSettingsRepository,
    private val safeRepository: SafeRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun autoBackupEnabled(currentSafeId: SafeId? = null): Flow<Boolean> =
        currentSafeId?.let { safeId ->
            autoBackupSettingsRepository.autoBackupEnabledFlow(safeId)
        } ?: safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
            safeId?.let {
                autoBackupSettingsRepository.autoBackupEnabledFlow(safeId)
            } ?: flowOf()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun autoBackupFrequencyFlow(currentSafeId: SafeId? = null): Flow<Duration> =
        currentSafeId?.let { safeId ->
            autoBackupSettingsRepository.autoBackupFrequencyFlow(safeId)
        } ?: safeRepository.currentSafeIdFlow()
            .flatMapLatest { safeId ->
                safeId?.let {
                    autoBackupSettingsRepository.autoBackupFrequencyFlow(safeId)
                } ?: flowOf()
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun autoBackupMaxNumberFlow(currentSafeId: SafeId? = null): Flow<Int> =
        currentSafeId?.let { safeId ->
            autoBackupSettingsRepository.autoBackupMaxNumberFlow(safeId)
        } ?: safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
            safeId?.let {
                autoBackupSettingsRepository.autoBackupMaxNumberFlow(safeId)
            } ?: flowOf()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun cloudBackupEnabled(currentSafeId: SafeId? = null): Flow<Boolean> =
        currentSafeId?.let { safeId ->
            autoBackupSettingsRepository.cloudBackupEnabled(safeId)
        } ?: safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
            safeId?.let {
                autoBackupSettingsRepository.cloudBackupEnabled(safeId)
            } ?: flowOf()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun keepLocalBackupEnabled(currentSafeId: SafeId? = null): Flow<Boolean> =
        currentSafeId?.let { safeId ->
            autoBackupSettingsRepository.keepLocalBackupEnabled(safeId)
        } ?: safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
            safeId?.let {
                autoBackupSettingsRepository.keepLocalBackupEnabled(safeId)
            } ?: flowOf()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun enableAutoBackupCtaState(currentSafeId: SafeId? = null): Flow<CtaState> =
        currentSafeId?.let { safeId ->
            autoBackupSettingsRepository.enableAutoBackupCtaState(safeId)
        } ?: safeRepository.currentSafeIdFlow()
            .flatMapLatest { safeId ->
                safeId?.let {
                    autoBackupSettingsRepository.enableAutoBackupCtaState(safeId)
                } ?: flowOf()
            }

    suspend fun autoBackupFrequency(): LBResult<Duration> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        autoBackupSettingsRepository.autoBackupFrequency(safeId)
    }

    suspend fun autoBackupMaxNumber(): LBResult<Int> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        autoBackupSettingsRepository.autoBackupMaxNumber(safeId)
    }
}
