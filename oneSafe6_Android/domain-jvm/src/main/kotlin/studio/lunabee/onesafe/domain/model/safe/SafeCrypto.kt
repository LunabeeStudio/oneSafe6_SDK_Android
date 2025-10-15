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
 * Last modified 6/6/24, 4:48 PM
 */

package studio.lunabee.onesafe.domain.model.safe

/**
 * Represent a safe with all the cryptographic stuff encrypted
 *
 * @property id The unique [SafeId] of the safe
 * @property salt Salt used for password derivation
 * @property encTest Encrypted value to verify a password against the safe
 * @property encIndexKey Encrypted key used to encrypt the index table
 * @property encBubblesKey Encrypted key used to encrypt the bubbles tables
 * @property encItemEditionKey Encrypted key used to encrypt items during edition/creation
 * @property biometricCryptoMaterial Master key encrypted with biometric
 * @property autoDestructionKey key derive from auto destruction password and `salt` used for safe auto destruction
 */
data class SafeCrypto(
    val id: SafeId,
    val salt: ByteArray,
    val encTest: ByteArray,
    val encIndexKey: ByteArray,
    val encBubblesKey: ByteArray,
    val encItemEditionKey: ByteArray,
    val biometricCryptoMaterial: BiometricCryptoMaterial?,
    val autoDestructionKey: ByteArray?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SafeCrypto

        if (id != other.id) return false
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
        var result = id.hashCode()
        result = 31 * result + salt.contentHashCode()
        result = 31 * result + encTest.contentHashCode()
        result = 31 * result + encIndexKey.contentHashCode()
        result = 31 * result + encBubblesKey.contentHashCode()
        result = 31 * result + encItemEditionKey.contentHashCode()
        result = 31 * result + (biometricCryptoMaterial?.hashCode() ?: 0)
        result = 31 * result + (autoDestructionKey?.contentHashCode() ?: 0)
        return result
    }
}
