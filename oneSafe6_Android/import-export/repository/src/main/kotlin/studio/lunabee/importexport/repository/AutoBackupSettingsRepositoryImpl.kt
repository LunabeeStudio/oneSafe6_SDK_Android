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
 * Last modified 6/24/24, 8:58 AM
 */

package studio.lunabee.importexport.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import studio.lunabee.importexport.datasource.AutoBackupSettingsDataSource
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.importexport.model.SafeAutoBackupEnabled
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import javax.inject.Inject
import kotlin.time.Duration

class AutoBackupSettingsRepositoryImpl @Inject constructor(
    private val dataSource: AutoBackupSettingsDataSource,
) : AutoBackupSettingsRepository {
    override fun autoBackupEnabledFlow(safeId: SafeId): Flow<Boolean> =
        dataSource.autoBackupEnabledFlow(safeId).filterNotNull()

    override fun autoBackupFrequencyFlow(safeId: SafeId): Flow<Duration> =
        dataSource.autoBackupFrequencyFlow(safeId).filterNotNull()

    override fun autoBackupMaxNumberFlow(safeId: SafeId): Flow<Int> =
        dataSource.autoBackupMaxNumberFlow(safeId).filterNotNull()

    override fun cloudBackupEnabled(safeId: SafeId): Flow<Boolean> =
        dataSource.cloudBackupEnabled(safeId).filterNotNull()

    override fun keepLocalBackupEnabled(safeId: SafeId): Flow<Boolean> =
        dataSource.keepLocalBackupEnabled(safeId).filterNotNull()

    override fun enableAutoBackupCtaState(safeId: SafeId): Flow<CtaState> =
        dataSource.enableAutoBackupCtaState(safeId).filterNotNull()

    override suspend fun getSafeAutoBackupEnabled(): List<SafeAutoBackupEnabled> {
        return dataSource.getSafeAutoBackupEnabled()
    }

    override suspend fun autoBackupFrequency(safeId: SafeId): Duration =
        dataSource.autoBackupFrequency(safeId)

    override suspend fun autoBackupMaxNumber(safeId: SafeId): Int =
        dataSource.autoBackupMaxNumber(safeId)

    override suspend fun toggleAutoBackupSettings(safeId: SafeId): Boolean {
        dataSource.toggleAutoBackupSettings(safeId)
        return dataSource.autoBackupEnabled(safeId)
    }

    override suspend fun setAutoBackupFrequency(safeId: SafeId, delay: Duration) {
        dataSource.setAutoBackupFrequency(safeId, delay)
    }

    override suspend fun updateAutoBackupMaxNumber(safeId: SafeId, updatedValue: Int) {
        dataSource.updateAutoBackupMaxNumber(safeId, updatedValue)
    }

    override suspend fun setCloudBackupEnabled(safeId: SafeId, enabled: Boolean) {
        dataSource.setCloudBackupEnabled(safeId, enabled)
    }

    override suspend fun setKeepLocalBackupSettings(safeId: SafeId, enabled: Boolean) {
        dataSource.setKeepLocalBackupSettings(safeId, enabled)
    }

    override suspend fun setEnableAutoBackupCtaState(safeId: SafeId, ctaState: CtaState) {
        dataSource.setEnableAutoBackupCtaState(safeId, ctaState)
    }
}
