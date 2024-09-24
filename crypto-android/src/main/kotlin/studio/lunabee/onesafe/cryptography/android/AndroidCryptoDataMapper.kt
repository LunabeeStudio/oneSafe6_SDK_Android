/*
 * Copyright (c) 2023-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 9/4/2024 - for the oneSafe6 SDK.
 * Last modified 9/4/24, 9:54 AM
 */

package studio.lunabee.onesafe.cryptography.android

import studio.lunabee.onesafe.cryptography.CryptoDataMapper
import studio.lunabee.onesafe.cryptography.toByteArray
import studio.lunabee.onesafe.cryptography.toLong
import studio.lunabee.onesafe.domain.model.common.IdentifiableObject
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.jvm.toByteArray
import studio.lunabee.onesafe.jvm.toUUID
import java.util.UUID
import javax.inject.Inject
import kotlin.reflect.KClass

class AndroidCryptoDataMapper @Inject constructor(
    private val cryptoDataMapper: CryptoDataMapper,
) {
    operator fun <Data : Any> invoke(mapBlock: ((Data) -> ByteArray)?, data: Data): ByteArray = when {
        mapBlock != null -> mapBlock(data)
        data is IdentifiableObject -> data.toByteArray()
        data is UUID -> data.toByteArray()
        data is java.time.Instant -> data.toEpochMilli().toByteArray()
        else -> cryptoDataMapper(null, data)
    }

    operator fun <Data : Any> invoke(
        mapBlock: (ByteArray.() -> Data)?,
        rawData: ByteArray,
        clazz: KClass<out Data>,
    ): Data {
        @Suppress("UNCHECKED_CAST")
        return when {
            mapBlock != null -> rawData.mapBlock()
            clazz == SafeItemFieldKind::class -> rawData.toSafeItemFieldKind() as Data
            clazz == UUID::class -> rawData.toUUID() as Data
            clazz == java.time.Instant::class -> java.time.Instant.ofEpochMilli(rawData.toLong()) as Data
            else -> cryptoDataMapper(null, rawData, clazz)
        }
    }
}