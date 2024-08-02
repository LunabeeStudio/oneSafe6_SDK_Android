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
 * Created by Lunabee Studio / Date - 6/24/2024 - for the oneSafe6 SDK.
 * Last modified 6/24/24, 9:00 AM
 */

package studio.lunabee.importexport.datasource

import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.importexport.model.SafeAutoBackupEnabled
import kotlin.time.Duration

interface AutoBackupSettingsDataSource {
    fun autoBackupEnabledFlow(safeId: SafeId): Flow<Boolean?>
    fun autoBackupFrequencyFlow(safeId: SafeId): Flow<Duration?>
    fun autoBackupMaxNumberFlow(safeId: SafeId): Flow<Int?>
    fun cloudBackupEnabled(safeId: SafeId): Flow<Boolean?>
    fun keepLocalBackupEnabled(safeId: SafeId): Flow<Boolean?>
    fun enableAutoBackupCtaState(safeId: SafeId): Flow<CtaState?>

    suspend fun autoBackupEnabled(safeId: SafeId): Boolean
    suspend fun autoBackupFrequency(safeId: SafeId): Duration
    suspend fun autoBackupMaxNumber(safeId: SafeId): Int
    suspend fun getSafeAutoBackupEnabled(): List<SafeAutoBackupEnabled>

    suspend fun toggleAutoBackupSettings(safeId: SafeId)
    suspend fun setAutoBackupFrequency(safeId: SafeId, frequency: Duration)
    suspend fun updateAutoBackupMaxNumber(safeId: SafeId, updatedValue: Int)
    suspend fun setCloudBackupEnabled(safeId: SafeId, enabled: Boolean)
    suspend fun setKeepLocalBackupSettings(safeId: SafeId, enabled: Boolean)
    suspend fun setEnableAutoBackupCtaState(safeId: SafeId, ctaState: CtaState)
}
