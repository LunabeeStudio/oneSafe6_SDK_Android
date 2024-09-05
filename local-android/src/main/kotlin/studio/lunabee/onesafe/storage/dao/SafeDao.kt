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
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safe.BiometricCryptoMaterial
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.storage.model.RoomSafe
import studio.lunabee.onesafe.storage.model.RoomSafeCryptoUpdate

@Dao
abstract class SafeDao {

    @Transaction
    open suspend fun insert(roomSafe: RoomSafe) {
        getAllSafeIdByLastOpenDesc().forEach {
            incrementOrder(it)
        }
        doInsert(roomSafe)
    }

    @Update(entity = RoomSafe::class)
    abstract suspend fun updateCrypto(safeCrypto: RoomSafeCryptoUpdate)

    @Transaction
    open suspend fun delete(id: SafeId) {
        val safes = getAllOrderByLastOpenAsc()
        val safeToDelete = safes.first { it.id == id }
        doDelete(id)
        safes
            .asSequence()
            .filter { it.openOrder > safeToDelete.openOrder }
            .forEach {
                decrementOrder(it.id)
            }
    }

    @Transaction
    open suspend fun setLastOpen(id: SafeId) {
        val safes = getAllOrderByLastOpenDesc()
        val safeToSet = safes.first { it.id == id }
        setOpenOrder(id, -1)
        safes
            .asSequence()
            .filter { it.openOrder < safeToSet.openOrder }
            .forEach {
                incrementOrder(it.id)
            }
        setOpenOrder(id, 0)
    }

    @Query("SELECT * FROM Safe ORDER BY open_order ASC")
    abstract suspend fun getAllOrderByLastOpenAsc(): List<RoomSafe>

    @Query("SELECT * FROM Safe ORDER BY open_order DESC")
    protected abstract suspend fun getAllOrderByLastOpenDesc(): List<RoomSafe>

    @Query("SELECT crypto_enc_index_key FROM Safe WHERE id = :id")
    abstract suspend fun getIndexKey(id: SafeId): ByteArray?

    @Query("SELECT crypto_enc_bubbles_key FROM Safe WHERE id = :id")
    abstract suspend fun getBubblesKey(id: SafeId): ByteArray?

    @Query("SELECT crypto_enc_item_edition_key FROM Safe WHERE id = :id")
    abstract suspend fun getItemEditionKey(id: SafeId): ByteArray?

    @Query("SELECT COUNT(*) FROM Safe")
    abstract suspend fun countAll(): Int

    @Query("SELECT crypto_master_salt FROM Safe WHERE id = :id")
    abstract suspend fun getSalt(id: SafeId): ByteArray?

    @Query("SELECT id FROM Safe ORDER BY open_order ASC")
    abstract suspend fun getAllSafeIdByLastOpenAsc(): List<SafeId>

    @Query("SELECT id FROM Safe ORDER BY open_order DESC")
    protected abstract suspend fun getAllSafeIdByLastOpenDesc(): List<SafeId>

    @Query("UPDATE Safe SET version = :version WHERE id IS :safeId")
    abstract suspend fun setVersion(safeId: SafeId, version: Int)

    @Query("SELECT version FROM Safe WHERE id IS :safeId")
    abstract suspend fun getSafeVersion(safeId: SafeId): Int

    // See addUniqueBiometricKeyTrigger for biometric key uniqueness
    @Query("UPDATE Safe SET crypto_biometric_crypto_material = :cryptoMaterial WHERE id IS :safeId")
    abstract suspend fun setBiometricMaterial(safeId: SafeId, cryptoMaterial: BiometricCryptoMaterial)

    @Query("UPDATE Safe SET crypto_biometric_crypto_material = NULL")
    abstract suspend fun removeAllBiometricKeys()

    @Query("SELECT * FROM Safe WHERE crypto_biometric_crypto_material IS NOT NULL")
    abstract suspend fun getBiometricSafe(): RoomSafe?

    @Query("SELECT EXISTS(SELECT 1 FROM Safe WHERE crypto_biometric_crypto_material IS NOT NULL LIMIT 1)")
    abstract fun hasBiometricSafe(): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM Safe WHERE crypto_biometric_crypto_material IS NOT NULL AND :safeId = id LIMIT 1)")
    abstract fun isBiometricEnabledForSafeFlow(safeId: SafeId): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM Safe WHERE crypto_biometric_crypto_material IS NOT NULL AND :safeId = id LIMIT 1)")
    abstract suspend fun isBiometricEnabledForSafe(safeId: SafeId): Boolean

    @Query("UPDATE Safe SET open_order = open_order + 1 WHERE id = :id")
    protected abstract suspend fun incrementOrder(id: SafeId)

    @Query("UPDATE Safe SET open_order = open_order - 1 WHERE id = :id")
    protected abstract suspend fun decrementOrder(id: SafeId)

    @Insert
    protected abstract suspend fun doInsert(roomSafe: RoomSafe)

    @Query("UPDATE Safe SET open_order = :openOrder WHERE id = :id")
    protected abstract suspend fun setOpenOrder(id: SafeId, openOrder: Int)

    @Query("DELETE FROM Safe WHERE id = :id")
    protected abstract suspend fun doDelete(id: SafeId)
}
