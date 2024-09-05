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
 * Created by Lunabee Studio / Date - 7/2/2024 - for the oneSafe6 SDK.
 * Last modified 6/28/24, 4:55 PM
 */

package studio.lunabee.onesafe.test

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.onesafe.domain.model.crypto.SubKeyType
import studio.lunabee.onesafe.domain.model.safe.AppVisit
import studio.lunabee.onesafe.domain.model.safe.BiometricCryptoMaterial
import studio.lunabee.onesafe.domain.model.safe.SafeCrypto
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safe.SafeSettings
import studio.lunabee.onesafe.domain.repository.SafeRepository
import kotlin.time.Duration

class DummySafeRepository(initSafeId: SafeId? = firstSafeId) : SafeRepository {
    private val safeIdFlow: MutableStateFlow<SafeId?> = MutableStateFlow(initSafeId)
    private var lastSafeId: SafeId? = initSafeId
    private val bioMap: MutableMap<SafeId, BiometricCryptoMaterial?> = mutableMapOf()

    override suspend fun loadSafeId(safeId: SafeId) {
        safeIdFlow.value = safeId
        lastSafeId = safeId
    }

    override suspend fun currentSafeId(): SafeId {
        return safeIdFlow.value!!
    }

    override fun lastSafeIdLoaded(): SafeId? {
        return lastSafeId
    }

    override suspend fun currentSafeIdOrNull(): SafeId? {
        return safeIdFlow.value
    }

    override suspend fun getAllSafeId(): List<SafeId> {
        return listOfNotNull(safeIdFlow.value)
    }

    override suspend fun getAllSafeOrderByLastOpenAsc(): List<SafeCrypto> {
        return listOfNotNull(
            safeIdFlow.value?.let {
                SafeCrypto(
                    it,
                    byteArrayOf(),
                    byteArrayOf(),
                    byteArrayOf(),
                    byteArrayOf(),
                    byteArrayOf(),
                    null,
                )
            },
        )
    }

    override suspend fun insertSafe(safeCrypto: SafeCrypto, safeSettings: SafeSettings, appVisit: AppVisit) {
    }

    override suspend fun updateSafeCrypto(safeCrypto: SafeCrypto) {
    }

    override suspend fun deleteSafe(safeId: SafeId) {
    }

    override fun currentSafeIdFlow(): Flow<SafeId?> {
        return safeIdFlow
    }

    override suspend fun getCurrentSubKey(subKeyType: SubKeyType): ByteArray? {
        return null
    }

    override suspend fun hasSafe(): Boolean {
        return safeIdFlow.value != null
    }

    override suspend fun getSalt(safeId: SafeId): ByteArray {
        return byteArrayOf()
    }

    override suspend fun getCurrentSalt(): ByteArray {
        return byteArrayOf()
    }

    override suspend fun clearSafeId() {
        safeIdFlow.value = null
    }

    override suspend fun getSafeVersion(safeId: SafeId): Int? {
        return null
    }

    override suspend fun setSafeVersion(safeId: SafeId, version: Int) {
    }

    override suspend fun isSafeIdInMemory(timeout: Duration): Boolean {
        return safeIdFlow.value != null
    }

    override suspend fun setBiometricMaterial(safeId: SafeId, biometricCryptoMaterial: BiometricCryptoMaterial) {
        bioMap.clear()
        bioMap[safeId] = biometricCryptoMaterial
    }

    override suspend fun removeBiometricKey() {
        bioMap.clear()
    }

    override suspend fun getBiometricSafe(): SafeCrypto {
        return SafeCrypto(
            id = bioMap.keys.first(),
            salt = byteArrayOf(),
            encTest = byteArrayOf(),
            encIndexKey = byteArrayOf(),
            encBubblesKey = byteArrayOf(),
            encItemEditionKey = byteArrayOf(),
            biometricCryptoMaterial = BiometricCryptoMaterial(ByteArray(16), ByteArray(32)),
        )
    }

    override fun hasBiometricSafe(): Flow<Boolean> {
        return flowOf(bioMap.isNotEmpty())
    }

    override fun isBiometricEnabledForSafeFlow(safeId: SafeId): Flow<Boolean> {
        return flowOf(bioMap.containsKey(safeId))
    }

    override suspend fun isBiometricEnabledForSafe(safeId: SafeId): Boolean {
        return bioMap.containsKey(safeId)
    }

    override suspend fun setLastOpen(safeId: SafeId) {
        /* no-op */
    }
}
