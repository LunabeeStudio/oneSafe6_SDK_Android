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
 * Last modified 10/3/24, 12:47 PM
 */

package studio.lunabee.onesafe.migration

import studio.lunabee.onesafe.domain.model.safe.BiometricCryptoMaterial
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.test.firstSafeId

object AppMigrationsTestUtils {
    fun safeData0(
        version: Int,
        masterKey: ByteArray = byteArrayOf(),
        id: SafeId = firstSafeId,
        salt: ByteArray = byteArrayOf(),
        encTest: ByteArray = byteArrayOf(),
        encIndexKey: ByteArray? = null,
        encBubblesKey: ByteArray? = null,
        encItemEditionKey: ByteArray? = null,
        biometricCryptoMaterial: BiometricCryptoMaterial? = null,
    ): MigrationSafeData0 = MigrationSafeData0(
        masterKey = masterKey,
        version = version,
        id = id,
        salt = salt,
        encTest = encTest,
        encIndexKey = encIndexKey,
        encBubblesKey = encBubblesKey,
        encItemEditionKey = encItemEditionKey,
        biometricCryptoMaterial = biometricCryptoMaterial,
    )

    fun safeData15(
        masterKey: ByteArray = byteArrayOf(),
        version: Int = 0,
        id: SafeId = firstSafeId,
        salt: ByteArray = byteArrayOf(),
        encTest: ByteArray = byteArrayOf(),
        encIndexKey: ByteArray = byteArrayOf(),
        encBubblesKey: ByteArray? = null,
        encItemEditionKey: ByteArray = byteArrayOf(),
        biometricCryptoMaterial: BiometricCryptoMaterial? = null,
    ): MigrationSafeData15 = MigrationSafeData15(
        masterKey = masterKey,
        version = version,
        id = id,
        salt = salt,
        encTest = encTest,
        encIndexKey = encIndexKey,
        encBubblesKey = encBubblesKey,
        encItemEditionKey = encItemEditionKey,
        biometricCryptoMaterial = biometricCryptoMaterial,
    )
}
