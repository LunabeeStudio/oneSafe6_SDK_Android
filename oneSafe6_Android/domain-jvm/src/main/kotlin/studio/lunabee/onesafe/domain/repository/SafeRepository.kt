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
 * Last modified 6/6/24, 3:29 PM
 */

package studio.lunabee.onesafe.domain.repository

import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.crypto.SubKeyType
import studio.lunabee.onesafe.domain.model.safe.AppVisit
import studio.lunabee.onesafe.domain.model.safe.BiometricCryptoMaterial
import studio.lunabee.onesafe.domain.model.safe.SafeCrypto
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safe.SafeSettings
import kotlin.time.Duration

interface SafeRepository {
    suspend fun loadSafeId(safeId: SafeId)
    suspend fun currentSafeId(): SafeId
    fun lastSafeIdLoaded(): SafeId?
    suspend fun currentSafeIdOrNull(): SafeId?
    suspend fun getAllSafeId(): List<SafeId>
    suspend fun getAllSafeOrderByLastOpenAsc(): List<SafeCrypto>
    suspend fun insertSafe(
        safeCrypto: SafeCrypto,
        safeSettings: SafeSettings,
        appVisit: AppVisit,
    )

    suspend fun updateSafeCrypto(safeCrypto: SafeCrypto)
    suspend fun deleteSafe(safeId: SafeId)

    fun currentSafeIdFlow(): Flow<SafeId?>

    suspend fun getSubKey(safeId: SafeId, subKeyType: SubKeyType): ByteArray?
    suspend fun hasSafe(): Boolean
    suspend fun getSalt(safeId: SafeId): ByteArray
    suspend fun getCurrentSalt(): ByteArray
    suspend fun clearSafeId()
    suspend fun getSafeVersion(safeId: SafeId): Int?
    suspend fun setSafeVersion(safeId: SafeId, version: Int)

    suspend fun isSafeIdInMemory(timeout: Duration): Boolean

    /**
     * Set the unique biometric material (remove any other biometric material set to other safes)
     */
    suspend fun setBiometricMaterial(safeId: SafeId, biometricCryptoMaterial: BiometricCryptoMaterial)
    suspend fun setAutoDestructionKey(safeId: SafeId, autoDestructionKey: ByteArray?)
    suspend fun removeBiometricKey()
    suspend fun getBiometricSafe(): SafeCrypto
    fun hasBiometricSafe(): Flow<Boolean>
    fun isBiometricEnabledForSafeFlow(safeId: SafeId): Flow<Boolean>
    suspend fun isAutoDestructionEnabledForSafe(safeId: SafeId): Boolean
    fun isAutoDestructionEnabledForSafeFlow(safeId: SafeId): Flow<Boolean>
    suspend fun isBiometricEnabledForSafe(safeId: SafeId): Boolean
    suspend fun setLastOpen(safeId: SafeId)
    suspend fun getSafeCrypto(safeId: SafeId): SafeCrypto?
    fun isPanicDestructionEnabledFlow(safeId: SafeId): Flow<Boolean>
    suspend fun setIsPanicDestructionEnabled(safeId: SafeId, isEnabled: Boolean)
    suspend fun hasAnySafePanicWidgetEnabled(): Boolean
    suspend fun getSafeToDestroy(): List<SafeId>
}
