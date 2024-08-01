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
 * Last modified 6/6/24, 3:45 PM
 */

package studio.lunabee.onesafe.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safe.BiometricCryptoMaterial
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.storage.model.RoomSafe
import studio.lunabee.onesafe.storage.model.RoomSafeCryptoUpdate

@Dao
interface SafeDao {
    @Insert
    suspend fun insert(roomSafe: RoomSafe)

    @Update(entity = RoomSafe::class)
    suspend fun updateCrypto(safeCrypto: RoomSafeCryptoUpdate)

    @Query("DELETE FROM Safe WHERE id = :id")
    suspend fun delete(id: SafeId)

    @Query("SELECT * FROM Safe")
    suspend fun getAll(): List<RoomSafe>

    @Query("SELECT crypto_enc_index_key FROM Safe WHERE id = :id")
    suspend fun getIndexKey(id: SafeId): ByteArray?

    @Query("SELECT crypto_enc_bubbles_key FROM Safe WHERE id = :id")
    suspend fun getBubblesKey(id: SafeId): ByteArray?

    @Query("SELECT crypto_enc_item_edition_key FROM Safe WHERE id = :id")
    suspend fun getItemEditionKey(id: SafeId): ByteArray?

    @Query("SELECT COUNT(*) FROM Safe")
    suspend fun countAll(): Int

    @Query("SELECT crypto_master_salt FROM Safe WHERE id = :id")
    suspend fun getSalt(id: SafeId): ByteArray?

    @Query("SELECT id FROM Safe")
    suspend fun getAllSafeId(): List<SafeId>

    @Query("UPDATE Safe SET version = :version WHERE id IS :safeId")
    suspend fun setVersion(safeId: SafeId, version: Int)

    @Query("SELECT version FROM Safe WHERE id IS :safeId")
    suspend fun getSafeVersion(safeId: SafeId): Int

    // See addUniqueBiometricKeyTrigger for biometric key uniqueness
    @Query("UPDATE Safe SET crypto_biometric_crypto_material = :cryptoMaterial WHERE id IS :safeId")
    suspend fun setBiometricMaterial(safeId: SafeId, cryptoMaterial: BiometricCryptoMaterial)

    @Query("UPDATE Safe SET crypto_biometric_crypto_material = NULL")
    suspend fun removeAllBiometricKeys()

    @Query("SELECT * FROM Safe WHERE crypto_biometric_crypto_material IS NOT NULL")
    suspend fun getBiometricSafe(): RoomSafe?

    @Query("SELECT EXISTS(SELECT 1 FROM Safe WHERE crypto_biometric_crypto_material IS NOT NULL LIMIT 1)")
    fun hasBiometricSafe(): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM Safe WHERE crypto_biometric_crypto_material IS NOT NULL AND :safeId = id LIMIT 1)")
    fun isBiometricEnabledForSafeFlow(safeId: SafeId): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM Safe WHERE crypto_biometric_crypto_material IS NOT NULL AND :safeId = id LIMIT 1)")
    suspend fun isBiometricEnabledForSafe(safeId: SafeId): Boolean
}
