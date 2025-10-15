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
 */

package studio.lunabee.onesafe.cryptography

import studio.lunabee.bubbles.domain.model.MessageSharingMode
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.model.toDoubleRatchetUUID
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.onesafe.error.OSCryptoError
import kotlin.reflect.KClass
import kotlin.time.Instant

class CryptoDataMapper @Inject constructor() {
    operator fun <Data : Any> invoke(mapBlock: ((Data) -> ByteArray)?, data: Data): ByteArray = when {
        mapBlock != null -> mapBlock(data)
        data is String -> data.encodeToByteArray()
        data is Int -> data.toByteArray()
        data is ByteArray -> data
        data is Instant -> data.toEpochMilliseconds().toByteArray()
        data is Boolean -> data.toByteArray()
        data is MessageSharingMode -> data.id.encodeToByteArray()
        data is DoubleRatchetUUID -> data.toByteArray()
        else -> throw OSCryptoError(
            OSCryptoError.Code.MISSING_MAPPER,
            "No mapper found or provided for type ${data::class.simpleName}",
        )
    }

    operator fun <Data : Any> invoke(
        mapBlock: (ByteArray.() -> Data)?,
        rawData: ByteArray,
        clazz: KClass<out Data>,
    ): Data {
        @Suppress("UNCHECKED_CAST")
        return when {
            mapBlock != null -> rawData.mapBlock()
            clazz == String::class -> rawData.decodeToString() as Data
            clazz == Int::class -> rawData.toInt() as Data
            clazz == ByteArray::class -> rawData as Data
            clazz == Instant::class -> Instant.fromEpochMilliseconds(rawData.toLong()) as Data
            clazz == Boolean::class -> rawData.toBoolean() as Data
            clazz == MessageSharingMode::class -> rawData.toMessageSharingMode() as Data
            clazz == DoubleRatchetUUID::class -> rawData.toDoubleRatchetUUID() as Data
            else -> throw OSCryptoError(
                OSCryptoError.Code.MISSING_MAPPER,
                "No mapper found for type ${clazz.simpleName}",
            )
        }
    }
}
