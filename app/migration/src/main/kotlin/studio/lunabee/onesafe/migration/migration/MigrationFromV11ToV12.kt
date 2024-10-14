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
 * Created by Lunabee Studio / Date - 7/9/2024 - for the oneSafe6 SDK.
 * Last modified 7/8/24, 4:58 PM
 */

package studio.lunabee.onesafe.migration.migration

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.domain.Constant
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.jvm.toByteArray
import studio.lunabee.onesafe.migration.MigrationSafeData0
import studio.lunabee.onesafe.migration.utils.MigrationCryptoV1UseCase
import javax.inject.Inject

private val logger = LBLogger.get<MigrationFromV11ToV12>()

/**
 * Add encThumbnailName placeholder on all fields
 */
class MigrationFromV11ToV12 @Inject constructor(
    private val safeItemFieldRepository: SafeItemFieldRepository,
    private val migrationCryptoV1UseCase: MigrationCryptoV1UseCase,
    private val safeItemKeyRepository: SafeItemKeyRepository,
) : AppMigration0(11, 12) {
    override suspend fun migrate(migrationSafeData: MigrationSafeData0): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = migrationSafeData.id
        val masterKey = migrationSafeData.masterKey
        val allFields = safeItemFieldRepository.getAllSafeItemFields(safeId)
        allFields.forEach { field ->
            val key = safeItemKeyRepository.getSafeItemKey(field.itemId)
            val plainKey = migrationCryptoV1UseCase.decrypt(key.encValue, masterKey)
            val encThumbnailFileName = migrationCryptoV1UseCase.encrypt(Constant.ThumbnailPlaceHolderName.toByteArray(), plainKey)
            safeItemFieldRepository.saveThumbnailFileName(field.id, encThumbnailFileName)
        }
        logger.i("Added encThumbnailName placeholders on ${allFields.size} fields")
    }
}
