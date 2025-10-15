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
 * Created by Lunabee Studio / Date - 6/6/2024 - for the oneSafe6 SDK.
 * Last modified 6/6/24, 3:37 PM
 */

package studio.lunabee.onesafe.repository.datasource

import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safe.AppVisit
import studio.lunabee.onesafe.domain.model.safe.BiometricCryptoMaterial
import studio.lunabee.onesafe.domain.model.safe.SafeCrypto
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safe.SafeSettings
import studio.lunabee.onesafe.importexport.model.GoogleDriveSettings

interface SafeLocalDataSource {
    suspend fun insertSafe(
        safeCrypto: SafeCrypto,
        safeSettings: SafeSettings,
        appVisit: AppVisit,
        driveSettings: GoogleDriveSettings,
    )

    suspend fun deleteSafe(safeId: SafeId)

    suspend fun getAllSafeCryptoOrderByLastOpenAsc(): List<SafeCrypto>

    suspend fun updateSafeCrypto(safeCrypto: SafeCrypto)

    suspend fun getIndexKey(safeId: SafeId): ByteArray?

    suspend fun getItemEditionKey(safeId: SafeId): ByteArray?

    suspend fun getBubblesKey(safeId: SafeId): ByteArray?

    suspend fun hasSafe(): Boolean

    suspend fun getSalt(safeId: SafeId): ByteArray

    suspend fun getAllSafeId(): List<SafeId>

    suspend fun setVersion(safeId: SafeId, version: Int)

    suspend fun getSafeVersion(safeId: SafeId): Int

    suspend fun setBiometricKey(safeId: SafeId, biometricCryptoMaterial: BiometricCryptoMaterial)

    suspend fun setAutoDestructionKey(safeId: SafeId, autoDestructionKey: ByteArray?)

    suspend fun removeAllBiometricKeys()

    suspend fun getBiometricSafe(): SafeCrypto?

    fun hasBiometricSafe(): Flow<Boolean>

    fun isBiometricEnabledForSafeFlow(safeId: SafeId): Flow<Boolean>

    suspend fun isAutoDestructionEnabledForSafe(safeId: SafeId): Boolean

    suspend fun isBiometricEnabledForSafe(safeId: SafeId): Boolean

    suspend fun setLastOpen(safeId: SafeId)

    suspend fun getSafeCrypto(safeId: SafeId): SafeCrypto?

    fun isAutoDestructionEnabledForSafeFlow(safeId: SafeId): Flow<Boolean>

    fun isPanicDestructionEnabledFlow(safeId: SafeId): Flow<Boolean>

    suspend fun setIsPanicDestructionEnabled(safeId: SafeId, isEnabled: Boolean)

    suspend fun hasAnySafePanicWidgetEnabled(): Boolean

    suspend fun getSafeToDestroy(): List<SafeId>
}
