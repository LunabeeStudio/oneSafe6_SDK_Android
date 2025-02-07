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
 * Last modified 6/6/24, 3:33 PM
 */

package studio.lunabee.onesafe.repository.repository

import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.crypto.SubKeyType
import studio.lunabee.onesafe.domain.model.safe.AppVisit
import studio.lunabee.onesafe.domain.model.safe.BiometricCryptoMaterial
import studio.lunabee.onesafe.domain.model.safe.SafeCrypto
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safe.SafeSettings
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.error.OSRepositoryError
import studio.lunabee.onesafe.importexport.model.GoogleDriveSettings
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.repository.datasource.SafeIdCacheDataSource
import studio.lunabee.onesafe.repository.datasource.SafeLocalDataSource
import javax.inject.Inject
import kotlin.time.Duration

class SafeRepositoryImpl @Inject constructor(
    private val cacheDataSource: SafeIdCacheDataSource,
    private val localDataSource: SafeLocalDataSource,
) : SafeRepository {
    override suspend fun loadSafeId(safeId: SafeId) {
        cacheDataSource.loadSafeId(safeId)
    }

    override suspend fun clearSafeId() {
        cacheDataSource.clearSafeId()
    }

    override suspend fun getSafeVersion(safeId: SafeId): Int {
        return localDataSource.getSafeVersion(safeId)
    }

    override suspend fun setSafeVersion(safeId: SafeId, version: Int) {
        localDataSource.setVersion(safeId, version)
    }

    override suspend fun getAllSafeOrderByLastOpenAsc(): List<SafeCrypto> {
        return localDataSource.getAllSafeCryptoOrderByLastOpenAsc()
    }

    override suspend fun insertSafe(
        safeCrypto: SafeCrypto,
        safeSettings: SafeSettings,
        appVisit: AppVisit,
    ) {
        val driveSettings = GoogleDriveSettings(null, null, null)
        localDataSource.insertSafe(
            safeCrypto,
            safeSettings,
            appVisit,
            driveSettings,
        )
    }

    override suspend fun updateSafeCrypto(safeCrypto: SafeCrypto) {
        localDataSource.updateSafeCrypto(safeCrypto)
    }

    override fun currentSafeIdFlow(): Flow<SafeId?> {
        return cacheDataSource.getSafeIdFlow()
    }

    override suspend fun getCurrentSubKey(subKeyType: SubKeyType): ByteArray? {
        val safeId = currentSafeId()
        return when (subKeyType) {
            SubKeyType.SearchIndex -> localDataSource.getIndexKey(safeId)
            SubKeyType.ItemEdition -> localDataSource.getItemEditionKey(safeId)
            SubKeyType.Bubbles -> localDataSource.getBubblesKey(safeId)
        }
    }

    override suspend fun hasSafe(): Boolean {
        return localDataSource.hasSafe()
    }

    override suspend fun getSalt(safeId: SafeId): ByteArray {
        return localDataSource.getSalt(safeId)
    }

    override suspend fun getCurrentSalt(): ByteArray {
        return getSalt(currentSafeId())
    }

    override suspend fun currentSafeId(): SafeId {
        return cacheDataSource.getSafeId()
            ?: throw OSRepositoryError.Code.SAFE_ID_NOT_LOADED.get()
    }

    override fun lastSafeIdLoaded(): SafeId? {
        return cacheDataSource.getLastSafeId()
    }

    override suspend fun currentSafeIdOrNull(): SafeId? {
        return cacheDataSource.getSafeId()
    }

    override suspend fun getAllSafeId(): List<SafeId> {
        return localDataSource.getAllSafeId()
    }

    override suspend fun deleteSafe(safeId: SafeId) {
        localDataSource.deleteSafe(safeId)
    }

    override suspend fun isSafeIdInMemory(timeout: Duration): Boolean {
        return cacheDataSource.getSafeId() != null
    }

    override suspend fun setBiometricMaterial(safeId: SafeId, biometricCryptoMaterial: BiometricCryptoMaterial) {
        localDataSource.setBiometricKey(safeId, biometricCryptoMaterial)
    }

    override suspend fun setAutoDestructionKey(safeId: SafeId, autoDestructionKey: ByteArray?) {
        localDataSource.setAutoDestructionKey(safeId, autoDestructionKey)
    }

    override suspend fun removeBiometricKey() {
        localDataSource.removeAllBiometricKeys()
    }

    override suspend fun getBiometricSafe(): SafeCrypto {
        return localDataSource.getBiometricSafe() ?: throw OSRepositoryError.Code.NO_BIOMETRIC_SAFE_FOUND.get()
    }

    override fun hasBiometricSafe(): Flow<Boolean> {
        return localDataSource.hasBiometricSafe()
    }

    override fun isBiometricEnabledForSafeFlow(safeId: SafeId): Flow<Boolean> {
        return localDataSource.isBiometricEnabledForSafeFlow(safeId)
    }

    override suspend fun isAutoDestructionEnabledForSafe(safeId: SafeId): Boolean {
        return localDataSource.isAutoDestructionEnabledForSafe(safeId)
    }

    override fun isAutoDestructionEnabledForSafeFlow(safeId: SafeId): Flow<Boolean> {
        return localDataSource.isAutoDestructionEnabledForSafeFlow(safeId)
    }

    override suspend fun isBiometricEnabledForSafe(safeId: SafeId): Boolean {
        return localDataSource.isBiometricEnabledForSafe(safeId)
    }

    override suspend fun setLastOpen(safeId: SafeId) {
        localDataSource.setLastOpen(safeId)
    }

    override suspend fun getSafeCrypto(safeId: SafeId): SafeCrypto? {
        return localDataSource.getSafeCrypto(safeId)
    }

    override fun isPanicDestructionEnabledFlow(safeId: SafeId): Flow<Boolean> {
        return localDataSource.isPanicDestructionEnabledFlow(safeId)
    }

    override suspend fun setIsPanicDestructionEnabled(safeId: SafeId, isEnabled: Boolean) {
        return localDataSource.setIsPanicDestructionEnabled(safeId, isEnabled)
    }

    override suspend fun hasAnySafePanicWidgetEnabled(): Boolean {
        return localDataSource.hasAnySafePanicWidgetEnabled()
    }

    override suspend fun getSafeToDestroy(): List<SafeId> {
        return localDataSource.getSafeToDestroy()
    }
}
