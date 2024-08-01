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
 * Created by Lunabee Studio / Date - 7/17/2024 - for the oneSafe6 SDK.
 * Last modified 7/17/24, 9:08 AM
 */

package studio.lunabee.onesafe.domain.model.safe

@JvmInline
value class BiometricCryptoMaterial(
    val raw: ByteArray,
) {
    constructor(iv: ByteArray, key: ByteArray) : this(iv + key) {
        check(iv.size == AesIvSizeByte)
        check(key.size == AesEncKeySizeByte)
    }

    /**
     * IV associated to the encrypted key
     */
    val iv: ByteArray
        get() = raw.copyOfRange(0, AesIvSizeByte)

    /**
     * Encrypted key
     */
    val encKey: ByteArray
        get() = raw.copyOfRange(AesIvSizeByte, AesIvSizeByte + AesEncKeySizeByte)
}

private const val AesIvSizeByte = 16
private const val AesEncKeySizeByte = 48
