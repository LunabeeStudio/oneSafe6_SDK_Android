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
 * Last modified 10/3/24, 10:05 AM
 */

package studio.lunabee.onesafe.migration

import studio.lunabee.onesafe.domain.model.safe.BiometricCryptoMaterial
import studio.lunabee.onesafe.domain.model.safe.SafeId

class MigrationSafeData0(
    val masterKey: ByteArray,
    val version: Int,
    val id: SafeId,
    val salt: ByteArray,
    val encTest: ByteArray,
    val encIndexKey: ByteArray?,
    val encBubblesKey: ByteArray?,
    val encItemEditionKey: ByteArray?,
    val biometricCryptoMaterial: BiometricCryptoMaterial?,
) {
    fun as15(): MigrationSafeData15 {
        return MigrationSafeData15(
            masterKey = masterKey,
            version = version,
            id = id,
            salt = salt,
            encTest = encTest,
            encIndexKey = encIndexKey!!,
            encBubblesKey = encBubblesKey,
            encItemEditionKey = encItemEditionKey!!,
            biometricCryptoMaterial = biometricCryptoMaterial,
        )
    }
}

// Safe data after V15 have non null encIndexKey and encItemEditionKey
class MigrationSafeData15(
    val masterKey: ByteArray,
    val version: Int,
    val id: SafeId,
    val salt: ByteArray,
    val encTest: ByteArray,
    val encIndexKey: ByteArray,
    val encBubblesKey: ByteArray?,
    val encItemEditionKey: ByteArray,
    val biometricCryptoMaterial: BiometricCryptoMaterial?,
)
