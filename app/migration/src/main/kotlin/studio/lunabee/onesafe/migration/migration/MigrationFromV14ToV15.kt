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
 * Created by Lunabee Studio / Date - 7/31/2024 - for the oneSafe6 SDK.
 * Last modified 7/31/24, 9:28 AM
 */

package studio.lunabee.onesafe.migration.migration

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.domain.model.safe.SafeCrypto
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.migration.utils.MigrationSafeData
import javax.inject.Inject

// TODO <multisafe> unit test MigrationFromV14ToV15

private val logger = LBLogger.get<MigrationFromV14ToV15>()

/**
 * Generate keys to fill empty keys added in DB by [studio.lunabee.onesafe.storage.migration.RoomMigration12to13]
 */
class MigrationFromV14ToV15 @Inject constructor(
    private val mainCryptoRepository: MainCryptoRepository,
    private val safeRepository: SafeRepository,
) {
    suspend operator fun invoke(
        migrationSafeData: MigrationSafeData,
    ): LBResult<Unit> = OSError.runCatching(logger) {
        val newCrypto = mainCryptoRepository.generateCrypto(
            key = migrationSafeData.masterKey,
            salt = migrationSafeData.salt,
            biometricCipher = null,
        )
        val mergedCrypto = SafeCrypto(
            id = migrationSafeData.id,
            salt = migrationSafeData.salt,
            encTest = migrationSafeData.encTest,
            encIndexKey = migrationSafeData.encIndexKey ?: newCrypto.encIndexKey,
            encBubblesKey = migrationSafeData.encBubblesKey ?: newCrypto.encBubblesKey,
            encItemEditionKey = migrationSafeData.encItemEditionKey ?: newCrypto.encItemEditionKey,
            biometricCryptoMaterial = migrationSafeData.biometricCryptoMaterial,
        )
        safeRepository.updateSafeCrypto(mergedCrypto)
    }
}
