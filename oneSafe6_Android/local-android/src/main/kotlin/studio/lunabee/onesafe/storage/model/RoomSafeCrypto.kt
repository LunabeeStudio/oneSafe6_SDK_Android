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
 * Created by Lunabee Studio / Date - 6/19/2024 - for the oneSafe6 SDK.
 * Last modified 6/19/24, 1:44 PM
 */

package studio.lunabee.onesafe.storage.model

import androidx.room.ColumnInfo
import studio.lunabee.onesafe.domain.model.safe.BiometricCryptoMaterial
import studio.lunabee.onesafe.domain.model.safe.SafeCrypto

data class RoomSafeCrypto(
    @ColumnInfo(name = "master_salt")
    val salt: ByteArray,
    @ColumnInfo(name = "enc_test")
    val encTest: ByteArray,
    @ColumnInfo(name = "enc_index_key")
    val encIndexKey: ByteArray,
    @ColumnInfo(name = "enc_bubbles_key")
    val encBubblesKey: ByteArray,
    @ColumnInfo(name = "enc_item_edition_key")
    val encItemEditionKey: ByteArray,
    @ColumnInfo(name = "biometric_crypto_material")
    val biometricCryptoMaterial: BiometricCryptoMaterial?,
    @ColumnInfo(name = "auto_destruction_key", defaultValue = "NULL")
    val autoDestructionKey: ByteArray?,
) {

    companion object {
        fun fromSafeCrypto(safeCrypto: SafeCrypto): RoomSafeCrypto = RoomSafeCrypto(
            salt = safeCrypto.salt,
            encTest = safeCrypto.encTest,
            encIndexKey = safeCrypto.encIndexKey,
            encBubblesKey = safeCrypto.encBubblesKey,
            encItemEditionKey = safeCrypto.encItemEditionKey,
            biometricCryptoMaterial = safeCrypto.biometricCryptoMaterial,
            autoDestructionKey = safeCrypto.autoDestructionKey,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomSafeCrypto

        if (!salt.contentEquals(other.salt)) return false
        if (!encTest.contentEquals(other.encTest)) return false
        if (!encIndexKey.contentEquals(other.encIndexKey)) return false
        if (!encBubblesKey.contentEquals(other.encBubblesKey)) return false
        if (!encItemEditionKey.contentEquals(other.encItemEditionKey)) return false
        if (biometricCryptoMaterial != other.biometricCryptoMaterial) return false
        if (autoDestructionKey != null) {
            if (other.autoDestructionKey == null) return false
            if (!autoDestructionKey.contentEquals(other.autoDestructionKey)) return false
        } else if (other.autoDestructionKey != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = salt.contentHashCode()
        result = 31 * result + encTest.contentHashCode()
        result = 31 * result + encIndexKey.contentHashCode()
        result = 31 * result + encBubblesKey.contentHashCode()
        result = 31 * result + encItemEditionKey.contentHashCode()
        result = 31 * result + (biometricCryptoMaterial?.hashCode() ?: 0)
        result = 31 * result + (autoDestructionKey?.contentHashCode() ?: 0)
        return result
    }
}
