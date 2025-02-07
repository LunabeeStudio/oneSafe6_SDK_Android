/*
 * Copyright (c) 2023-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 7/9/2024 - for the oneSafe6 SDK.
 * Last modified 7/9/24, 9:41 AM
 */

package studio.lunabee.onesafe.migration.migration

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.repository.ItemSettingsRepository
import studio.lunabee.onesafe.migration.MigrationSafeData0
import javax.inject.Inject

/**
 * Keep position index by default
 */
class MigrationFromV7ToV8 @Inject constructor(
    private val itemSettingsRepository: ItemSettingsRepository,
) : AppMigration0(7, 8) {
    override suspend fun migrate(migrationSafeData: MigrationSafeData0): LBResult<Unit> {
        val safeId = migrationSafeData.id
        itemSettingsRepository.setItemOrdering(safeId, ItemOrder.Position)
        return LBResult.Success(Unit)
    }
}
