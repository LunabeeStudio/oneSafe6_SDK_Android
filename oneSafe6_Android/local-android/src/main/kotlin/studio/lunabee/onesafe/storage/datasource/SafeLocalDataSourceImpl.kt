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
 * Last modified 6/6/24, 3:47 PM
 */

package studio.lunabee.onesafe.storage.datasource

import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safe.AppVisit
import studio.lunabee.onesafe.domain.model.safe.BiometricCryptoMaterial
import studio.lunabee.onesafe.domain.model.safe.SafeCrypto
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safe.SafeSettings
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.importexport.model.GoogleDriveSettings
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.repository.datasource.SafeLocalDataSource
import studio.lunabee.onesafe.storage.dao.SafeDao
import studio.lunabee.onesafe.storage.model.RoomSafe
import studio.lunabee.onesafe.storage.model.RoomSafeCryptoUpdate
import javax.inject.Inject

class SafeLocalDataSourceImpl @Inject constructor(
    private val safeDao: SafeDao,
) : SafeLocalDataSource {
    override suspend fun insertSafe(
        safeCrypto: SafeCrypto,
        safeSettings: SafeSettings,
        appVisit: AppVisit,
        driveSettings: GoogleDriveSettings,
    ) {
        val roomSafe = RoomSafe.fromDomain(
            safeCrypto = safeCrypto,
            safeSettings = safeSettings,
            appVisit = appVisit,
            driveSettings = driveSettings,
            openOrder = 0,
        )
        safeDao.insert(roomSafe)
    }

    override suspend fun deleteSafe(safeId: SafeId) {
        safeDao.delete(safeId)
    }

    override suspend fun getAllSafeCryptoOrderByLastOpenAsc(): List<SafeCrypto> = safeDao
        .getAllOrderByLastOpenAsc()
        .map {
            it.toSafeCrypto()
        }

    override suspend fun updateSafeCrypto(safeCrypto: SafeCrypto) {
        safeDao.updateCrypto(RoomSafeCryptoUpdate.fromSafeCrypto(safeCrypto))
    }

    override suspend fun getIndexKey(safeId: SafeId): ByteArray? = safeDao.getIndexKey(safeId)

    override suspend fun getItemEditionKey(safeId: SafeId): ByteArray? = safeDao.getItemEditionKey(safeId)

    override suspend fun getBubblesKey(safeId: SafeId): ByteArray? = safeDao.getBubblesKey(safeId)

    override suspend fun hasSafe(): Boolean = safeDao.countAll() > 0

    override suspend fun getSalt(safeId: SafeId): ByteArray = safeDao.getSalt(safeId) ?: throw OSStorageError.Code.NO_SALT_FOUND
        .get()

    override suspend fun getAllSafeId(): List<SafeId> = safeDao.getAllSafeIdByLastOpenAsc()

    override suspend fun setVersion(safeId: SafeId, version: Int) {
        safeDao.setVersion(safeId, version)
    }

    override suspend fun getSafeVersion(safeId: SafeId): Int = safeDao.getSafeVersion(safeId)

    override suspend fun setBiometricKey(safeId: SafeId, biometricCryptoMaterial: BiometricCryptoMaterial) = safeDao
        .setBiometricMaterial(
            safeId,
            biometricCryptoMaterial,
        )

    override suspend fun setAutoDestructionKey(safeId: SafeId, autoDestructionKey: ByteArray?) = safeDao
        .setAutoDestructionKey(
            safeId,
            autoDestructionKey,
        )

    override suspend fun removeAllBiometricKeys() = safeDao.removeAllBiometricKeys()

    override suspend fun getBiometricSafe(): SafeCrypto? = safeDao.getBiometricSafe()?.toSafeCrypto()

    override fun hasBiometricSafe(): Flow<Boolean> = safeDao.hasBiometricSafe()

    override fun isBiometricEnabledForSafeFlow(safeId: SafeId): Flow<Boolean> = safeDao
        .isBiometricEnabledForSafeFlow(safeId)

    override suspend fun isAutoDestructionEnabledForSafe(safeId: SafeId): Boolean = safeDao
        .isAutoDestructionEnabledForSafe(safeId)

    override suspend fun isBiometricEnabledForSafe(safeId: SafeId): Boolean = safeDao.isBiometricEnabledForSafe(safeId)

    override suspend fun setLastOpen(safeId: SafeId) {
        safeDao.setLastOpen(safeId)
    }

    override suspend fun getSafeCrypto(safeId: SafeId): SafeCrypto? = safeDao.getSafeCrypto(safeId)?.toSafeCrypto()

    override fun isAutoDestructionEnabledForSafeFlow(safeId: SafeId): Flow<Boolean> = safeDao
        .isAutoDestructionEnabledForSafeFlow(safeId)

    override fun isPanicDestructionEnabledFlow(safeId: SafeId): Flow<Boolean> = safeDao
        .isPanicDestructionEnabledFlow(safeId = safeId)

    override suspend fun setIsPanicDestructionEnabled(safeId: SafeId, isEnabled: Boolean) {
        safeDao.setIsPanicDestructionEnabled(safeId = safeId, isEnabled = isEnabled)
    }

    override suspend fun hasAnySafePanicWidgetEnabled(): Boolean = safeDao.hasAnySafePanicWidgetEnabled()

    override suspend fun getSafeToDestroy(): List<SafeId> = safeDao.getSafeToDestroy()
}
