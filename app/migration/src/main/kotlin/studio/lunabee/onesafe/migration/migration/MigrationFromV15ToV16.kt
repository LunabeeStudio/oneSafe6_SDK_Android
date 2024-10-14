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
 * Created by Lunabee Studio / Date - 10/3/2024 - for the oneSafe6 SDK.
 * Last modified 10/3/24, 9:40 AM
 */

package studio.lunabee.onesafe.migration.migration

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.domain.model.safe.SafeCrypto
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.migration.MigrationSafeData15
import javax.inject.Inject

private val logger = LBLogger.get<MigrationFromV15ToV16>()

/**
 * Generate key to fill empty bubbles key added in DB by [studio.lunabee.onesafe.storage.migration.RoomMigration19to20]
 */
class MigrationFromV15ToV16 @Inject constructor(
    private val mainCryptoRepository: MainCryptoRepository,
    private val safeRepository: SafeRepository,
) : AppMigration15(15, 16) {
    override suspend fun migrate(migrationSafeData: MigrationSafeData15): LBResult<Unit> = OSError.runCatching(logger) {
        if (migrationSafeData.encBubblesKey == null) {
            val newCrypto = mainCryptoRepository.generateCrypto(
                key = migrationSafeData.masterKey,
                salt = migrationSafeData.salt,
                biometricCipher = null,
            )

            val mergedCrypto = SafeCrypto(
                id = migrationSafeData.id,
                salt = migrationSafeData.salt,
                encTest = migrationSafeData.encTest,
                encIndexKey = migrationSafeData.encIndexKey,
                encBubblesKey = newCrypto.encBubblesKey,
                encItemEditionKey = migrationSafeData.encItemEditionKey,
                biometricCryptoMaterial = migrationSafeData.biometricCryptoMaterial,
                autoDestructionKey = null,
            )

            safeRepository.updateSafeCrypto(mergedCrypto)
        }
    }
}
